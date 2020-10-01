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
import org.jooq.InsertValuesStep2;
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
            String userIdS = ctx.pathParam("userId");
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            JSONObject jsonObject = new JSONObject();
            if(userIdS.isBlank()){
                // get all members
                JSONArray members = new JSONArray();
                jsonObject.put("members", members);
                Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.GUILD_ID.eq(guildId)).fetch();
                for(MembersRecord membersRecord : membersRecords){
                    Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(membersRecord.getUserId())).fetch();
                    JSONArray roles = new JSONArray();
                    JSONObject member = new JSONObject()
                            .put("userId", membersRecord.getUserId())
                            .put("guildId", membersRecord.getGuildId())
                            .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                            .put("roles", roles);
                    members.put(member);
                    for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                        roles.put(membersRolesRecord.getRoleId());
                    }
                }
            }else{
                // get specific member
                long userId = Long.parseLong(userIdS);
                // fetch
                Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId)).fetch();
                if(membersRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                MembersRecord membersRecord = membersRecords.get(0);
                Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId)).fetch();
                // build json
                JSONArray roles = new JSONArray();
                jsonObject
                        .put("userId", membersRecord.getUserId())
                        .put("guildId", membersRecord.getGuildId())
                        .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                        .put("roles", roles);
                for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                    roles.put(membersRolesRecord.getRoleId());
                }
            }
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#Get", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // fetch
            Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId)).fetch();
            if(membersRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            MembersRecord membersRecord = membersRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update object values
                // nothing to update for now
            // update object roles - delete roles & add only the ones mentioned in the json again
            sqlContext.deleteFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId)).execute(); // delete current roles
            List<Long> roleIdList= (List<Long>)(List<?>) newData.getJSONArray("roles");
            Result<RolesRecord> rolesRecords = sqlContext.selectFrom(Tables.ROLES).where(Tables.ROLES.GUILD_ID.eq(guildId).and(Tables.ROLES.ROLE_ID.in(roleIdList))).fetch(); // get roles wanted & available for this guild
            InsertValuesStep2<MembersRolesRecord, Long, Long> ivst = sqlContext.insertInto(Tables.MEMBERS_ROLES).columns(Tables.MEMBERS_ROLES.USER_ID, Tables.MEMBERS_ROLES.ROLE_ID); // build and execute new role add request
            for(long roleId : rolesRecords.getValues(Tables.ROLES.ROLE_ID)){ ivst.values(userId, roleId);}
            ivst.execute();
            Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId)).fetch(); // fetch roles for this user
            // update db
            sqlContext.executeUpdate(membersRecord);
            // build json
            JSONArray roles = new JSONArray();
            JSONObject jsonObject = new JSONObject()
                    .put("userId", membersRecord.getUserId())
                    .put("guildId", membersRecord.getGuildId())
                    .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("roles", roles);
            for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                roles.put(membersRolesRecord.getRoleId());
            }
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#Put", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // insert
            sqlContext.insertInto(Tables.MEMBERS, Tables.MEMBERS.GUILD_ID, Tables.MEMBERS.USER_ID).values(guildId, userId).execute();
            // fetch
            Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId)).fetch();
            if(membersRecords.isEmpty()){
                logger.error("An Error Occurred Processing DataGuildMember#Post: Fetch Returned No Result After Creating It");
                throw new NotFoundResponse();
            }
            MembersRecord membersRecord = membersRecords.get(0);
            Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId)).fetch();
            // build json
            JSONArray roles = new JSONArray();
            JSONObject jsonObject = new JSONObject()
                    .put("userId", membersRecord.getUserId())
                    .put("guildId", membersRecord.getGuildId())
                    .put("creationTimestamp", membersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("roles", roles);
            for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
                roles.put(membersRolesRecord.getRoleId());
            }
            // return response
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#Post", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            int mod = sqlContext.deleteFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId)).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMember#Delete", e);
            throw new BadRequestResponse();
        }
    }
}
