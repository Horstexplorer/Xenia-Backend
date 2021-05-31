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

import java.security.SecureRandom;
import java.util.Base64;

public class WSRequest{

	private static final SecureRandom secureRandom = new SecureRandom();
	public final IO way;
	public final ExitOn exitOn;
	public final long timeout;
	private final String requestId;
	private final Mode requestMode;
	private final String action;
	private long sender;
	private long recipient;
	private JSONObject payload;

	public WSRequest(JSONObject jsonObject, IO way, ExitOn exitOn, long timeout){
		this.requestId = jsonObject.getString("requestId");
		this.requestMode = Mode.valueOf(jsonObject.getString("requestMode"));
		if(jsonObject.has("recipient")){
			this.recipient = jsonObject.getLong("recipient");
		}
		if(jsonObject.has("sender")){
			this.sender = jsonObject.getLong("sender");
		}
		this.action = jsonObject.getString("action").toLowerCase();
		if(jsonObject.has("payload")){
			this.payload = jsonObject.getJSONObject("payload");
		}

		this.way = way;
		this.exitOn = exitOn;
		this.timeout = timeout;
	}

	public String getRequestId(){
		return requestId;
	}

	public Mode getRequestMode(){
		return requestMode;
	}

	public long getSender(){
		return sender;
	}

	// PRIMARY

	public long getRecipient(){
		return recipient;
	}

	public String getAction(){
		return action;
	}

	public JSONObject getPayload(){
		return payload;
	}

	public IO getWay(){
		return way;
	}

	public ExitOn getExitOn(){
		return exitOn;
	}

	public long getTimeout(){
		return timeout;
	}

	// SECONDARY

	public JSONObject asJSON(){
		return new JSONObject()
			.put("requestId", requestId)
			.put("requestMode", requestMode)
			.put("recipient", recipient)
			.put("sender", sender)
			.put("action", action)
			.put("payload", payload);
	}

	public enum Mode{
		UNICAST,
		BROADCAST
	}

	public enum IO{
		IN,
		OUT
	}

	// EXPORT

	public enum ExitOn{
		INSTANT,
		FIRST_RESULT,
		TIMEOUT
	}

	public static class Builder{

		private final JSONObject jsonObject = new JSONObject();
		private ExitOn exitOn = ExitOn.FIRST_RESULT;
		private long timeout = 5000;

		public Builder mode(Mode mode){
			jsonObject.put("requestMode", mode.toString());
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

		public Builder exitOn(ExitOn exitOn){
			this.exitOn = exitOn;
			return this;
		}

		public Builder timeout(long ms){
			this.timeout = ms;
			return this;
		}

		public WSRequest build(){
			return new WSRequest(jsonObject.put("requestId", getRandomId()), IO.OUT, exitOn, timeout);
		}

		private String getRandomId(){
			byte[] bytes = new byte[128];
			secureRandom.nextBytes(bytes);
			return Base64.getEncoder().encodeToString(bytes);
		}

	}

}
