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
import org.json.JSONObject;


public class DataUser extends RequestProcessor {

    public DataUser(SQLConnectionPool sqlConnectionPool) {
        super("user", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        // get user data
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long userid = Long.parseLong(ctx.pathParam("userid"));
            UsersRecord usersRecord = context.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userid)).fetch().get(0);
            // build a nice fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTime", usersRecord.getCreationTimestamp())
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return those values
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (Exception e) {
            // log properly
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        // update user data
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long userid = Long.parseLong(ctx.pathParam("userid"));
            // get the current user data
            UsersRecord usersRecord = context.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userid)).fetch().get(0);
            // get the json body
            JSONObject newClientObject = new JSONObject(ctx.body());
            // check if data matches
            if(newClientObject.getLong("userId") != usersRecord.getUserId()){
                throw new BadRequestResponse("Invalid Data");
            }
            // update data
            usersRecord.setInternalRole(newClientObject.getString("internalRole"));
            usersRecord.setPreferredLanguage(newClientObject.getString("preferredLanguage"));
            context.executeUpdate(usersRecord);
            // build a nice fluffy json
            JSONObject clientObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTime", usersRecord.getCreationTimestamp())
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return those values
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(clientObject.toString());
        }catch (Exception e) {
            // log properly
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        // create new
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long userid = Long.parseLong(ctx.pathParam("userid"));
            // insert create new user
            context.insertInto(Tables.USERS, Tables.USERS.USER_ID).values(userid).execute();
            // fetch this user
            UsersRecord usersRecord = context.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userid)).fetch().get(0);
            // build a nice fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("userId", usersRecord.getUserId())
                    .put("creationTime", usersRecord.getCreationTimestamp())
                    .put("internalRole", usersRecord.getInternalRole())
                    .put("preferredLanguage", usersRecord.getPreferredLanguage());
            // return those values
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        } catch (Exception e) {
            // log properly
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        // delete user
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long userid = Long.parseLong(ctx.pathParam("userid"));
            context.deleteFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userid)).execute();
            ctx.status(200);
        }catch (Exception e) {
            // log properly
            throw new BadRequestResponse();
        }
    }
}
