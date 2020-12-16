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

package de.netbeacon.xenia.backend.processor.root.data.frontend.me;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.UsersRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendMe extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(FrontendMe.class);

    public FrontendMe(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("me", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(!client.getClientType().equals(ClientType.DISCORD) || !context.method().equalsIgnoreCase("get")){
            throw new ForbiddenResponse();
        }
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long userId = Long.parseLong(ctx.pathParam("userId"));
            Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
            if(usersRecordResult.isEmpty()){
                throw new NotFoundResponse();
            }
            UsersRecord usersRecord = usersRecordResult.get(0);
            // fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("id", usersRecord.getUserId())
                    .put("name",usersRecord.getMetaUsername())
                    .put("icon", usersRecord.getMetaIconurl())
                    .put("internalRole", usersRecord.getInternalRole());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing FrontendMe#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing FrontendMe#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
