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

package de.netbeacon.xenia.backend.processor.root.data.frontend.guildlist;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.GuildsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jooq.impl.DSL.bitAnd;

public class FrontendGuildList extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(FrontendGuildList.class);

    public FrontendGuildList(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("guildlist", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(!client.getClientType().equals(ClientType.DISCORD) || !context.method().equalsIgnoreCase("get")){
            throw new ForbiddenResponse();
        }
        return this;
    }

    private static final long DISCORD_USER_PERM_FILTER = 268435457; // interact, web_use

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()) {
            var sqlContext = getSqlConnectionPool().getContext(con);
            // select all guilds the user is able to interact with
            Result<GuildsRecord> records;
            if(((DiscordClient)client).getInternalRole().equalsIgnoreCase("admin")){
                records = sqlContext.selectFrom(Tables.GUILDS).fetch();
            }else{
                records = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.in(
                        sqlContext.select(Tables.MEMBERS_ROLES.GUILD_ID)
                                .from(Tables.MEMBERS_ROLES)
                                .join(Tables.VROLES)
                                .on(Tables.MEMBERS_ROLES.ROLE_ID.eq(Tables.VROLES.VROLE_ID))
                                .where(
                                        Tables.MEMBERS_ROLES.USER_ID.eq(client.getClientId())
                                                .and(bitAnd(DISCORD_USER_PERM_FILTER, Tables.VROLES.VROLE_ID).eq(DISCORD_USER_PERM_FILTER))
                                )
                                .groupBy(Tables.MEMBERS_ROLES.GUILD_ID)
                )).fetch();
            }

            JSONArray jsonArray = new JSONArray();

            for(GuildsRecord record : records){
                jsonArray.put(new JSONObject()
                        .put("guildId", record.getGuildId())
                        .put("guildName", record.getMetaGuildname())
                        .put("iconUrl", (record.getMetaIconurl() != null) ? record.getMetaIconurl() : JSONObject.NULL)
                );
            }

            JSONObject jsonObject = new JSONObject().put("guilds", jsonArray);
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing FrontendGuildList#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing FrontendGuildList#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
