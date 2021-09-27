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

package de.netbeacon.xenia.backend.security;

import de.netbeacon.utils.ratelimiter.RateLimiter;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.tuples.Triplet;
import de.netbeacon.xenia.backend.client.ClientManager;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.websocket.WsContext;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SecurityManager implements IShutdown{

	private final File file;
	private final ClientManager clientManager;

	private final HashSet<String> blockedIPs = new HashSet<>();
	private final ConcurrentHashMap<UUID, ConcurrentHashMap<Client, RateLimiter>> authRateLimiterMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, RateLimiter> noAuthRateLimiterMap = new ConcurrentHashMap<>();

	public SecurityManager(ClientManager clientManager, File data){
		this.clientManager = clientManager;
		this.file = data;
	}


	public Client authorizeConnection(SecuritySettings securitySettings, Context ctx){
		// check if running behind proxy and we should use the sent ip
		String clientIP = ctx.header("X-Real-IP");
		if(clientIP == null || clientIP.isBlank()){
			clientIP = ctx.ip(); // use the default one
		}
		try{
			// check ip block
			if(blockedIPs.contains(clientIP)){
				throw new ForbiddenResponse();
			}
			// check auth if specified
			Client client = null;
			AuthHeaderContent authHeaderContent = AuthHeaderContent.parseHeader(ctx.header("authorization"));
			if(authHeaderContent == null){
				if(!securitySettings.getRequiredAuthType().equals(SecuritySettings.AuthType.OPTIONAL)){
					throw new UnauthorizedResponse();
				}
			}
			else{
				if(!(securitySettings.getRequiredAuthType().equals(authHeaderContent.getGeneric("authType")) || (securitySettings.getRequiredAuthType().equals(SecuritySettings.AuthType.BEARER))) && !securitySettings.getRequiredAuthType().equals(SecuritySettings.AuthType.OPTIONAL)){
					throw new ForbiddenResponse();
				}
				Long id = authHeaderContent.getGeneric("userId");
				if(SecuritySettings.AuthType.BEARER.equals(authHeaderContent.getGeneric("authType"))){
					if(authHeaderContent.getGeneric("isDiscordToken")){
						client = clientManager.getClient(ClientType.DISCORD, id);
					}
					else{
						client = clientManager.getClient(ClientType.INTERNAL, id);
					}
				}
				else{
					client = clientManager.getClient(ClientType.INTERNAL, id);
				}
				if(client == null || !client.verifyAuth(authHeaderContent.getGeneric("authType"), authHeaderContent.getGeneric("userAuth"))){
					throw new UnauthorizedResponse();
				}
				// check rate limit
				UUID secSetUUID = securitySettings.getSecSetUUID();
				if(!authRateLimiterMap.containsKey(secSetUUID)){
					authRateLimiterMap.put(secSetUUID, new ConcurrentHashMap<>());
				}
				ConcurrentHashMap<Client, RateLimiter> rateLimiterEndpointMap = authRateLimiterMap.get(secSetUUID);
				if(!rateLimiterEndpointMap.containsKey(client)){
					Triplet<TimeUnit, Integer, Long> rateLimiterSettings = securitySettings.getRateLimiterSettings(client.getClientType());
					RateLimiter rateLimiter = new RateLimiter(rateLimiterSettings.getValue1(), rateLimiterSettings.getValue2());
					rateLimiter.setMaxUsages(rateLimiterSettings.getValue3());
					rateLimiterEndpointMap.put(client, rateLimiter);
				}
				RateLimiter rateLimiter = rateLimiterEndpointMap.get(client);
				try{
					if(!rateLimiter.takeNice()){
						throw new HttpResponseException(429, "Too Many Requests", new HashMap<>());
					}
				}
				finally{
					ctx.header("Ratelimit-Limit", String.valueOf(rateLimiter.getMaxUsages()));
					ctx.header("Ratelimit-Remaining", String.valueOf(rateLimiter.getRemainingUsages()));
					ctx.header("Ratelimit-Reset", String.valueOf(rateLimiter.getRefillTime()));
				}
			}
			// check client type
			if(!securitySettings.getRequiredClientType().equals(ClientType.ANY) && !securitySettings.getRequiredAuthType().equals(SecuritySettings.AuthType.OPTIONAL) && client != null){
				if(!securitySettings.getRequiredClientType().containsType(client.getClientType())){
					throw new ForbiddenResponse();
				}
			}
			return client;
		}
		catch(HttpResponseException e){
			if(e.getStatus() == 429){
				throw e;
			}
			if(!noAuthRateLimiterMap.containsKey(clientIP)){
				RateLimiter rateLimiter = new RateLimiter(TimeUnit.MINUTES, 5);
				rateLimiter.setMaxUsages(35);
				noAuthRateLimiterMap.put(clientIP, rateLimiter);
			}
			if(!noAuthRateLimiterMap.get(clientIP).takeNice()){
				throw new HttpResponseException(429, "Too Many Requests", new HashMap<>());
			}
			throw e;
		}
	}

	public Client authorizeWsConnection(SecuritySettings securitySettings, WsContext ctx, WebsocketProcessor websocketProcessor){
		// check if running behind proxy and we should use the sent ip
		String clientIP = ctx.header("X-Real-IP");
		if(clientIP == null || clientIP.isBlank()){
			clientIP = ctx.host(); // use the default one
		}
		try{
			// check ip block
			if(blockedIPs.contains(clientIP)){
				throw new ForbiddenResponse(); // not actually a valid status code
			}
			// check auth if specified
			Client client = null;
			AuthHeaderContent authHeaderContent = AuthHeaderContent.parseHeader("Bearer " + (ctx.queryParamMap().containsKey("token") ? ctx.queryParam("token") : ""));
			if(authHeaderContent == null || !SecuritySettings.AuthType.BEARER.equals(authHeaderContent.getGeneric("authType"))){
				throw new ForbiddenResponse(); // not actually a valid status code
			}
			Long id = authHeaderContent.getGeneric("userId");
			client = clientManager.getClient(ClientType.INTERNAL, id);
			if(client == null || !client.verifyAuth(authHeaderContent.getGeneric("authType"), authHeaderContent.getGeneric("userAuth"))){
				throw new UnauthorizedResponse();
			}
			// check client type
			if(!securitySettings.getRequiredClientType().equals(ClientType.ANY) && !securitySettings.getRequiredAuthType().equals(SecuritySettings.AuthType.OPTIONAL)){
				if(!securitySettings.getRequiredClientType().containsType(client.getClientType())){
					throw new ForbiddenResponse();
				}
			}
			ctx.send(websocketProcessor.getConnectedMessage().get().toString()); // send something which can be parsed by the client to check if the auth succeeded
			return client;
		}
		catch(HttpResponseException e){
			if(!noAuthRateLimiterMap.containsKey(clientIP)){
				RateLimiter rateLimiter = new RateLimiter(TimeUnit.MINUTES, 5);
				rateLimiter.setMaxUsages(35);
				noAuthRateLimiterMap.put(clientIP, rateLimiter);
			}
			if(!noAuthRateLimiterMap.get(clientIP).takeNice()){
				closeWS(ctx.session, 3429, "Too Many Requests", true);
			}
			closeWS(ctx.session, 3000 + e.getStatus(), e.getMessage(), false);
			return null;
		}
	}

	private void closeWS(Session session, int code, String status, boolean now){
		try{
			session.close(code, status);
			if(!now){
				TimeUnit.MILLISECONDS.sleep(2500);
			}
			session.disconnect();
		}
		catch(Exception ignore){
		}
	}

	public ConcurrentHashMap<String, RateLimiter> getNoAuthRateLimiterMap(){
		return noAuthRateLimiterMap;
	}

	public ConcurrentHashMap<UUID, ConcurrentHashMap<Client, RateLimiter>> getAuthRateLimiterMap(){
		return authRateLimiterMap;
	}

	public SecurityManager loadFromFile() throws IOException{
		String contentS = new String(Files.readAllBytes(file.toPath()));
		if(contentS.isBlank()){
			return this;
		}
		JSONObject content = new JSONObject(contentS);
		// get blocked ips
		JSONArray bip = content.getJSONArray("blockedIPs");
		for(int i = 0; i < bip.length(); i++){
			blockedIPs.add(bip.getString(i));
		}

		return this;
	}

	public SecurityManager writeToFile() throws IOException{
		try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
			JSONObject jsonObject = new JSONObject().put("blockedIPs", blockedIPs);
			bufferedWriter.write(jsonObject.toString());
			bufferedWriter.newLine();
			bufferedWriter.flush();
		}
		return this;
	}

	@Override
	public void onShutdown() throws Exception{
		writeToFile();
	}

	private static class AuthHeaderContent{

		private static final Pattern WS_SPLIT = Pattern.compile("\\s+");
		private static final Pattern D_SPLIT = Pattern.compile("\\.");
		private final HashMap<String, Object> data = new HashMap<>();

		private AuthHeaderContent(){}

		protected static AuthHeaderContent parseHeader(String content){
			try{
				List<String> parts = new ArrayList<>(Arrays.asList(WS_SPLIT.split(content)));
				switch(parts.get(0).toLowerCase()){
					case "basic":{
						String decoded = new String(Base64.getDecoder().decode(parts.get(1)));
						return new AuthHeaderContent()
							.add("userId", Long.parseLong(decoded.substring(0, decoded.indexOf(":"))))
							.add("userAuth", decoded.substring(decoded.indexOf(":") + 1))
							.add("authType", SecuritySettings.AuthType.BASIC);
					}
					case "bearer":{
						String token = parts.get(1);
						String[] splitToken = D_SPLIT.split(token);
						Header<?> jwtHeader = Jwts.parserBuilder().build().parse(splitToken[0] + "." + splitToken[1] + ".").getHeader();
						return new AuthHeaderContent()
							.add("userId", Long.parseLong(String.valueOf(jwtHeader.get("cid"))))
							.add("userAuth", token)
							.add("authType", SecuritySettings.AuthType.BEARER)
							.add("isDiscordToken", jwtHeader.containsKey("isDiscordToken"));
					}
					default:
						return null;
				}
			}
			catch(Exception e){
				return null;
			}
		}

		public AuthHeaderContent add(String key, Object object){
			data.put(key, object);
			return this;
		}

		public Object getObject(String key){
			return data.get(key);
		}

		public <T> T getGeneric(String key){
			return (T) data.get(key);
		}

	}

}
