/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.xenia.backend.processor.root.auth.discord;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.utils.oauth.DiscordOAuthHandler;
import de.netbeacon.xenia.joop.Tables;
import io.javalin.http.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

public class AuthDiscord extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(AuthDiscord.class);

    public AuthDiscord(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("discord", sqlConnectionPool, websocketProcessor,
                new AuthDiscordVerify(sqlConnectionPool, websocketProcessor),
                new AuthDiscordRevoke(sqlConnectionPool, websocketProcessor),
                new AuthDiscordRenew(sqlConnectionPool, websocketProcessor),
                new AuthDiscordPrepare(sqlConnectionPool, websocketProcessor)
        );
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(!context.method().equalsIgnoreCase("get")){
            throw new ForbiddenResponse();
        }
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            if(!ctx.queryParamMap().containsKey("code") || !ctx.queryParamMap().containsKey("scope") || !ctx.queryParamMap().containsKey("state")){
                throw new BadRequestResponse();
            }
            String state = ctx.queryParam("state");
            String owner = null;
            if(ctx.queryParamMap().containsKey("ownerId")){
                owner = ctx.queryParam("ownerId");
            }
            // verify state
            int mod0 = sqlContext.deleteFrom(Tables.OAUTH_STATES)
                    .where(Tables.OAUTH_STATES.STATE.eq(state).and((owner != null) ? Tables.OAUTH_STATES.STATE_OWNER.eq(owner) : Tables.OAUTH_STATES.STATE_OWNER.isNull()))
                    .execute();
            if(mod0 != 1){
                throw new BadRequestResponse();
            }
            DiscordOAuthHandler.Token token;
            try{
                token = DiscordOAuthHandler.getInstance().retrieve(ctx.queryParam("code"), new DiscordOAuthHandler.Scopes(ctx.queryParams("scope")));
            }catch (DiscordOAuthHandler.Exception e){
                throw new BadRequestResponse();
            }
            // get user id
            long userId = DiscordOAuthHandler.getInstance().getUserID(token);
            // generate auth
            byte[] bytes = new byte[64];
            new SecureRandom().nextBytes(bytes);
            String localAuthSecret = Base64.getEncoder().encodeToString(bytes);
            String localAuthToken = Jwts.builder()
                    .setIssuedAt(new Date())
                    .setSubject("Auth")
                    .setHeaderParam("cid", userId)
                    .setHeaderParam("isDiscordToken", true)
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(localAuthSecret)))
                    .compact();
            // insert all that to da db
            int mod = sqlContext
                    .insertInto(Tables.OAUTH, Tables.OAUTH.USER_ID, Tables.OAUTH.LOCAL_AUTH_SECRET, Tables.OAUTH.DISCORD_ACCESS_TOKEN, Tables.OAUTH.DISCORD_REFRESH_TOKEN, Tables.OAUTH.DISCORD_INVALIDATION_TIME, Tables.OAUTH.DISCORD_SCOPES)
                    .values(userId, localAuthSecret, token.getAccessToken(), token.getRefreshToken(), token.expiresOn(), token.getScopes())
                    .onConflict(Tables.OAUTH.USER_ID)
                    .doUpdate()
                    .set(Tables.OAUTH.LOCAL_AUTH_SECRET, localAuthSecret)
                    .set(Tables.OAUTH.DISCORD_ACCESS_TOKEN, token.getAccessToken())
                    .set(Tables.OAUTH.DISCORD_REFRESH_TOKEN, token.getRefreshToken())
                    .set(Tables.OAUTH.DISCORD_INVALIDATION_TIME, token.expiresOn())
                    .set(Tables.OAUTH.DISCORD_SCOPES, token.getScopes())
                    .where(Tables.OAUTH.USER_ID.eq(userId))
                    .execute();
            if(mod == 0){
                throw new InternalServerErrorResponse();
            }
            // prepare nice response
            JSONObject jsonObject = new JSONObject()
                    .put("authToken", localAuthToken);
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing AuthDiscord#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing AuthDiscord#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
