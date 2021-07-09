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

package de.netbeacon.xenia.backend.processor.root.auth.discord;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.backend.utils.oauth.DiscordOAuthHandler;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.OauthRecord;
import io.javalin.http.*;
import org.jooq.Result;

import java.time.LocalDateTime;

public class AuthDiscordRenew extends RequestProcessor{

	public AuthDiscordRenew(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("renew", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(!client.getClientType().equals(ClientType.DISCORD) || !context.method().equalsIgnoreCase("get")){
			throw new ForbiddenResponse();
		}
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			Result<OauthRecord> result = sqlContext.selectFrom(Tables.OAUTH).where(Tables.OAUTH.USER_ID.eq(client.getClientId())).fetch();
			if(result.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			OauthRecord oauthRecord = result.get(0);
			// check if we can renew the token
			if(oauthRecord.getDiscordInvalidationTime().minusMinutes(90).isAfter(LocalDateTime.now())){
				// the token is still valid for some time
				ctx.status(200);
				return;
			}
			// get new token
			DiscordOAuthHandler.Token newToken;
			try{
				newToken = DiscordOAuthHandler.getInstance().renew(new DiscordOAuthHandler.Token(oauthRecord));
			}
			catch(Exception e){
				throw new BadRequestResponse();
			}
			// update record
			oauthRecord.setDiscordAccessToken(newToken.getAccessToken());
			oauthRecord.setDiscordRefreshToken(newToken.getRefreshToken());
			oauthRecord.setDiscordInvalidationTime(newToken.expiresOn());
			oauthRecord.setDiscordScopes(newToken.getScopes());
			// update db
			sqlContext.executeUpdate(oauthRecord);
			// send ok
			ctx.status(204);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing AuthDiscordRenew#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing AuthDiscordRenew#GET ", e);
			throw new BadRequestResponse();
		}
	}

}
