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

package de.netbeacon.xenia.backend.client.objects;

import de.netbeacon.xenia.backend.security.SecuritySettings;

public abstract class Client {

    private final long clientId;
    private final ClientType clientType;

    public Client(long clientId, ClientType clientType){
        this.clientId = clientId;
        this.clientType = clientType;
    }

    public long getClientId() {
        return clientId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public abstract boolean verifyAuth(SecuritySettings.AuthType authType, String credentials);
}
