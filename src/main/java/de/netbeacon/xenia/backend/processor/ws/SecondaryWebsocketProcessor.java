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

import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSProcessorCore;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class SecondaryWebsocketProcessor extends WebsocketProcessor {

    private final WSProcessorCore wsProcessorCore;
    private final Logger logger = LoggerFactory.getLogger(SecondaryWebsocketProcessor.class);

    public SecondaryWebsocketProcessor(WSProcessorCore wsProcessorCore){
        this.wsProcessorCore = wsProcessorCore;
        this.wsProcessorCore.setWSP(this);
    }

    @Override
    public WsMessage getConnectedMessage() {
        return new WsMessage(getMessage(getRandomId(), "BROADCAST", null,0L, "connected", null));
    }

    @Override
    public WsMessage getHeartBeatMessage() {
        return new WsMessage(getMessage(getRandomId(), "BROADCAST", null, 0L, "heartbeat", null));
    }

    /**
     * requestId:
     *  String
     * requestMode:
     *  UNICAST
     *  BROADCAST
     *  RESPONSE
     * recipient: (removed by backend; ignored for BROADCAST)
     *  Long
     * sender: (set by backend)
     *  Long
     * action: (missing on response)
     *  String
     * payload: (optional)
     *  JSONObject
     */

    @Override
    public void register(WsContext wsContext, Client client){
        super.register(wsContext, client);
        // send join
        broadcast(new WsMessage(getMessage(getRandomId(), "BROADCAST", null, 0L, "join", new JSONObject().put("clientId", client.getClientId()))), client);
    }


    @Override
    public void remove(WsContext wsContext){
        Client client = getClientOf(wsContext);
        if(client == null){
            return;
        }
        // send leave
        broadcast(new WsMessage(getMessage(getRandomId(), "BROADCAST", null, 0L, "leave", new JSONObject().put("clientId", client.getClientId()))), client);
        super.remove(wsContext);
    }


    @Override
    public void onMessage(WsMessageContext wsMessageContext) {
        try{
            JSONObject jsonObject = new JSONObject(wsMessageContext.message());
            // redirect responses
            if(jsonObject.getString("requestMode").equalsIgnoreCase("response")){
                // get client to send to
                Client recipient = findClient(jsonObject.getLong("recipient"));
                if(recipient == null && jsonObject.getLong("recipient") != 0L){ // filter requests to the backend
                    return;
                }
                // prepare message
                WsMessage wsMessage = new WsMessage(getMessage(jsonObject.getString("requestId"), "RESPONSE", null, getClientOf(wsMessageContext).getClientId(), jsonObject.getString("action"), (jsonObject.has("payload") ? jsonObject.getJSONObject("payload") : null)));
                // send back
                if(jsonObject.getLong("recipient") != 0L){
                    unicast(wsMessage, recipient);
                }else{
                    wsProcessorCore.handle(wsMessage.get()); // this is meant for the backend
                }
            }else if(jsonObject.getString("requestMode").equalsIgnoreCase("unicast")){
                // get client to send to
                Client recipient = findClient(jsonObject.getLong("recipient"));
                if(recipient == null && jsonObject.getLong("recipient") != 0L){ // filter requests to the backend
                    return; // the other things arent interesting to us
                }
                // prepare message
                WsMessage wsMessage = new WsMessage(getMessage(jsonObject.getString("requestId"), "UNICAST", null, getClientOf(wsMessageContext).getClientId(), jsonObject.getString("action"), (jsonObject.has("payload") ? jsonObject.getJSONObject("payload") : null)));
                // send back
                if(jsonObject.getLong("recipient") != 0L){
                    unicast(wsMessage, recipient);
                }else{
                    wsProcessorCore.handle(wsMessage.get()); // this is meant for the backend
                }
            }else if(jsonObject.getString("requestMode").equalsIgnoreCase("broadcast")){
                // prepare message
                WsMessage wsMessage = new WsMessage(getMessage(jsonObject.getString("requestId"), "BROADCAST", null, getClientOf(wsMessageContext).getClientId(), jsonObject.getString("action"), (jsonObject.has("payload") ? jsonObject.getJSONObject("payload") : null)));
                // send back
                broadcast(wsMessage, getClientOf(wsMessageContext));
                wsProcessorCore.handle(wsMessage.get()); // send to backend as this might be contacted via broadcasts aswell
            }
        }catch (Exception e){
            logger.error("Something Went Wrong Handling An Incoming Message: "+wsMessageContext.message()+" ", e);
        }
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public String getRandomId(){
        byte[] bytes = new byte[128];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public JSONObject getMessage(String id, String requestMode, Long recipient, Long sender, String action, JSONObject payload){
        return new JSONObject()
                .put("requestId", id)
                .put("requestMode", requestMode)
                .put("recipient", recipient)
                .put("sender", sender)
                .put("action", action)
                .put("payload", payload);
    }

    @Override
    public void onShutdown() throws Exception {
        super.onShutdown();
        wsProcessorCore.onShutdown();
    }
}
