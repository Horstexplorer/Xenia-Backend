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

package de.netbeacon.xenia.backend.processor.root;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.ClientManager;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.root.auth.AuthRoot;
import de.netbeacon.xenia.backend.processor.root.data.DataRoot;
import de.netbeacon.xenia.backend.processor.root.info.InfoRoot;
import de.netbeacon.xenia.backend.processor.root.management.ManagementRoot;
import de.netbeacon.xenia.backend.processor.root.setup.SetupRoot;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;

public class Root extends RequestProcessor{

	public Root(ClientManager clientManager, SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("", sqlConnectionPool, websocketProcessor,
			new AuthRoot(sqlConnectionPool, websocketProcessor),
			new SetupRoot(sqlConnectionPool, websocketProcessor),
			new InfoRoot(sqlConnectionPool, websocketProcessor),
			new DataRoot(sqlConnectionPool, websocketProcessor),
			new ManagementRoot(clientManager, sqlConnectionPool, websocketProcessor)
		);
	}

}
