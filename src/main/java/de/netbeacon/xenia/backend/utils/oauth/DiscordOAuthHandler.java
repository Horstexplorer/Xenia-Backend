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

package de.netbeacon.xenia.backend.utils.oauth;

import de.netbeacon.xenia.jooq.tables.records.OauthRecord;
import okhttp3.*;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;

public class DiscordOAuthHandler{

	private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
	private static DiscordOAuthHandler instance;
	private final OkHttpClient okHttpClient;
	private final long appId;
	private final String appSecret;
	private final String appRedirectUrl;

	private DiscordOAuthHandler(long appId, String appSecret, String appRedirectUrl){
		okHttpClient = new OkHttpClient.Builder().build();
		this.appId = appId;
		this.appSecret = appSecret;
		this.appRedirectUrl = appRedirectUrl;
	}

	public static DiscordOAuthHandler getInstance(){
		return instance;
	}

	public static DiscordOAuthHandler createInstance(long appId, String appSecret, String appRedirectUrl){
		if(instance == null){
			instance = new DiscordOAuthHandler(appId, appSecret, appRedirectUrl);
		}
		return instance;
	}

	public Token retrieve(String code, Scopes scopes){
		try{
			RequestBody requestBody = new FormBody.Builder()
				.add("client_id", String.valueOf(appId))
				.add("client_secret", String.valueOf(appSecret))
				.add("grant_type", "authorization_code")
				.add("code", code)
				.add("redirect_uri", appRedirectUrl)
				.add("scope", scopes.toString())
				.build();
			Request.Builder builder = new Request.Builder()
				.url(TOKEN_URL)
				.header("User-Agent", "Xenia/X Backend")
				.post(requestBody);
			try(Response response = okHttpClient.newCall(builder.build()).execute()){
				if(response.code() != 200){
					throw new DiscordOAuthHandler.Exception(response.code(), response.message());
				}
				return new Token(new JSONObject(response.body().string()));
			}
		}
		catch(DiscordOAuthHandler.Exception e){
			throw e;
		}
		catch(java.lang.Exception e){
			throw new DiscordOAuthHandler.Exception(e);
		}
	}

	public Token renew(Token token){
		try{
			RequestBody requestBody = new FormBody.Builder()
				.add("client_id", String.valueOf(appId))
				.add("client_secret", String.valueOf(appSecret))
				.add("grant_type", "refresh_token")
				.add("refresh_token", token.getRefreshToken())
				.add("redirect_uri", appRedirectUrl)
				.add("scope", token.getScopes())
				.build();
			Request.Builder builder = new Request.Builder()
				.url(TOKEN_URL)
				.header("User-Agent", "Xenia/X Backend")
				.post(requestBody);
			try(Response response = okHttpClient.newCall(builder.build()).execute()){
				if(response.code() != 200){
					throw new DiscordOAuthHandler.Exception(response.code(), response.message());
				}
				return new Token(new JSONObject(response.body().string()));
			}
		}
		catch(DiscordOAuthHandler.Exception e){
			throw e;
		}
		catch(java.lang.Exception e){
			throw new DiscordOAuthHandler.Exception(e);
		}
	}

	public Long getUserID(Token token){
		try{
			Request.Builder builder = new Request.Builder()
				.url("https://discordapp.com/api/users/@me")
				.header("Authorization", "Bearer " + token.getAccessToken())
				.get();
			try(Response response = okHttpClient.newCall(builder.build()).execute()){
				if(response.code() != 200){
					throw new DiscordOAuthHandler.Exception(response.code(), response.message());
				}
				return new JSONObject(response.body().string()).getLong("id");
			}
		}
		catch(DiscordOAuthHandler.Exception e){
			throw e;
		}
		catch(java.lang.Exception e){
			return null;
		}
	}


	public static class Exception extends java.lang.RuntimeException{

		private final int code;

		public Exception(int code, String message){
			super(message);
			this.code = code;
		}

		public Exception(java.lang.Exception e){
			super(e);
			this.code = -1;
		}

		public int getCode(){
			return code;
		}

	}

	public static class Scopes{

		private final List<String> scopes;

		public Scopes(List<String> scopes){
			this.scopes = scopes;
		}

		@Override
		public String toString(){
			StringBuilder stringBuilder = new StringBuilder();
			for(String scope : scopes){
				stringBuilder.append(scope).append(" ");
			}
			return stringBuilder.toString().trim();
		}

	}

	public static class Token{

		private final String accessToken;
		private final LocalDateTime expires;
		private final String refreshToken;
		private final String scope;

		public Token(String accessToken, String tokenType, long expiresIn, String refreshToken, String scope){
			this.accessToken = accessToken;
			this.expires = LocalDateTime.now().plusSeconds(expiresIn);
			this.refreshToken = refreshToken;
			this.scope = scope;
		}

		public Token(JSONObject jsonObject){
			this.accessToken = jsonObject.getString("access_token");
			this.expires = LocalDateTime.now().plusSeconds(jsonObject.getLong("expires_in"));
			this.refreshToken = jsonObject.getString("refresh_token");
			this.scope = jsonObject.getString("scope");
		}

		public Token(OauthRecord dbOauthRecord){
			this.accessToken = dbOauthRecord.getDiscordAccessToken();
			this.refreshToken = dbOauthRecord.getDiscordRefreshToken();
			this.scope = dbOauthRecord.getDiscordScopes();
			this.expires = dbOauthRecord.getDiscordInvalidationTime();
		}

		public String getAccessToken(){
			return accessToken;
		}

		public LocalDateTime expiresOn(){
			return expires;
		}

		public String getRefreshToken(){
			return refreshToken;
		}

		public String getScopes(){
			return scope;
		}

	}

}
