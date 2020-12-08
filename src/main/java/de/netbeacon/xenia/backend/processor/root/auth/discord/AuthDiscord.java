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
import de.netbeacon.xenia.joop.tables.records.OauthRecord;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import org.jooq.Result;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class AuthDiscord extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(AuthDiscord.class);

    public AuthDiscord(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("discord", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            if(!ctx.queryParamMap().containsKey("code") || !ctx.queryParamMap().containsKey("scope")){
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
            UUID uuid = UUID.randomUUID();
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES*2);
            byteBuffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
            String authToken = Base64.getEncoder().encodeToString(String.valueOf(userId).getBytes())+Base64.getEncoder().encodeToString(byteBuffer.array());
            String authTokenHash = BCrypt.hashpw(authToken, BCrypt.gensalt(4));
            // insert all that to da db
            Result<OauthRecord> recordResult = sqlContext
                    .insertInto(Tables.OAUTH, Tables.OAUTH.USER_ID, Tables.OAUTH.LOCAL_AUTH_HASH, Tables.OAUTH.LOCAL_AUTH_INVALIDATION_TIME, Tables.OAUTH.DISCORD_ACCESS_TOKEN, Tables.OAUTH.DISCORD_REFRESH_TOKEN, Tables.OAUTH.DISCORD_INVALIDATION_TIME, Tables.OAUTH.DISCORD_SCOPES)
                    .values(userId, authTokenHash, LocalDateTime.now().plusMinutes(60), token.getAccessToken(), token.getRefreshToken(), token.expiresOn(), token.getScopes())
                    .onConflict(Tables.OAUTH.USER_ID)
                    .doUpdate()
                    .set(Tables.OAUTH.LOCAL_AUTH_HASH, authTokenHash)
                    .set(Tables.OAUTH.LOCAL_AUTH_INVALIDATION_TIME, LocalDateTime.now().plusMinutes(60))
                    .set(Tables.OAUTH.DISCORD_ACCESS_TOKEN, token.getAccessToken())
                    .set(Tables.OAUTH.DISCORD_REFRESH_TOKEN, token.getRefreshToken())
                    .set(Tables.OAUTH.DISCORD_INVALIDATION_TIME, token.expiresOn())
                    .set(Tables.OAUTH.DISCORD_SCOPES, token.getScopes())
                    .where(Tables.OAUTH.USER_ID.eq(userId))
                    .returning()
                    .fetch();
            if(recordResult.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            OauthRecord record = recordResult.get(0);
            // prepare nice response
            JSONObject jsonObject = new JSONObject()
                    .put("authToken", authToken)
                    .put("validUntil", record.getLocalAuthInvalidationTime());
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing AuthDiscord#POST ", e);
            throw new BadRequestResponse();
        }
    }
}
