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
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.VrolesRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildRole extends RequestProcessor {

    public final Logger logger = LoggerFactory.getLogger(DataGuildRole.class);

    public DataGuildRole(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("role", sqlConnectionPool, websocketProcessor);
    }

    private static final long DISCORD_USER_PERM_FILTER = 268435457; // interact, guild_roles_ov

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            if(((DiscordClient)client).getInternalRole().equalsIgnoreCase("admin")){
                return this;
            }
            long guildId = Long.parseLong(context.pathParam("guildId"));
            try(var con = getSqlConnectionPool().getConnection()) {
                var sqlContext = getSqlConnectionPool().getContext(con);

                Result<Record> memberGuildRelations = sqlContext.select()
                        .from(Tables.MEMBERS)
                        .join(Tables.GUILDS)
                        .on(Tables.MEMBERS.GUILD_ID.eq(Tables.GUILDS.GUILD_ID))
                        .where(Tables.MEMBERS.GUILD_ID.eq(guildId).and(Tables.MEMBERS.USER_ID.eq(client.getClientId())))
                        .fetch();
                if(memberGuildRelations.isEmpty()){
                    throw new BadRequestResponse();
                }
                Record memberGuildRelation = memberGuildRelations.get(0);
                if((!memberGuildRelation.get(Tables.GUILDS.USE_VPERMS) && memberGuildRelation.get(Tables.MEMBERS.META_IS_ADMINISTRATOR)) || memberGuildRelation.get(Tables.MEMBERS.META_IS_OWNER)){
                    return this;
                }
                Result<Record> vpermRecords = sqlContext.select()
                        .from(Tables.MEMBERS_ROLES)
                        .join(Tables.VROLES)
                        .on(Tables.MEMBERS_ROLES.ROLE_ID.eq(Tables.VROLES.VROLE_ID))
                        .where(
                                Tables.MEMBERS_ROLES.GUILD_ID.eq(Long.parseLong(context.pathParam("guildId")))
                                        .and(Tables.MEMBERS_ROLES.USER_ID.eq(client.getClientId()))
                                        .and(bitAnd(DISCORD_USER_PERM_FILTER, Tables.VROLES.VROLE_ID).eq(DISCORD_USER_PERM_FILTER))
                        )
                        .fetch();
                if(vpermRecords.isEmpty()){
                    throw new ForbiddenResponse();
                }
            }catch (HttpResponseException e){
                if(e instanceof InternalServerErrorResponse){
                    logger.error("An Error Occurred Processing DataGuild#PRE ", e);
                }
                throw e;
            }catch (NullPointerException e){
                throw new BadRequestResponse();
            }catch (Exception e){
                logger.warn("An Error Occurred Processing DataGuild#PRE ", e);
                throw new BadRequestResponse();
            }
        }
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
            PrimaryWebsocketProcessor.WsMessage wsMessage = new PrimaryWebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_ROLE").put("action", "CREATE").put("guildId", guildId).put("roleId", roleId);
            getWebsocketProcessor().broadcast(wsMessage, client);
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
            PrimaryWebsocketProcessor.WsMessage wsMessage = new PrimaryWebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_ROLE").put("action", "CREATE").put("guildId", guildId).put("roleId", rolesRecord.getVroleId());
            getWebsocketProcessor().broadcast(wsMessage, client);
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
            PrimaryWebsocketProcessor.WsMessage wsMessage = new PrimaryWebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_ROLE").put("action", "DELETE").put("guildId", guildId).put("roleId", roleId);
            getWebsocketProcessor().broadcast(wsMessage, client);
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
