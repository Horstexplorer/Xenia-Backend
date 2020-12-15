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

package de.netbeacon.xenia.backend.processor.root.auth.discord;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import io.javalin.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthDiscordRevoke extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(AuthDiscordRevoke.class);

    public AuthDiscordRevoke(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("revoke", sqlConnectionPool, websocketProcessor);
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
        try(var con = getSqlConnectionPool().getConnection()) {
            var sqlContext = getSqlConnectionPool().getContext(con);
            int mod = sqlContext.deleteFrom(Tables.OAUTH).where(Tables.OAUTH.USER_ID.eq(client.getClientId())).execute();
            if(mod == 0){
                throw new InternalServerErrorResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing AuthDiscordRevoke#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing AuthDiscordRevoke#GET ", e);
            throw new BadRequestResponse();
        }
    }

}
