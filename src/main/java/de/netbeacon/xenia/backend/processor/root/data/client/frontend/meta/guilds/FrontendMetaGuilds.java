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

package de.netbeacon.xenia.backend.processor.root.data.client.frontend.meta.guilds;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FrontendMetaGuilds extends RequestProcessor{

	private final Logger logger = LoggerFactory.getLogger(FrontendMetaGuilds.class);

	public FrontendMetaGuilds(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("meta_guilds", sqlConnectionPool, websocketProcessor);
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
			// select all guilds the user is able to interact with
			Result<Record> records;
			Map<Long, Long> permMerge = new HashMap<>();
			boolean adminOverride = false;
			if(((DiscordClient) client).getInternalRole().equalsIgnoreCase("admin")){
				// get all guilds. max perms
				records = sqlContext.select().from(Tables.GUILDS).fetch();
				adminOverride = true;
			}
			else{
				// get all guild where we are a member
				records = sqlContext.select().from(Tables.GUILDS)
					.join(Tables.MEMBERS).on(Tables.GUILDS.GUILD_ID.eq(Tables.MEMBERS.GUILD_ID))
					.where(Tables.MEMBERS.USER_ID.eq(client.getClientId())).fetch();
				// vperms where we find em
				Result<Record2<Long, Long>> vPermsRecords = sqlContext.select(Tables.VROLES.GUILD_ID, Tables.VROLES.VROLE_PERMISSION).from(Tables.VROLES)
					.join(Tables.MEMBERS_ROLES).on(Tables.VROLES.GUILD_ID.eq(Tables.MEMBERS_ROLES.GUILD_ID))
					.where(Tables.MEMBERS_ROLES.USER_ID.eq(client.getClientId())).fetch();
				for(Record2<Long, Long> record33 : vPermsRecords){
					if(!permMerge.containsKey(record33.value1())){
						permMerge.put(record33.value1(), record33.value2());
					}
					else{
						permMerge.put(record33.value1(), permMerge.get(record33.value2()) | record33.value2());
					}
				}
			}

			JSONArray jsonArray = new JSONArray();

			for(Record record : records){
				jsonArray.put(new JSONObject()
					.put("guildId", String.valueOf(record.get(Tables.GUILDS.GUILD_ID)))
					.put("guildName", record.get(Tables.GUILDS.META_GUILDNAME))
					.put("iconUrl", (record.field(Tables.GUILDS.META_ICONURL) != null) ? record.get(Tables.GUILDS.META_ICONURL) : JSONObject.NULL)
					.put("member", new JSONObject()
						.put("isAdmin", ((record.field(Tables.MEMBERS.META_IS_ADMINISTRATOR) != null) ? (boolean) record.get(Tables.MEMBERS.META_IS_ADMINISTRATOR) : (adminOverride) ? true : false))
						.put("isOwner", ((record.field(Tables.MEMBERS.META_IS_OWNER) != null) ? (boolean) record.get(Tables.MEMBERS.META_IS_OWNER) : (adminOverride) ? true : false))
						.put("userPermValue", String.valueOf((permMerge.getOrDefault(record.get(Tables.GUILDS.GUILD_ID), (adminOverride) ? Long.MAX_VALUE : 0L))))
					)
				);
			}

			JSONObject jsonObject = new JSONObject().put("guilds", jsonArray);
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing FrontendMetaGuilds#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing FrontendMetaGuilds#GET ", e);
			throw new BadRequestResponse();
		}
	}

}
