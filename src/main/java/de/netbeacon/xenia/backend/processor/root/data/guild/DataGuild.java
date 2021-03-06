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

package de.netbeacon.xenia.backend.processor.root.data.guild;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.channel.DataGuildChannel;
import de.netbeacon.xenia.backend.processor.root.data.guild.license.DataGuildLicense;
import de.netbeacon.xenia.backend.processor.root.data.guild.member.DataGuildMember;
import de.netbeacon.xenia.backend.processor.root.data.guild.misc.DataGuildMisc;
import de.netbeacon.xenia.backend.processor.root.data.guild.role.DataGuildRole;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.GuildsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.json.JSONObject;

import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuild extends RequestProcessor{

	private static final long DISCORD_USER_PERM_FILTER = 536870913; // interact, guild_set_ov

	public DataGuild(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("guild", sqlConnectionPool, websocketProcessor,
			new DataGuildMember(sqlConnectionPool, websocketProcessor),
			new DataGuildChannel(sqlConnectionPool, websocketProcessor),
			new DataGuildRole(sqlConnectionPool, websocketProcessor),
			new DataGuildLicense(sqlConnectionPool, websocketProcessor),
			new DataGuildMisc(sqlConnectionPool, websocketProcessor)
		);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(client.getClientType().equals(ClientType.DISCORD)){
			if(((DiscordClient) client).getInternalRole().equalsIgnoreCase("admin")){
				return this;
			}
			if(!(context.method().equalsIgnoreCase("get") || context.method().equalsIgnoreCase("put"))){
				throw new ForbiddenResponse();
			}
			long guildId = Long.parseLong(context.pathParam("guildId"));
			try(var con = getSqlConnectionPool().getConnection()){
				var sqlContext = getSqlConnectionPool().getContext(con);

				Result<Record> memberGuildRelations = sqlContext.select()
					.from(Tables.MEMBERS)
					.join(Tables.GUILDS)
					.on(Tables.MEMBERS.GUILD_ID.eq(Tables.GUILDS.GUILD_ID))
					.where(Tables.MEMBERS.GUILD_ID.eq(guildId).and(Tables.MEMBERS.USER_ID.eq(client.getClientId())))
					.fetch();
				if(memberGuildRelations.isEmpty()){
					throw new BadRequestResponse();
				}
				Record memberGuildRelation = memberGuildRelations.get(0);
				if(((((memberGuildRelation.get(Tables.GUILDS.GUILD_SETTINGS).intValue() >> 0) & 1) == 0) && memberGuildRelation.get(Tables.MEMBERS.META_IS_ADMINISTRATOR)) || memberGuildRelation.get(Tables.MEMBERS.META_IS_OWNER)){
					return this;
				}
				Result<Record> vpermRecords = sqlContext.select()
					.from(Tables.MEMBERS_ROLES)
					.join(Tables.VROLES)
					.on(Tables.MEMBERS_ROLES.ROLE_ID.eq(Tables.VROLES.VROLE_ID))
					.where(
						Tables.MEMBERS_ROLES.GUILD_ID.eq(Long.parseLong(context.pathParam("guildId")))
							.and(Tables.MEMBERS_ROLES.USER_ID.eq(client.getClientId()))
							.and(bitAnd(DISCORD_USER_PERM_FILTER, Tables.VROLES.VROLE_ID).eq(DISCORD_USER_PERM_FILTER))
					)
					.fetch();
				if(vpermRecords.isEmpty()){
					throw new ForbiddenResponse();
				}
			}
			catch(HttpResponseException e){
				if(e instanceof InternalServerErrorResponse){
					logger.error("An Error Occurred Processing DataGuild#PRE ", e);
				}
				throw e;
			}
			catch(NullPointerException e){
				throw new BadRequestResponse();
			}
			catch(Exception e){
				logger.warn("An Error Occurred Processing DataGuild#PRE ", e);
				throw new BadRequestResponse();
			}
		}
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
			if(guildsRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			GuildsRecord guildsRecord = guildsRecords.get(0);
			// build json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", guildsRecord.getGuildId())
				.put("creationTimestamp", guildsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("preferredLanguage", guildsRecord.getPreferredLanguage())
				.put("prefix", guildsRecord.getGuildPrefix())
				.put("settings", guildsRecord.getGuildSettings())
				.put("d43z1Mode", guildsRecord.getGuildD43z1Mode())
				.put("meta", new JSONObject()
					.put("name", guildsRecord.getMetaGuildname())
					.put("iconUrl", (guildsRecord.getMetaIconurl() != null) ? guildsRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuild#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuild#GET ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void put(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
			if(guildsRecords.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			GuildsRecord guildsRecord = guildsRecords.get(0);
			// get new data
			JSONObject newData = new JSONObject(ctx.body());
			// update data
			guildsRecord.setPreferredLanguage(newData.getString("preferredLanguage"));
			guildsRecord.setGuildSettings(newData.getInt("settings"));
			guildsRecord.setGuildPrefix(newData.getString("prefix"));
			guildsRecord.setGuildD43z1Mode(newData.getInt("d43z1Mode"));
			JSONObject metaData = newData.getJSONObject("meta");
			guildsRecord.setMetaGuildname(metaData.getString("name"));
			guildsRecord.setMetaIconurl(metaData.get("iconUrl") != JSONObject.NULL ? metaData.getString("iconUrl") : null);
			// update db
			sqlContext.executeUpdate(guildsRecord);
			// build json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", guildsRecord.getGuildId())
				.put("creationTimestamp", guildsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("preferredLanguage", guildsRecord.getPreferredLanguage())
				.put("prefix", guildsRecord.getGuildPrefix())
				.put("settings", guildsRecord.getGuildSettings())
				.put("d43z1Mode", guildsRecord.getGuildD43z1Mode())
				.put("meta", new JSONObject()
					.put("name", guildsRecord.getMetaGuildname())
					.put("iconUrl", (guildsRecord.getMetaIconurl() != null) ? guildsRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD").put("action", "UPDATE").put("guildId", guildId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuild#PUT ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuild#PUT ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void post(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));

			Result<GuildsRecord> guildsRecords;
			AtomicBoolean getOC = new AtomicBoolean(false);
			if(ctx.queryParamMap().containsKey("goc") && Boolean.parseBoolean(ctx.queryParam("goc"))){
				guildsRecords = sqlContext.transactionResult(transactionConfig -> {
					var withTransaction = DSL.using(transactionConfig);
					Result<GuildsRecord> guildsRecordsL = withTransaction.insertInto(Tables.GUILDS, Tables.GUILDS.GUILD_ID).values(guildId).onConflict(Tables.GUILDS.GUILD_ID).doNothing().returning().fetch();
					if(guildsRecordsL.isEmpty()){ // if there are no records the entry should already exist so we just need to fetch it
						guildsRecordsL = withTransaction.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
					}else {
						getOC.set(true);
					}
					return guildsRecordsL;
				});
			}else{
				guildsRecords = sqlContext.insertInto(Tables.GUILDS, Tables.GUILDS.GUILD_ID).values(guildId).returning().fetch();
			}
			if(guildsRecords.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			GuildsRecord guildsRecord = guildsRecords.get(0);
			// build json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", guildsRecord.getGuildId())
				.put("creationTimestamp", guildsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("preferredLanguage", guildsRecord.getPreferredLanguage())
				.put("prefix", guildsRecord.getGuildPrefix())
				.put("settings", guildsRecord.getGuildSettings())
				.put("d43z1Mode", guildsRecord.getGuildD43z1Mode())
				.put("meta", new JSONObject()
					.put("name", guildsRecord.getMetaGuildname())
					.put("iconUrl", (guildsRecord.getMetaIconurl() != null) ? guildsRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(202);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			if(getOC.get()) return;
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD").put("action", "CREATE").put("guildId", guildId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuild#POST ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuild#POST ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void delete(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			int mod = sqlContext.deleteFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).execute();
			if(mod == 0){
				throw new NotFoundResponse();
			}
			ctx.status(204);
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD").put("action", "DELETE").put("guildId", guildId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuild#DELETE ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuild#DELETE ", e);
			throw new BadRequestResponse();
		}
	}

}
