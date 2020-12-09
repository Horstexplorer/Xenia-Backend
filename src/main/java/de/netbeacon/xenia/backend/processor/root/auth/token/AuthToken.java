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

package de.netbeacon.xenia.backend.processor.root.auth.token;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.LocalClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpResponseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class AuthToken extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(AuthToken.class);

    public AuthToken(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("token", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            throw new ForbiddenResponse();
        }
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            String authToken = Base64.getEncoder().encodeToString(String.valueOf(client.getClientId()).getBytes())+"."+((LocalClient) client).getAuth().getToken();
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("token", authToken);
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
            logger.warn("An Error Occurred Processing AuthToken#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
