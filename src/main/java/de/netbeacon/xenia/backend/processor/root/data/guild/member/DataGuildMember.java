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

package de.netbeacon.xenia.backend.processor.root.data.guild.member;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.MembersRecord;
import de.netbeacon.xenia.joop.tables.records.MembersRolesRecord;
import de.netbeacon.xenia.joop.tables.records.RolesRecord;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import org.jooq.InsertValuesStep3;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.List;


public class DataGuildMember extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMember.class);

    public DataGuildMember(SQLConnectionPool sqlConnectionPool) {
        super("member", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String userIds = ctx.pathParam("userId");
            JSONObject jsonObject = new JSONObject();
            if(userIds.isBlank()){
                Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.GUILD_ID.eq(guildId)).fetch();
                if(membersRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("members", jsonArray);
                for(MembersRecord membersRecord : membersRecords){
                    jsonArray.put(new JSONObject()
                            .put("guildId", membersRecord.getGuildId())
                            .put("userId", membersRecord.getUserId())
                            .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC)));
                }
            }else{
                long userId = Long.parseLong(userIds);
                Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
                Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId))).fetch();
                if(membersRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                MembersRecord membersRecord = membersRecords.get(0);
                JSONArray jsonArray = new JSONArray();
                for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                    jsonArray.put(membersRolesRecord.getRoleId());
                }
                jsonObject
                        .put("guildId", membersRecord.getGuildId())
                        .put("userId", membersRecord.getUserId())
                        .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                        .put("roles", membersRolesRecords);
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#DELETE ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long userId = Long.parseLong(ctx.pathParam("userId"));
            // fetch
            Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
            if(membersRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            MembersRecord membersRecord = membersRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update data
                // nothing to update for now
            // update db
            sqlContext.executeUpdate(membersRecord);
            // update roles
            sqlContext.deleteFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId)));
            List<Long> newRoles = (List<Long>)(List<?>) newData.getJSONArray("roles");
            Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.ROLE_ID.in(newRoles).and(Tables.ROLES.GUILD_ID.eq(guildId))).fetch();
            InsertValuesStep3<MembersRolesRecord, Long, Long, Long> ivs = sqlContext.insertInto(Tables.MEMBERS_ROLES).columns(Tables.MEMBERS_ROLES.GUILD_ID, Tables.MEMBERS_ROLES.USER_ID, Tables.MEMBERS_ROLES.ROLE_ID);
            JSONArray jsonArray = new JSONArray();
            for(RolesRecord rolesRecord : rolesRecords){
                ivs.values(guildId, userId, rolesRecord.getRoleId());
                jsonArray.put(rolesRecord.getRoleId());
            }
            ivs.execute();
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", membersRecord.getGuildId())
                    .put("userId", membersRecord.getUserId())
                    .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("roles", jsonArray);
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long userId = Long.parseLong(ctx.pathParam("userId"));
            // insert
            sqlContext.insertInto(Tables.MEMBERS, Tables.MEMBERS.USER_ID, Tables.MEMBERS.GUILD_ID).values(userId, guildId);
            // fetch
            Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
            Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId))).fetch();
            if(membersRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            MembersRecord membersRecord = membersRecords.get(0);
            // json
            JSONArray jsonArray = new JSONArray();
            for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                jsonArray.put(membersRolesRecord.getRoleId());
            }
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", membersRecord.getGuildId())
                    .put("userId", membersRecord.getUserId())
                    .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("roles", jsonArray);
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long userId = Long.parseLong(ctx.pathParam("userId"));
            int mod = sqlContext.deleteFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
