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
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebsocketProcessor implements IShutdown {

    private final ConcurrentHashMap<WsContext, Client> wsContextClientConcurrentHashMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public WebsocketProcessor(){
        // start heartbeat messages
        scheduledExecutorService.scheduleAtFixedRate(()-> broadcast(getHeartBeatMessage()), 0, 5000, TimeUnit.MILLISECONDS);
    }

    private BroadcastMessage getHeartBeatMessage(){
        WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
        broadcastMessage.get().put("type", "HEARTBEAT").put("action", "HEARTBEAT").put("timestamp", System.currentTimeMillis());
        return broadcastMessage;
    }

    public void register(WsContext wsConnectContext, Client client){
        if(client == null){
            try{wsConnectContext.session.disconnect();}catch (Exception ignore){} // should not be needed
            return;
        }
        wsContextClientConcurrentHashMap.put(wsConnectContext, client);
    }

    public void remove(WsContext wsConnectContext){
        wsContextClientConcurrentHashMap.remove(wsConnectContext);
    }

    public void broadcast(BroadcastMessage broadcastMessage){
        broadcast(broadcastMessage, null);
    }

    public void broadcast(BroadcastMessage broadcastMessage, Client except){
        for(Map.Entry<WsContext, Client> entry : wsContextClientConcurrentHashMap.entrySet()){
            try{
                if(entry.getValue().equals(except)){
                    continue;
                }
                entry.getKey().send(broadcastMessage.asString());
            }catch (Exception e){
                try{entry.getKey().session.disconnect();}catch (Exception ignore){}
            }
        }
    }

    @Override
    public void onShutdown() throws Exception {
        scheduledExecutorService.shutdown();
    }

    public static class BroadcastMessage{

        private final JSONObject message = new JSONObject();

        public JSONObject get(){
            return message;
        }

        public String asString(){
            return message.toString()+"/n";
        }
    }

}
