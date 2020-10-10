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

package de.netbeacon.xenia.backend.processor.root.data;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.DataGuild;
import de.netbeacon.xenia.backend.processor.root.data.message.DataMessage;
import de.netbeacon.xenia.backend.processor.root.data.user.DataUser;

public class DataRoot extends RequestProcessor {

    public DataRoot(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("data", sqlConnectionPool, websocketProcessor,
                new DataGuild(sqlConnectionPool, websocketProcessor),
                new DataUser(sqlConnectionPool, websocketProcessor),
                new DataMessage(sqlConnectionPool, websocketProcessor)
        );
    }

}
