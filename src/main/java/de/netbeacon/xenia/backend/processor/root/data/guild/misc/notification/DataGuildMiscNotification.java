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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.notification;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import io.javalin.http.Context;

public class DataGuildMiscNotification extends RequestProcessor {

    public DataGuildMiscNotification(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("notifications", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        super.get(client, ctx);
    }

    @Override
    public void put(Client client, Context ctx) {
        super.put(client, ctx);
    }

    @Override
    public void post(Client client, Context ctx) {
        super.post(client, ctx);
    }

    @Override
    public void delete(Client client, Context ctx) {
        super.delete(client, ctx);
    }
}
