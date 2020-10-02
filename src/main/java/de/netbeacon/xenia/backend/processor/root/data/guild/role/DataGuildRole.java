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
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.PermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesPermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesRecord;
import io.javalin.http.*;
import org.jooq.InsertValuesStep2;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataGuildRole extends RequestProcessor {

    public final Logger logger = LoggerFactory.getLogger(DataGuildRole.class);

    public DataGuildRole(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("role", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String roleIdS = ctx.pathParam("roleId");
            JSONObject jsonObject = new JSONObject();
            if(roleIdS.isBlank()){
                Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.GUILD_ID.eq(guildId)).fetch();
                if(rolesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("roles", jsonArray);
                for(RolesRecord rolesRecord : rolesRecords){
                    Result<Record> rolesPermissions = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(rolesRecord.getRoleId())).fetch();
                    JSONArray jsonArray2 = new JSONArray();
                    for(Record record : rolesPermissions){
                        jsonArray2.put(new JSONObject()
                                .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                                .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                                .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                                .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED))
                        );
                    }
                    jsonArray.put(new JSONObject()
                                    .put("guildId", rolesRecord.getGuildId())
                                    .put("roleId", rolesRecord.getRoleId())
                                    .put("roleName", rolesRecord.getRoleName())
                                    .put("permissions", jsonArray));
                }
            }else{
                long roleId = Long.parseLong(roleIdS);
                Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.ROLE_ID.eq(roleId).and(Tables.ROLES.GUILD_ID.eq(guildId))).fetch();
                Result<Record> rolesPermissions = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId)).fetch();
                if(rolesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                RolesRecord rolesRecord = rolesRecords.get(0);
                JSONArray jsonArray = new JSONArray();
                for(Record record : rolesPermissions){
                    jsonArray.put(new JSONObject()
                            .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                            .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                            .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                            .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED))
                    );
                }
                jsonObject
                        .put("guildId", rolesRecord.getGuildId())
                        .put("roleId", rolesRecord.getRoleId())
                        .put("roleName", rolesRecord.getRoleName())
                        .put("permissions", jsonArray);
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
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
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            // fetch
            Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.ROLE_ID.eq(roleId).and(Tables.ROLES.GUILD_ID.eq(guildId))).fetch();
            if(rolesRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            RolesRecord rolesRecord = rolesRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update object data
            rolesRecord.setRoleName(newData.getString("roleName"));
            // update with db
            sqlContext.executeUpdate(rolesRecord);
            // set permissions
            Result<RolesPermissionRecord> rolesPermissionRecords = sqlContext.selectFrom(Tables.ROLES_PERMISSION).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId)).fetch();
            List<RolesPermissionRecord> updateRoles = new ArrayList<>();
            JSONArray jsonArray = newData.getJSONArray("permissions");
            for(RolesPermissionRecord rolesPermissionRecord : rolesPermissionRecords){
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if((rolesPermissionRecord.getPermissionId() != jsonObject.getInt("permissionId")) || (rolesPermissionRecord.getPermissionGranted() == jsonObject.getBoolean("permissionGranted"))){
                        break;
                    }
                    updateRoles.add(rolesPermissionRecord.setPermissionGranted(jsonObject.getBoolean("permissionGranted")));
                }
            }
            sqlContext.batchUpdate(updateRoles).execute();
            // fetch full data for permissions
            Result<Record> rolesPermissions = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId)).fetch();
            // json
            for(Record record : rolesPermissions){
                jsonArray.put(new JSONObject()
                        .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                        .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                        .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                        .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED))
                );
            }
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", rolesRecord.getGuildId())
                    .put("roleId", rolesRecord.getRoleId())
                    .put("roleName", rolesRecord.getRoleName())
                    .put("permissions", jsonArray);
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
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
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            // insert role
            sqlContext.insertInto(Tables.ROLES, Tables.ROLES.GUILD_ID, Tables.ROLES.ROLE_ID).values(guildId, roleId).execute();
            // insert permissions
            Result<PermissionRecord> permissionRecords = sqlContext.selectFrom(Tables.PERMISSION).fetch();
            InsertValuesStep2<RolesPermissionRecord, Long, Integer> ivs = sqlContext.insertInto(Tables.ROLES_PERMISSION).columns(Tables.ROLES_PERMISSION.ROLE_ID, Tables.ROLES_PERMISSION.PERMISSION_ID);
            for(PermissionRecord permissionRecord : permissionRecords){
                ivs.values(roleId, permissionRecord.getPermissionId());
            }
            ivs.execute();
            // fetch
            Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.ROLE_ID.eq(roleId).and(Tables.ROLES.GUILD_ID.eq(guildId))).fetch();
            Result<Record> rolesPermissions = sqlContext.select().from(Tables.ROLES_PERMISSION).leftJoin(Tables.PERMISSION).on(Tables.ROLES_PERMISSION.PERMISSION_ID.eq(Tables.PERMISSION.PERMISSION_ID)).where(Tables.ROLES_PERMISSION.ROLE_ID.eq(roleId)).fetch();
            if(rolesRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            RolesRecord rolesRecord = rolesRecords.get(0);
            JSONArray jsonArray = new JSONArray();
            for(Record record : rolesPermissions){
                jsonArray.put(new JSONObject()
                        .put("permissionId", record.get(Tables.PERMISSION.PERMISSION_ID))
                        .put("permissionName", record.get(Tables.PERMISSION.PERMISSION_NAME))
                        .put("permissionDescription", record.get(Tables.PERMISSION.PERMISSION_DESCRIPTION))
                        .put("permissionGranted", record.get(Tables.ROLES_PERMISSION.PERMISSION_GRANTED))
                );
            }
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", rolesRecord.getGuildId())
                    .put("roleId", rolesRecord.getRoleId())
                    .put("roleName", rolesRecord.getRoleName())
                    .put("permissions", jsonArray);
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
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
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long roleId = Long.parseLong(ctx.pathParam("roleId"));
            int mod = sqlContext.deleteFrom(Tables.ROLES).where(Tables.ROLES.ROLE_ID.eq(roleId).and(Tables.ROLES.GUILD_ID.eq(guildId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
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
