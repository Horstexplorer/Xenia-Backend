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

package de.netbeacon.xenia.backend.processor.root.setup.bot;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.InternalBotDataRecord;
import de.netbeacon.xenia.joop.tables.records.InternalBotShardsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SetupBot extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(SetupBot.class);

    public SetupBot(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("bot", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            // fetch data for this client
            long clientId = client.getClientId();
            Result<InternalBotDataRecord> internalBotDataRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_DATA).where(Tables.INTERNAL_BOT_DATA.CLIENT_ID.eq(clientId)).fetch();
            if(internalBotDataRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            InternalBotDataRecord internalBotData = internalBotDataRecords.get(0);
            // count shards
            int shardsTotal = sqlContext.fetchCount(Tables.INTERNAL_BOT_SHARDS);
            // get shards
            Result<InternalBotShardsRecord> internalBotShardsRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_SHARDS).where(Tables.INTERNAL_BOT_SHARDS.CLIENT_ID.eq(clientId)).fetch();
            if(internalBotShardsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            List<Integer> shards = new ArrayList<>();
            for(InternalBotShardsRecord internalBotShardsRecord : internalBotShardsRecords){
                shards.add(internalBotShardsRecord.getShardId());
            }
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("clientId", internalBotData.getClientId())
                    .put("clientName", internalBotData.getClientName())
                    .put("clientDescription", internalBotData.getClientInfo())
                    .put("discordToken", internalBotData.getDiscordToken())
                    .put("cryptHash", internalBotData.getMessageCryptHash())
                    .put("shards", new JSONObject()
                            .put("total", shardsTotal)
                            .put("use", shards));
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
            logger.warn("An Error Occurred Processing InfoPublic#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
