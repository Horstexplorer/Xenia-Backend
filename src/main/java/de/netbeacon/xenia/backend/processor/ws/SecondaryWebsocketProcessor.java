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

package de.netbeacon.xenia.backend.processor.ws;

import de.netbeacon.utils.tuples.Pair;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public class SecondaryWebsocketProcessor extends WebsocketProcessor {

    @Override
    public WsMessage getHeartBeatMessage() {
        return getMessage(getRandomId(), "NONE", 0, System.currentTimeMillis(), "heartbeat", new JSONObject());
    }

    /**
     * requestId:
     *  String
     * requestMode:
     *  UNICAST
     *  BROADCAST
     * responseMode:
     *  NONE
     *  BACKEND_ACK
     *  CLIENT_ACK
     *  DATA
     * recipient: (removed by backend)
     *  Long
     * sender: (set by backend)
     *  Long
     * timestamp: (optional)
     *  Long
     * timeout: (optional)
     *  Long
     * dataHint: (optional)
     *  String
     * data: (optional)
     *  JSONObject
     */

    @Override
    public void register(WsContext wsContext, Client client){
        super.register(wsContext, client);
        broadcast(getMessage(getRandomId(), "NONE", getClientOf(wsContext).getClientId(), System.currentTimeMillis(), "client_join", new JSONObject()), getClientOf(wsContext));
    }


    @Override
    public void remove(WsContext wsContext){
        broadcast(getMessage(getRandomId(), "NONE", getClientOf(wsContext).getClientId(), System.currentTimeMillis(), "client_leave", new JSONObject()), getClientOf(wsContext));
        super.remove(wsContext);
    }


    private static final List<Pair<String, ?>> optionals = List.of(
            new Pair<>("timestamp", Long.class),
            new Pair<>("timeout", Long.class),
            new Pair<>("dataHint", String.class),
            new Pair<>("data", JSONObject.class)
    );

    @Override
    public void onMessage(WsMessageContext wsMessageContext) {
        try{
            JSONObject messageIn = new JSONObject(wsMessageContext.message());
            if(!(messageIn.get("requestId") instanceof String)){
                return; // we cant handle processing the request
            }
            try{
                // put the things in we already can copy
                JSONObject messageOut = new JSONObject()
                        .put("requestId", messageIn.getString("requestId"))
                        .put("sender", getClientOf(wsMessageContext).getClientId());
                // add optionals
                for(Pair<String, ?> pair : optionals){
                    if(messageIn.has(pair.getValue1()) && (messageIn.get(pair.getValue1()).getClass().equals(pair.getValue2()))){
                        messageOut.put(pair.getValue1(), messageIn.get(pair.getValue1()));
                    }
                }
                // set response mode
                switch(messageIn.getString("responseMode").toLowerCase()){
                    case "none":
                    case "client_ack":
                    case "data":
                        messageOut.put("responseMode", messageIn.getString("responseMode"));
                        break;
                    case "backend_ack":
                    default:
                        messageOut.put("responseMode", "NONE");
                        break;
                }
                // send data
                switch(messageIn.getString("requestMode").toLowerCase()){
                    case "unicast":
                        if(messageOut.getLong("recipient") == 0){
                            localProcessor(messageOut, wsMessageContext);
                        }else{
                            unicast(new WsMessage(messageOut), findClient(messageIn.getLong("recipient")));
                        }
                        break;
                    case "broadcast":
                    default:
                        broadcast(new WsMessage(messageOut), getClientOf(wsMessageContext));
                        localProcessor(messageOut, wsMessageContext);
                        break;
                }
                // send backend ack if needed
                if(messageIn.getString("responseMode").equalsIgnoreCase("backend_ack")){
                    unicast(getMessage(messageIn.getString("requestId"), "NONE", 0L, System.currentTimeMillis(), "ack", new JSONObject()), getClientOf(wsMessageContext));
                }
            }catch (Exception e){
                try{ unicast(getMessage(messageIn.getString("requestId"), "NONE", 0L, System.currentTimeMillis(), "error", new JSONObject()), getClientOf(wsMessageContext)); }catch (Exception ignore){}
            }
        }catch (Exception ignore){}
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public String getRandomId(){
        byte[] bytes = new byte[128];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private WsMessage getMessage(String requestId, String responseMode, long sender, long timestamp, String dataHint, JSONObject data){
        return new WsMessage(new JSONObject()
                .put("requestId", requestId)
                .put("responseMode", responseMode)
                .put("sender", sender)
                .put("timestamp", timestamp)
                .put("dataHint", dataHint)
                .put("data", data)
        );
    }

    private void localProcessor(JSONObject request, WsContext wsContext){

    }
}
