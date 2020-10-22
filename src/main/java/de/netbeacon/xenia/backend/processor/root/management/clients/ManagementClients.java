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

package de.netbeacon.xenia.backend.processor.root.management.clients;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.ClientManager;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.LocalClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.InternalBotDataRecord;
import de.netbeacon.xenia.joop.tables.records.InternalBotShardsRecord;
import io.javalin.http.*;
import org.jooq.InsertValuesStep2;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ManagementClients extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(ManagementClients.class);
    private final ClientManager clientManager;

    public ManagementClients(ClientManager clientManager, SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("clients", sqlConnectionPool, websocketProcessor);
        this.clientManager = clientManager;
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long clientId = Long.parseLong(ctx.queryParam("clientId"));
            // json
            JSONObject jsonObject = new JSONObject();
            // get local data
            LocalClient client1 = (LocalClient) clientManager.getClient(ClientType.INTERNAL, clientId);
            if(client1 == null){
                throw new NotFoundResponse();
            }
            if(ClientType.BOT.containsType(client1.getClientType())){
                // get db data
                Result<InternalBotDataRecord> botDataRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_DATA).where(Tables.INTERNAL_BOT_DATA.CLIENT_ID.eq(clientId)).fetch();
                if(botDataRecords.isEmpty()){
                    throw new InternalServerErrorResponse();
                }
                InternalBotDataRecord internalBotDataRecord = botDataRecords.get(0);
                Result<InternalBotShardsRecord> botShardsRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_SHARDS).where(Tables.INTERNAL_BOT_SHARDS.CLIENT_ID.eq(clientId)).fetch();
                List<Integer> shards = new ArrayList<>();
                for(InternalBotShardsRecord botShardsRecord : botShardsRecords){
                    shards.add(botShardsRecord.getShardId());
                }
                // json
                jsonObject.put("db", new JSONObject()
                        .put("clientName", internalBotDataRecord.getClientName())
                        .put("clientInfo", internalBotDataRecord.getClientInfo())
                        .put("discordToken", internalBotDataRecord.getDiscordToken())
                        .put("shards", shards));
            }
            // json
            jsonObject
                    .put("clientID", client1.getClientId())
                    .put("local", new JSONObject()
                            .put("clientName", client1.getClientName())
                            .put("clientType", client1.getClientType()));
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
            logger.warn("An Error Occurred Processing ManagementClients#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long clientId = Long.parseLong(ctx.queryParam("clientId"));
            // get local client
            LocalClient client1 = (LocalClient) clientManager.getClient(ClientType.INTERNAL, clientId);
            if(client1 == null){
                throw new NotFoundResponse();
            }
            JSONObject jsonObject = new JSONObject();
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // set client data
            if(newData.getJSONObject("local").has("password")){
                client1.getAuth().setPassword(newData.getJSONObject("local").getString("password"));
            }
            // check db things
            if(ClientType.BOT.containsType(client1.getClientType())){
                // get from db
                Result<InternalBotDataRecord> internalBotDataRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_DATA).where(Tables.INTERNAL_BOT_DATA.CLIENT_ID.eq(clientId)).fetch();
                if(internalBotDataRecords.isEmpty()){
                    throw new InternalServerErrorResponse();
                }
                InternalBotDataRecord internalBotDataRecord = internalBotDataRecords.get(0);
                sqlContext.deleteFrom(Tables.INTERNAL_BOT_SHARDS).where(Tables.INTERNAL_BOT_SHARDS.CLIENT_ID.eq(clientId)).execute();
                JSONObject newDataDB = newData.getJSONObject("db");
                // update data
                internalBotDataRecord.setClientName(newDataDB.getString("clientName"));
                internalBotDataRecord.setClientInfo(newDataDB.getString("clientInfo"));
                internalBotDataRecord.setDiscordToken(newDataDB.getString("discordToken"));
                // shards
                List<Integer> newShards = (List<Integer>)(List<?>)newDataDB.getJSONObject("shards");
                InsertValuesStep2<InternalBotShardsRecord, Long, Integer> ivs = sqlContext.insertInto(Tables.INTERNAL_BOT_SHARDS).columns(Tables.INTERNAL_BOT_SHARDS.CLIENT_ID, Tables.INTERNAL_BOT_SHARDS.SHARD_ID);
                for(int shardId : newShards){
                    ivs.values(clientId, shardId);
                }
                ivs.execute();
                sqlContext.executeUpdate(internalBotDataRecord);
                // json
                jsonObject.put("db", new JSONObject()
                        .put("clientName", internalBotDataRecord.getClientName())
                        .put("clientInfo", internalBotDataRecord.getClientInfo())
                        .put("discordToken", internalBotDataRecord.getDiscordToken())
                        .put("shards", newShards));
            }
            // json
            jsonObject
                    .put("clientID", client1.getClientId())
                    .put("local", new JSONObject()
                            .put("clientName", client1.getClientName())
                            .put("clientType", client1.getClientType()));
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
            logger.warn("An Error Occurred Processing ManagementClients#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            String clientName = ctx.queryParam("clientName");
            String password = ctx.queryParam("clientPassword");
            ClientType clientType = ClientType.fromString(ctx.queryParam("clientType"));
            if(clientName == null ||password == null || clientType == null || ClientType.ANY.equals(clientType)){
                throw new BadRequestResponse();
            }
            // create local client
            LocalClient client1 = (LocalClient) clientManager.createLocalClient(clientType, clientName, password);
            // create db if bot
            if(ClientType.BOT.containsType(client1.getClientType())){
                int mod = sqlContext.insertInto(Tables.INTERNAL_BOT_DATA, Tables.INTERNAL_BOT_DATA.CLIENT_ID, Tables.INTERNAL_BOT_DATA.CLIENT_NAME).values(client1.getClientId(), client1.getClientName()).execute();
                if(mod == 0){
                    logger.error("Could Not Create Bot Data On DB");
                    throw new InternalServerErrorResponse();
                }
            }
            // json
            JSONObject jsonObject = new JSONObject();
            if(ClientType.BOT.containsType(client1.getClientType())){
                // get db data
                Result<InternalBotDataRecord> botDataRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_DATA).where(Tables.INTERNAL_BOT_DATA.CLIENT_ID.eq(client1.getClientId())).fetch();
                if(botDataRecords.isEmpty()){
                    throw new InternalServerErrorResponse();
                }
                InternalBotDataRecord internalBotDataRecord = botDataRecords.get(0);
                Result<InternalBotShardsRecord> botShardsRecords = sqlContext.selectFrom(Tables.INTERNAL_BOT_SHARDS).where(Tables.INTERNAL_BOT_SHARDS.CLIENT_ID.eq(client1.getClientId())).fetch();
                List<Integer> shards = new ArrayList<>();
                for(InternalBotShardsRecord botShardsRecord : botShardsRecords){
                    shards.add(botShardsRecord.getShardId());
                }
                // json
                jsonObject.put("db", new JSONObject()
                        .put("clientName", internalBotDataRecord.getClientName())
                        .put("clientInfo", internalBotDataRecord.getClientInfo())
                        .put("discordToken", internalBotDataRecord.getDiscordToken())
                        .put("shards", shards));
            }
            // json
            jsonObject
                    .put("clientID", client1.getClientId())
                    .put("local", new JSONObject()
                            .put("clientName", client1.getClientName())
                            .put("clientType", client1.getClientType()));
            // return
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing ManagementClients#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long clientId = Long.parseLong(ctx.queryParam("clientId"));
            // get local client
            Client client1 = clientManager.getClient(ClientType.INTERNAL, clientId);
            if(client1 == null){
                throw new NotFoundResponse();
            }
            if(ClientType.BOT.containsType(client1.getClientType())){
                sqlContext.deleteFrom(Tables.INTERNAL_BOT_DATA).where(Tables.INTERNAL_BOT_DATA.CLIENT_ID.eq(clientId)).execute();
            }
            clientManager.deleteLocalClient(clientId);
            // result
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing ManagementClients#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
