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

package de.netbeacon.xenia.backend.processor.root.data.user;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.UsersRecord;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;


public class DataUser extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataUser.class);

    public DataUser(SQLConnectionPool sqlConnectionPool) {
        super("user", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
            if(usersRecordResult.isEmpty()){
                throw new NotFoundResponse();
            }
            // get user data
            UsersRecord usersRecord = usersRecordResult.get(0);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTimestamp", usersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataUser#Get", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            // fetch
            Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
            if(usersRecordResult.isEmpty()){
                throw new NotFoundResponse();
            }
            UsersRecord usersRecord = usersRecordResult.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            if(newData.getLong("userId") != userId){
                throw new BadRequestResponse("Invalid Data");
            }
            // update values
            usersRecord.setPreferredLanguage(newData.getString("preferredLanguage"));
            usersRecord.setInternalRole(newData.getString("internalRole"));
            // update db
            sqlContext.executeUpdate(usersRecord);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTimestamp", usersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataUser#Put", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            // insert - this should throw if insertion failed
            sqlContext.insertInto(Tables.USERS, Tables.USERS.USER_ID).values(userId).execute();
            // fetch
            Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
            if(usersRecordResult.isEmpty()){
                logger.error("An Error Occurred Processing DataUser#Post: Fetch Returned No Result After Creating It");
                throw new NotFoundResponse(); // should not throw
            }
            // get user data
            UsersRecord usersRecord = usersRecordResult.get(0);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTimestamp", usersRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return response
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataUser#Post", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long userId = Long.parseLong(ctx.pathParam("userId"));
            int mod = sqlContext.deleteFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataUser#Delete", e);
            throw new BadRequestResponse();
        }
    }
}
