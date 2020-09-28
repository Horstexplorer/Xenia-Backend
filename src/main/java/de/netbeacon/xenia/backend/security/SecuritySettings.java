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

import de.netbeacon.xenia.backend.clients.objects.Client;

public class SecuritySettings {

    public enum AuthType {
        Optional,
        Required,
        Basic,
        Token;
    }

    public enum ClientType {
        Any,
        System,
        Bot,
        WebInterface;
    }

    private final AuthType authType;
    private final ClientType clientType;

    public SecuritySettings(AuthType authType, ClientType clientType){
        this.authType = authType;
        this.clientType = clientType;
    }

    public AuthType getRequiredAuthType() {
        return authType;
    }

    public ClientType getRequiredClientType() {
        return clientType;
    }
}
