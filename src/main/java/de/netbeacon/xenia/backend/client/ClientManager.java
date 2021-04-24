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

package de.netbeacon.xenia.backend.client;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.client.objects.imp.LocalClient;
import org.json.JSONObject;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager implements IShutdown{

	private final File localData;
	private final SQLConnectionPool sqlConnectionPool;
	private final ConcurrentHashMap<Long, LocalClient> localClients = new ConcurrentHashMap<>();

	public ClientManager(File localData, SQLConnectionPool sqlConnectionPool){
		this.localData = localData;
		this.sqlConnectionPool = sqlConnectionPool;
	}

	public Client getClient(ClientType clientType, long clientId){
		if(ClientType.INTERNAL.containsType(clientType)){
			return localClients.get(clientId); // will return from cache
		}
		else if(ClientType.DISCORD.containsType(clientType)){
			return DiscordClient.create(clientId, sqlConnectionPool); // will return db linked object
		}
		else{
			return null;
		}
	}

	public Client createLocalClient(ClientType clientType, String clientName, String password){
		if(ClientType.INTERNAL.containsType(clientType)){
			LocalClient client = LocalClient.create(clientType, clientName, password);
			localClients.put(client.getClientId(), client);
			return client;
		}
		return null;
	}

	public void deleteLocalClient(long clientId){
		localClients.remove(clientId);
	}

	public ClientManager loadFromFile() throws IOException{
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(localData))){
			String line;
			while((line = bufferedReader.readLine()) != null){
				if(line.isBlank()){
					continue;
				}
				LocalClient client = LocalClient.create(new JSONObject(line));
				localClients.put(client.getClientId(), client);
			}
		}
		return this;
	}

	public ClientManager writeToFile() throws IOException{
		try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(localData))){
			for(Map.Entry<Long, LocalClient> entry : localClients.entrySet()){
				bufferedWriter.write(entry.getValue().asJSON().toString());
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		}
		return this;
	}

	public int size(){
		return localClients.size();
	}

	@Override
	public void onShutdown() throws Exception{
		writeToFile();
		// do not shut down sql connection pool
	}

}
