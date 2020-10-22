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

package de.netbeacon.xenia.backend.processor;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RequestProcessor {

    private final String identifier;
    private final SQLConnectionPool sqlConnectionPool;
    private final WebsocketProcessor websocketProcessor;
    private final ConcurrentHashMap<String, RequestProcessor> processorHashMap = new ConcurrentHashMap<>();

    public RequestProcessor(String identifier, SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor){
        this.identifier = identifier;
        this.sqlConnectionPool = sqlConnectionPool;
        this.websocketProcessor = websocketProcessor;
    }

    public RequestProcessor(String identifier, SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor, RequestProcessor...requestProcessors){
        this.identifier = identifier;
        this.sqlConnectionPool = sqlConnectionPool;
        this.websocketProcessor = websocketProcessor;
        Arrays.stream(requestProcessors).forEach(p->processorHashMap.put(p.getIdentifier(), p));
    }

    public String getIdentifier() {
        return identifier;
    }

    public SQLConnectionPool getSqlConnectionPool(){
        return sqlConnectionPool;
    }

    public WebsocketProcessor getWebsocketProcessor(){
        return websocketProcessor;
    }

    public RequestProcessor next(String identifier){
        return processorHashMap.get(identifier);
    }

    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    public void get(Client client, Context ctx){
        ctx.result("Not Implemented");
    }

    public void put(Client client, Context ctx){
        ctx.result("Not Implemented");
    }

    public void post(Client client, Context ctx){
        ctx.result("Not Implemented");
    }

    public void delete(Client client, Context ctx){
        ctx.result("Not Implemented");
    }
}
