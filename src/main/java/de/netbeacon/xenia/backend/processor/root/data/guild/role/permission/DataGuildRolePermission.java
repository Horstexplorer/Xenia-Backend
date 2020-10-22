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

package de.netbeacon.xenia.backend.processor.root.data.guild.role.permission;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.PermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesPermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGuildRolePermission extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildRolePermission.class);

    public DataGuildRolePermission(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("permission", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int permissionId = Integer.parseInt(ctx.pathParam("permissionId"));
            Result<Record> rolesPermissionsRecords = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId).and(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(permissionId)))).fetch();
            if(rolesPermissionsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            Record record = rolesPermissionsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                    .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                    .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                    .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED));
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildRolePermission#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRolePermission#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int permissionId = Integer.parseInt(ctx.pathParam("permissionId"));
            Result<RolesPermissionRecord> rolesPermissionRecords = sqlContext.selectFrom(Tables.ROLES_PERMISSION).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId).and(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(permissionId))).fetch();
            if(rolesPermissionRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            RolesPermissionRecord rolesPermissionRecord = rolesPermissionRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update object data
            rolesPermissionRecord.setPermissionGranted(newData.getBoolean("permissionGranted"));
            // update with db
            sqlContext.executeUpdate(rolesPermissionRecord);
            Result<Record> rolesPermissionsRecords = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId).and(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(permissionId)))).fetch();
            if(rolesPermissionsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            Record record = rolesPermissionsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                    .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                    .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                    .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED));
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE_PERMISSION").put("action", "UPDATE").put("guildId", guildId).put("roleId", roleId).put("permissionId", permissionId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRolePermission#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int permissionId = Integer.parseInt(ctx.pathParam("permissionId"));
            Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES.ROLE_ID.eq(roleId))).fetch();
            if(rolesRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            Result<PermissionRecord> permissionRecords = sqlContext.selectFrom(Tables.PERMISSION).fetch();
            if(permissionRecords.isEmpty() || !permissionRecords.getValues(Tables.PERMISSION.PERMISSION_ID).contains(permissionId)){
                throw new NotFoundResponse();
            }
           sqlContext.insertInto(Tables.ROLES_PERMISSION, Tables.ROLES_PERMISSION.ROLE_ID, Tables.ROLES_PERMISSION.PERMISSION_ID).values(roleId, permissionId).execute();
            if(rolesRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            Result<Record> rolesPermissionsRecords = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId).and(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(permissionId)))).fetch();
            if(rolesPermissionsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            Record record = rolesPermissionsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                    .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                    .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                    .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED));
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE_PERMISSION").put("action", "CREATE").put("guildId", guildId).put("roleId", roleId).put("permissionId", permissionId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildRolePermission#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int permissionId = Integer.parseInt(ctx.pathParam("permissionId"));
            Result<RolesPermissionRecord> rolesPermissionRecords = sqlContext.selectFrom(Tables.ROLES_PERMISSION).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId).and(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(permissionId)))).fetch();
            if(rolesPermissionRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            // deactivate by setting it to not granted
            sqlContext.executeUpdate(rolesPermissionRecords.get(0).setPermissionGranted(false));
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_ROLE_PERMISSION").put("action", "UPDATE").put("guildId", guildId).put("roleId", roleId).put("permissionId", permissionId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing InfoPublic#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing InfoPublic#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
