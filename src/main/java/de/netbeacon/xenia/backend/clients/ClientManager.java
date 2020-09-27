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

package de.netbeacon.xenia.backend.clients;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.clients.objects.Client;


import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager implements IShutdown {

    private final File data;
    private final ConcurrentHashMap<Long, Client> clientMap = new ConcurrentHashMap<>();

    public ClientManager(File data){
        this.data = data;
    }



    public Client getClient(long userId){
        return clientMap.get(userId);
    }

    public Client createClient(Client.Type type, String clientName, String clientPassword){
        Client client = new Client(type, clientName, clientPassword);
        clientMap.put(client.getClientId(), client);
        return client;
    }

    public void deleteClient(long userId){
        clientMap.remove(userId);
    }



    public ClientManager loadFromFile() {
        return this;
    }

    public ClientManager writeToFile() {
        return this;
    }



    @Override
    public void onShutdown() throws Exception {
        writeToFile();
    }
}
