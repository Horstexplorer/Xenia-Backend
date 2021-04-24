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
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;

public class AuthTokenRenew extends RequestProcessor{

	public AuthTokenRenew(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("renew", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(client.getClientType().equals(ClientType.DISCORD)){
			throw new ForbiddenResponse();
		}
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		ctx.status(204);
	}

}
