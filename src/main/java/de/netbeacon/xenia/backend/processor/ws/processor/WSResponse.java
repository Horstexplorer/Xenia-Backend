/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.xenia.backend.processor.ws.processor;

import org.json.JSONObject;

public class WSResponse{

	private final String requestId;
	private long sender;
	private long recipient;
	private final String action;
	private JSONObject payload;

	private final IO way;

	public enum IO{
		IN,
		OUT
	}


	public WSResponse(JSONObject jsonObject, IO way){
		this.requestId = jsonObject.getString("requestId");
		if(jsonObject.has("sender")){
			this.sender = jsonObject.getLong("sender");
		}
		if(jsonObject.has("recipient")){
			this.recipient = jsonObject.getLong("recipient");
		}
		this.action = jsonObject.getString("action");
		if(jsonObject.has("payload")){
			this.payload = jsonObject.getJSONObject("payload");
		}

		this.way = way;
	}

	public String getRequestId(){
		return requestId;
	}

	public long getRecipient(){
		return recipient;
	}

	public long getSender(){
		return sender;
	}

	public String getAction(){
		return action;
	}

	public JSONObject getPayload(){
		return payload;
	}

	// SECONDARY


	public IO getWay(){
		return way;
	}

	public JSONObject asJSON(){
		return new JSONObject()
			.put("requestId", requestId)
			.put("requestMode", "RESPONSE")
			.put("recipient", recipient)
			.put("sender", sender)
			.put("action", action)
			.put("payload", payload);
	}

	public static class Builder{

		private final JSONObject jsonObject = new JSONObject();

		public Builder requestId(String id){
			jsonObject.put("requestId", id);
			return this;
		}

		public Builder recipient(long id){
			jsonObject.put("recipient", id);
			return this;
		}

		public Builder action(String action){
			jsonObject.put("action", action);
			return this;
		}

		public Builder payload(JSONObject payload){
			jsonObject.put("payload", payload);
			return this;
		}

		public WSResponse build(){
			return new WSResponse(jsonObject, IO.OUT);
		}

	}

}
