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

import java.util.HashSet;
import java.util.List;

public class ClientType {

    // SINGLE
    public static final ClientType DISCORD = new ClientType("DISCORD", 10);
    public static final ClientType BOT = new ClientType("BOT", 100);
    public static final ClientType WEB_INTERFACE = new ClientType("WEB_INTERFACE", 1000);
    public static final ClientType METRICS = new ClientType("METRICS", 2500);
    public static final ClientType SYSTEM = new ClientType("SYSTEM", 10000);
    // GROUPS
    public static final ClientType INTERNAL = new ClientType("INTERNAL", 1,
            List.of(SYSTEM, WEB_INTERFACE, BOT, METRICS)
    );
    public static final ClientType REMOTE = new ClientType("REMOTE", 2,
            List.of(DISCORD)
    );
    public static final ClientType ANY = new ClientType("ANY", 0,
           List.of(INTERNAL, REMOTE)
    );


    private final String name;
    private final int value;
    private final HashSet<ClientType> childTypes = new HashSet<>();

    protected ClientType(String name, int value){
        this.name = name;
        this.value = value;
    }

    protected ClientType(String name, int value, List<ClientType> childTypes){
        this.name = name;
        this.value = value;
        this.childTypes.addAll(childTypes);
    }

    public String getName(){
        return name;
    }

    public int getValue(){
        return value;
    }

    public boolean containsType(ClientType clientType){
        if(this.equals(clientType)){
            return true;
        }else{
            for(ClientType child : childTypes){
                if(child.containsType(clientType)){
                    return true;
                }
            }
            return false;
        }
    }

    public static ClientType fromString(String string){
        switch (string.toLowerCase()){
            case "discord":
                return DISCORD;
            case "bot":
                return BOT;
            case "web_interface":
                return WEB_INTERFACE;
            case "internal":
                return INTERNAL;
            case "system":
                return SYSTEM;
            case "remote":
                return REMOTE;
            case "any":
                return ANY;
            default:
                return null;
        }
    }
}
