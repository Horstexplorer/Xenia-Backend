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

import de.netbeacon.utils.tuples.Triplet;
import de.netbeacon.xenia.backend.client.objects.ClientType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SecuritySettings {

    public enum AuthType {
        OPTIONAL,
        REQUIRED,
        BASIC,
        BEARER;
    }


    private final UUID uuid = UUID.randomUUID();
    private final AuthType authType;
    private final ClientType clientType;

    private final HashMap<ClientType, Triplet<TimeUnit, Integer, Long>> rateLimiterSettings = new HashMap<>();
    private static final Triplet<TimeUnit, Integer, Long> DEFAULT_RATELIMITER_SETTING = new Triplet<>(TimeUnit.MINUTES, 1, 200000L);

    public SecuritySettings(AuthType authType, ClientType clientType){
        this.authType = authType;
        this.clientType = clientType;
    }

    public UUID getSecSetUUID(){
        return uuid;
    }

    public AuthType getRequiredAuthType() {
        return authType;
    }

    public ClientType getRequiredClientType() {
        return clientType;
    }

    public SecuritySettings putRateLimiterSetting(ClientType clientType, TimeUnit timeUnit, Integer timeScale, Long requests){
        rateLimiterSettings.put(clientType, new Triplet<>(timeUnit, timeScale, requests));
        return this;
    }

    public Triplet<TimeUnit, Integer, Long> getRateLimiterSettings(ClientType clientType){
        if(rateLimiterSettings.containsKey(clientType)){
            return rateLimiterSettings.get(clientType);
        }
        return DEFAULT_RATELIMITER_SETTING;
    }
}
