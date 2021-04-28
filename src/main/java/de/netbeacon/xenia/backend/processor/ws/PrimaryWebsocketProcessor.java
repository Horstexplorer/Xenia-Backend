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

public class PrimaryWebsocketProcessor extends de.netbeacon.xenia.backend.processor.WebsocketProcessor{

	public PrimaryWebsocketProcessor(){
		super();
	}

	@Override
	public WsMessage getConnectedMessage(){
		WsMessage wsMessage = new WsMessage();
		wsMessage.get().put("type", "status").put("action", "CONNECTED");
		return wsMessage;
	}

	@Override
	public WsMessage getHeartBeatMessage(){
		WsMessage wsMessage = new WsMessage();
		wsMessage.get().put("type", "HEARTBEAT").put("action", "HEARTBEAT").put("timestamp", System.currentTimeMillis());
		return wsMessage;
	}

}
