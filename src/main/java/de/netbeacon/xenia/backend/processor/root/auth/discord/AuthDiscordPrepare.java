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
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import io.javalin.http.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthDiscordPrepare extends RequestProcessor{

	private final Logger logger = LoggerFactory.getLogger(AuthDiscordPrepare.class);

	public AuthDiscordPrepare(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("prepare", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(!context.method().equalsIgnoreCase("get")){
			throw new ForbiddenResponse();
		}
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			// fetch owner
			String owner = null;
			if(ctx.queryParamMap().containsKey("ownerId")){
				owner = ctx.queryParam("ownerId");
			}
			StringBuilder scopesB = new StringBuilder();
			if(ctx.queryParamMap().containsKey("scopes")){
				ctx.queryParamMap().get("scopes")
					.forEach(scope -> scopesB.append(scope).append("%20"));
			}
			String scopes = ((scopes = scopesB.toString()).isBlank()) ? "identify" : scopes;
			// generate random state
			String randomState = RandomStringUtils.randomAlphanumeric(32);
			// insert
			sqlContext.insertInto(Tables.OAUTH_STATES, Tables.OAUTH_STATES.STATE_OWNER, Tables.OAUTH_STATES.STATE)
				.values(owner, randomState)
				.execute();
			// redirect
			ctx.redirect("https://discord.com/api/oauth2/authorize?client_id=509065864763408385&redirect_uri=https%3A%2F%2Fxenia.netbeacon.de%2Fauth%2Freturning&response_type=code&scope=" + scopes + "&state=" + randomState);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing AuthDiscordPrepare#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing AuthDiscordPrepare#GET ", e);
			throw new BadRequestResponse();
		}
	}

}
