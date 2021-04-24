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

package de.netbeacon.xenia.backend.utils.twitch.auth;

import de.netbeacon.xenia.backend.utils.twitch.TwitchWrap;
import de.netbeacon.xenia.backend.utils.twitch.exceptions.AuthException;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents an bearer token
 */
public class ApplicationBearerToken{

	private static final int MAX_RETRIES = 10;

	private final String clientID;
	private final String clientSecret;
	private String bearerToken;

	private final Lock lock = new ReentrantLock();
	private final TwitchWrap twitchWrap;

	private final Logger logger = LoggerFactory.getLogger(ApplicationBearerToken.class);


	/**
	 * Creates a new instance of this class
	 *
	 * @param twitchWrap   TwitchWrap
	 * @param clientID     the client ID
	 * @param clientSecret the client Secret
	 * @param bearerToken  an already created bearer
	 */
	public ApplicationBearerToken(TwitchWrap twitchWrap, String clientID, String clientSecret, String bearerToken){
		this.twitchWrap = twitchWrap;
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.bearerToken = bearerToken;
		if(this.bearerToken == null || this.bearerToken.isEmpty()){
			request();
		}
		else{
			update();
		}
	}

	/**
	 * Used to request a new bearer token
	 */
	public void request(){
		boolean recieved = false;
		for(int i = 0; i < MAX_RETRIES; i++){
			try{
				logger.debug("Requesting New Bearer Token");
				Request request = new Request.Builder()
					.post(RequestBody.create(new byte[]{}, null))
					.url("https://id.twitch.tv/oauth2/token?client_id=" + clientID + "&client_secret=" + clientSecret + "&grant_type=client_credentials")
					.build();
				try(Response response = twitchWrap.getOKHTTPClient().newCall(request).execute()){
					int code = response.code();
					if(code != 200){
						throw new Exception("Unexpected Response Code: " + code);
					}
					try(ResponseBody responseBody = response.body()){
						if(responseBody == null){
							throw new Exception("Unexpected Response: No Data Received");
						}
						JSONObject jsonObject = new JSONObject(responseBody.string());
						bearerToken = jsonObject.getString("access_token");
						recieved = true;
						break;
					}
				}
			}
			catch(Exception e){
				logger.warn("An Error Occurred While Trying To Revoke The Bearer Token");
			}
			try{
				TimeUnit.MILLISECONDS.sleep(new Random().nextInt(300) + 100);
			}
			catch(Exception ignore){
			}
		}
		if(!recieved){
			logger.error("Failed To Request Bearer Token");
			throw new AuthException("Failed To Request Bearer Token");
		}
		logger.debug("Successfully Requested Bearer Token");
	}

	/**
	 * Used to revoke this bearer token
	 */
	public void revoke(){
		boolean revoked = false;
		for(int i = 0; i < MAX_RETRIES; i++){
			try{
				logger.debug("Revoking Bearer Token");
				Request request = new Request.Builder()
					.post(RequestBody.create(new byte[]{}, null))
					.url("https://id.twitch.tv/oauth2/revoke?client_id=" + clientID + "&token=" + bearerToken)
					.build();
				try(Response response = twitchWrap.getOKHTTPClient().newCall(request).execute()){
					int code = response.code();
					if(code != 200 && code != 400){ // we dont care if it failed to invalidate (then it is invalid already)
						throw new Exception("Unexpected Response Code: " + code);
					}
					revoked = true;
					break;
				}
			}
			catch(Exception e){
				logger.warn("An Error Occurred While Trying To Revoke The Bearer Token");
			}
			try{
				TimeUnit.MILLISECONDS.sleep(new Random().nextInt(300) + 100);
			}
			catch(Exception ignore){
			}
		}
		if(!revoked){
			logger.error("Failed To Revoke Bearer Token");
			throw new AuthException("Failed To Revoke Bearer Token");
		}
		logger.debug("Successfully Revoked Bearer Token");
	}

	/**
	 * Can be used to update the bearer
	 */
	public void update(){
		try{
			lock.lock();
			if(isValid()){ // check if it is invalid
				logger.debug("Updating The Bearer Is Not Needed");
				return;
			}
			try{
				revoke();
			}
			catch(Exception ignore){
			} // make sure the token is invalidated
			request(); // request a new token
		}
		catch(Exception e){
			logger.warn("Failed To Update The Bearer Token");
			throw new AuthException("Failed To Update The Bearer Token");
		}
		finally{
			lock.unlock();
		}
	}

	/**
	 * Can be used to check if the bearer is valid
	 *
	 * @return token is valid
	 */
	public boolean isValid(){
		for(int i = 0; i < MAX_RETRIES; i++){
			try{
				logger.debug("Verifying Bearer Token");
				Request request = new Request.Builder()
					.get()
					.url("https://id.twitch.tv/oauth2/validate")
					.addHeader("Authorization", "OAuth " + bearerToken)
					.build();
				try(Response response = twitchWrap.getOKHTTPClient().newCall(request).execute()){
					int code = response.code();
					if(code == 200){ // token is valid
						return true;
					}
					else if(code == 401){ // token is invalid
						return false;
					}
					else{ // we dont know
						throw new Exception("Unexpected Response Code: " + code);
					}
				}
			}
			catch(Exception e){
				logger.warn("An Error Occurred While Trying To Verify The Bearer Token: " + e.getMessage());
			}
			// wait some time before next retry
			try{
				TimeUnit.MILLISECONDS.sleep(new Random().nextInt(300) + 100);
			}
			catch(Exception ignore){
			}
		}
		logger.error("Failed To Verify Bearer Token");
		throw new AuthException("Failed To Verify Bearer Token");
	}

	/**
	 * Returns the bearer token
	 *
	 * @return String
	 */
	public String getBearerToken(){
		return bearerToken;
	}

	/**
	 * Returns the client id
	 *
	 * @return String
	 */
	public String getClientID(){
		return clientID;
	}

}
