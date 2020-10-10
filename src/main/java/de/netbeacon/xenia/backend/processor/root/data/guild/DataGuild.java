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

package de.netbeacon.xenia.backend.processor.root.data.guild;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.channel.DataGuildChannel;
import de.netbeacon.xenia.backend.processor.root.data.guild.license.DataGuildLicense;
import de.netbeacon.xenia.backend.processor.root.data.guild.member.DataGuildMember;
import de.netbeacon.xenia.backend.processor.root.data.guild.role.DataGuildRole;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGuild extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuild.class);

    public DataGuild(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("guild", sqlConnectionPool, websocketProcessor,
                new DataGuildMember(sqlConnectionPool, websocketProcessor),
                new DataGuildChannel(sqlConnectionPool, websocketProcessor),
                new DataGuildRole(sqlConnectionPool, websocketProcessor),
                new DataGuildLicense(sqlConnectionPool, websocketProcessor)
        );
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
