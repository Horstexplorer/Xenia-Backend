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

package de.netbeacon.xenia.backend.client.objects.imp;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Auth;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.OauthRecord;
import de.netbeacon.xenia.jooq.tables.records.UsersRecord;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jooq.Result;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

public class DiscordClient extends Client{

	private final SQLConnectionPool sqlConnectionPool;
	private LocalDateTime validUntil = LocalDateTime.now().minusHours(1);
	private DCAuth dcAuth;
	private String internalRole = "";

	public static DiscordClient create(long clientId, SQLConnectionPool sqlConnectionPool){
		return new DiscordClient(clientId, sqlConnectionPool);
	}

	private DiscordClient(long clientId, SQLConnectionPool sqlConnectionPool){
		super(clientId, ClientType.DISCORD);
		this.sqlConnectionPool = sqlConnectionPool;
		try(var con = sqlConnectionPool.getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			Result<OauthRecord> oauthRecords = sqlContext.selectFrom(Tables.OAUTH).where(Tables.OAUTH.USER_ID.eq(clientId)).fetch();
			if(oauthRecords.isEmpty()){
				return;
			}
			OauthRecord oauthRecord = oauthRecords.get(0);
			validUntil = oauthRecord.getDiscordInvalidationTime();
			dcAuth = new DCAuth(this, oauthRecord.getLocalAuthSecret());

			Result<UsersRecord> usersRecords = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(clientId)).fetch();
			if(usersRecords.isEmpty()){
				return;
			}

			UsersRecord usersRecord = usersRecords.get(0);
			this.internalRole = usersRecord.getInternalRole();
		}
		catch(Exception ignore){
		}
	}

	public SQLConnectionPool getSqlConnectionPool(){
		return sqlConnectionPool;
	}

	public String getInternalRole(){
		return internalRole;
	}

	@Override
	public boolean verifyAuth(SecuritySettings.AuthType authType, String credentials){
		if(SecuritySettings.AuthType.BEARER.equals(authType)){
			return !LocalDateTime.now().isAfter(validUntil) && dcAuth.verifyToken(credentials);
		}
		return false;
	}

	static class DCAuth implements Auth{

		private final Client client;
		private final String dcAuthSecret;

		public DCAuth(Client client){
			this.client = client;
			byte[] bytes = new byte[64];
			new SecureRandom().nextBytes(bytes);
			this.dcAuthSecret = Base64.getEncoder().encodeToString(bytes);
		}

		public DCAuth(Client client, String localAuthSecret){
			this.client = client;
			this.dcAuthSecret = localAuthSecret;
		}

		public String getLocalAuthSecret(){
			return dcAuthSecret;
		}

		@Override
		public String getToken(){
			return Jwts.builder()
				.setIssuedAt(new Date())
				.setSubject("Auth")
				.setHeaderParam("cid", client.getClientId())
				.setHeaderParam("isDiscordToken", true)
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(dcAuthSecret)))
				.compact();
		}

		@Override
		public boolean verifyToken(String token){
			try{
				Jwts.parserBuilder()
					.setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(dcAuthSecret)))
					.build()
					.parse(token);
				return true;
			}
			catch(JwtException ignore){
			}
			return false;
		}

	}

}
