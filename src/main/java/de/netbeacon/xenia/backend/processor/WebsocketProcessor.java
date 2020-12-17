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

package de.netbeacon.xenia.backend.processor;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.client.objects.Client;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class WebsocketProcessor implements IShutdown {

    private final ConcurrentHashMap<Client, WsContext> clientWSContextConcurrentHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WsContext, Client> wsContextClientConcurrentHashMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public WebsocketProcessor(){
        scheduledExecutorService.scheduleAtFixedRate(()-> broadcast(getHeartBeatMessage()), 0, 30000, TimeUnit.MILLISECONDS);
    }

    public abstract WsMessage getHeartBeatMessage();

    public void register(WsContext wsConnectContext, Client client){
        if(client == null){
            try{wsConnectContext.session.disconnect();}catch (Exception ignore){} // should not be needed
            return;
        }
        wsContextClientConcurrentHashMap.put(wsConnectContext, client);
        clientWSContextConcurrentHashMap.put(client, wsConnectContext);
    }

    public void remove(WsContext wsConnectContext){
        clientWSContextConcurrentHashMap.remove(wsContextClientConcurrentHashMap.remove(wsConnectContext));
    }

    public void onMessage(WsMessageContext wsMessageContext) {}

    public void broadcast(WsMessage wsMessage){
        broadcast(wsMessage, null);
    }

    public void broadcast(WsMessage wsMessage, Client except){
        for(Map.Entry<Client, WsContext> entry : clientWSContextConcurrentHashMap.entrySet()){
            try{
                if(entry.getKey().equals(except)){
                    continue;
                }
                entry.getValue().send(wsMessage.asString());
            }catch (Exception e){
                try{entry.getValue().session.disconnect();}catch (Exception ignore){}
            }
        }
    }

    public void unicast(WsMessage wsMessage, Client recipient){
        if(clientWSContextConcurrentHashMap.containsKey(recipient)){
            try{
                clientWSContextConcurrentHashMap.get(recipient).send(wsMessage.asString());
            }catch (Exception e){
                try{clientWSContextConcurrentHashMap.get(recipient).session.disconnect();}catch (Exception ignore){}
            }
        }
    }

    public Client getSelfClient(WsContext wsContext){
        return wsContextClientConcurrentHashMap.get(wsContext);
    }

    public Client findClient(long clientId){
        return wsContextClientConcurrentHashMap.values().stream()
                .filter(c -> c.getClientId() == clientId)
                .findFirst().orElse(null);
    }

    @Override
    public void onShutdown() throws Exception {
        scheduledExecutorService.shutdown();
    }

    public static class WsMessage {

        private final JSONObject message;

        public WsMessage(){
            this.message = new JSONObject();
        }

        public WsMessage(JSONObject jsonObject){
            this.message = jsonObject;
        }

        public JSONObject get(){
            return message;
        }

        public String asString(){
            return message.toString()+"/n";
        }
    }
}
