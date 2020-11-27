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

package de.netbeacon.xenia.backend.processor.root.data.guild.role;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.VrolesRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGuildRole extends RequestProcessor {

    public final Logger logger = LoggerFactory.getLogger(DataGuildRole.class);

    public DataGuildRole(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("role", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("roleId")){
                Result<VrolesRecord> rolesRecords = sqlContext.selectFrom(Tables.VROLES).where(Tables.VROLES.GUILD_ID.eq(guildId)).fetch();
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("roles", jsonArray);
                for(VrolesRecord rolesRecord : rolesRecords){
                    jsonArray.put(new JSONObject()
                            .put("guildId", rolesRecord.getGuildId())
                            .put("roleId", rolesRecord.getVroleId())
                            .put("roleName", rolesRecord.getVroleName())
                            .put("rolePermissions", rolesRecord.getVrolePermission()));
                }
            }else{
                long roleId = Long.parseLong(ctx.pathParam("roleId"));
                Result<VrolesRecord> rolesRecords = sqlContext.selectFrom(Tables.VROLES).where(Tables.VROLES.VROLE_ID.eq(roleId).and(Tables.VROLES.GUILD_ID.eq(guildId))).fetch();
                if(rolesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                VrolesRecord rolesRecord = rolesRecords.get(0);
                jsonObject
                        .put("guildId", rolesRecord.getGuildId())
                        .put("roleId", rolesRecord.getVroleId())
                        .put("roleName", rolesRecord.getVroleName())
                        .put("rolePermissions", rolesRecord.getVrolePermission());
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildRole#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRole#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            Result<VrolesRecord> rolesRecords = sqlContext.selectFrom(Tables.VROLES).where(Tables.VROLES.VROLE_ID.eq(roleId).and(Tables.VROLES.GUILD_ID.eq(guildId))).fetch();
            if(rolesRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            VrolesRecord rolesRecord = rolesRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update object data
            rolesRecord.setVroleName(newData.getString("roleName"));
            rolesRecord.setVrolePermission(newData.getLong("rolePermissions"));
            // update with db
            sqlContext.executeUpdate(rolesRecord);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", rolesRecord.getGuildId())
                    .put("roleId", rolesRecord.getVroleId())
                    .put("roleName", rolesRecord.getVroleName())
                    .put("rolePermissions", rolesRecord.getVrolePermission());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE").put("action", "CREATE").put("guildId", guildId).put("roleId", roleId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildRole#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRole#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // long roleId = Long.parseLong(ctx.pathParam("roleId")); - given by db
            Result<VrolesRecord> rolesRecords = sqlContext.insertInto(Tables.VROLES, Tables.VROLES.GUILD_ID).values(guildId).returning().fetch();
            if(rolesRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            VrolesRecord rolesRecord = rolesRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", rolesRecord.getGuildId())
                    .put("roleId", rolesRecord.getVroleId())
                    .put("roleName", rolesRecord.getVroleName())
                    .put("rolePermissions", rolesRecord.getVrolePermission());
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE").put("action", "CREATE").put("guildId", guildId).put("roleId", rolesRecord.getVroleId());
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildRole#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRole#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int mod = sqlContext.deleteFrom(Tables.VROLES).where(Tables.VROLES.VROLE_ID.eq(roleId).and(Tables.VROLES.GUILD_ID.eq(guildId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE").put("action", "DELETE").put("guildId", guildId).put("roleId", roleId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildRole#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRole#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
