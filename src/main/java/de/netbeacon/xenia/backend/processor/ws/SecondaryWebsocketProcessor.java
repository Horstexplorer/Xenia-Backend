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
import io.javalin.websocket.WsMessageContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondaryWebsocketProcessor extends WebsocketProcessor {

    private final Logger logger = LoggerFactory.getLogger(SecondaryWebsocketProcessor.class);

    @Override
    public WsMessage getHeartBeatMessage() {
        WsMessage wsMessage = new WsMessage();
        wsMessage.get().put("type", "HEARTBEAT").put("timestamp", System.currentTimeMillis());
        return wsMessage;
    }

    /**
     * type:
     *  HEARTBEAT
     *  BROADCAST
     *  UNICAST
     * recipient: (set by client when UNICAST type)
     *  Long
     * sender: (set by backend)
     *  Long
     * timestamp:
     *  Long
     */

    @Override
    public void onMessage(WsMessageContext wsMessageContext) {
        try{
            JSONObject jsonObject = new JSONObject(wsMessageContext.message());
            switch (jsonObject.getString("type").toLowerCase()){
                case "unicast":{
                    Client sender = getSelfClient(wsMessageContext);
                    Client recipient = findClient(jsonObject.getLong("recipient"));
                    if(recipient == null) return;
                    jsonObject.put("sender", sender.getClientId());
                    unicast(new WsMessage(jsonObject), recipient);
                }
                break;
                case "broadcast":
                default: {
                    Client sender = getSelfClient(wsMessageContext);
                    jsonObject.put("sender", sender.getClientId());
                    broadcast(new WsMessage(jsonObject), sender);
                }
                break;
            }
        }catch (Exception e){
            logger.debug("");
        }
    }
}
