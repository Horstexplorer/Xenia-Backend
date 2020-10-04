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

package de.netbeacon.xenia.backend.clients.objects;

import de.netbeacon.utils.json.serial.IJSONSerializable;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import de.netbeacon.utils.security.auth.Auth;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Random;

public class Client implements IJSONSerializable {

    private long clientId;
    private String clientName;
    private SecuritySettings.ClientType clientType = SecuritySettings.ClientType.Unknown;
    private Auth clientAuth = new Auth();

    private final static HashSet<Long> usedIds = new HashSet<>();


    public Client(){
        Random random = new Random();
        this.clientId = Math.abs(random.nextLong());
        while(usedIds.contains(this.clientId)){
            this.clientId = Math.abs(random.nextLong());
        }
        usedIds.add(this.clientId);
    }

    public Client(SecuritySettings.ClientType type, String clientName, String password){
        Random random = new Random();
        this.clientId = Math.abs(random.nextLong());
        while(usedIds.contains(this.clientId)){
            this.clientId = Math.abs(random.nextLong());
        }
        usedIds.add(this.clientId);
        this.clientType = type;
        this.clientName = clientName;
        this.clientAuth.setPassword(password);
    }

    public Client(JSONObject jsonObject){
        if(usedIds.contains(jsonObject.getLong("clientId"))){
            throw new RuntimeException("ID Already In Use");
        }
        usedIds.remove(this.clientId); // remove old
        this.clientId = jsonObject.getLong("clientId");
        usedIds.add(this.clientId); // add new
        this.clientType = SecuritySettings.ClientType.valueOf(jsonObject.getString("clientType"));
        this.clientName = jsonObject.getString("clientName");
        this.clientAuth = new Auth(jsonObject.getJSONObject("clientAuth"));
    }



    public long getClientId(){
        return clientId;
    }

    public String getClientName(){
        return clientName;
    }

    public SecuritySettings.ClientType getClientType(){
        return clientType;
    }

    public Auth getClientAuth() {
        return clientAuth;
    }



    @Override
    public JSONObject asJSON() throws JSONSerializationException {
        return new JSONObject()
                .put("clientId", clientId)
                .put("clientName", clientName)
                .put("clientType", clientType)
                .put("clientAuth", clientAuth.asJSON());
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
        if(usedIds.contains(jsonObject.getLong("clientId"))){
            throw new RuntimeException("ID Already In Use");
        }
        usedIds.remove(this.clientId); // remove old
        this.clientId = jsonObject.getLong("clientId");
        usedIds.add(this.clientId); // add new
        this.clientType = SecuritySettings.ClientType.valueOf(jsonObject.getString("clientType"));
        this.clientName = jsonObject.getString("clientName");
        this.clientAuth = new Auth(jsonObject.getJSONObject("clientAuth"));
    }
}
