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

package de.netbeacon.xenia.backend.security;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.clients.ClientManager;
import de.netbeacon.xenia.backend.clients.objects.Client;
import io.javalin.http.Context;

import java.io.File;
import java.security.Security;

public class SecurityManager implements IShutdown {

    private final ClientManager clientManager;

    public SecurityManager(ClientManager clientManager, File data){
        this.clientManager = clientManager;
    }


    public Client authorizeConnection(SecuritySettings securitySettings, Context ctx){
        return null;
    }


    public SecurityManager loadFromFile() {
        return this;
    }

    public SecurityManager writeToFile() {
        return this;
    }



    @Override
    public void onShutdown() throws Exception {

    }
}
