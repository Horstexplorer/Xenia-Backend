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

package de.netbeacon.xenia.backend.client.objects.imp;

import de.netbeacon.utils.json.serial.IJSONSerializable;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import de.netbeacon.utils.security.auth.Auth;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Random;

public class LocalClient extends Client implements IJSONSerializable {

    private final String clientName;
    private final Auth auth;
    private static final HashSet<Long> usedIDs = new HashSet<>();

    public static LocalClient create(ClientType clientType, String clientName, String password){
        Random random = new Random();
        long clientId = Math.abs(random.nextLong());
        while(usedIDs.contains(clientId)){
            clientId = Math.abs(random.nextLong());
        }
        usedIDs.add(clientId);
        return new LocalClient(clientId, clientType, clientName, password);
    }

    public static LocalClient create(JSONObject jsonObject){
        if(usedIDs.contains(jsonObject.getLong("clientId"))){
            throw new RuntimeException("ID Already In Use");
        }
        usedIDs.add(jsonObject.getLong("clientId"));
        return new LocalClient(jsonObject);
    }

    private LocalClient(long userId, ClientType clientType, String clientName, String password){
        super(userId, clientType);
        this.clientName = clientName;
        this.auth = new Auth();
        this.auth.setPassword(password);
    }

    private LocalClient(JSONObject jsonObject) {
        super(jsonObject.getLong("clientId"), ClientType.fromString(jsonObject.getString("clientType")));
        this.clientName = jsonObject.getString("clientName");
        this.auth = new Auth(jsonObject.getJSONObject("auth"));
    }

    public String getClientName() {
        return clientName;
    }

    public Auth getAuth() {
        return auth;
    }

    @Override
    public boolean verifyAuth(SecuritySettings.AuthType authType, String credentials) {
        if(SecuritySettings.AuthType.BASIC.equals(authType)){
            return auth.verifyPassword(credentials);
        }else if(SecuritySettings.AuthType.TOKEN.equals(authType)){
            return auth.verifyToken(credentials);
        }
        return false;
    }

    @Override
    public JSONObject asJSON() throws JSONSerializationException {
        return new JSONObject()
                .put("clientId", getClientId())
                .put("clientType", getClientType().getName())
                .put("clientName", clientName)
                .put("auth", auth.asJSON());
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
       // not used
    }
}
