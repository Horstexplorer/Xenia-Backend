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

package de.netbeacon.xenia.backend.processor.root.data.guild.channel.automod;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.ChannelAutoModRecord;
import de.netbeacon.xenia.jooq.tables.records.ChannelsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONObject;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildChannelAutoMod extends RequestProcessor{

	private static final long DISCORD_USER_PERM_FILTER = 134217729; // interact, guild_channel_ov

	public DataGuildChannelAutoMod(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("automod", sqlConnectionPool, websocketProcessor);
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
			long channelId = Long.parseLong(ctx.pathParam("channelId"));
			// fetch channel
			Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.GUILD_ID.eq(guildId).and(Tables.CHANNELS.CHANNEL_ID.eq(channelId))).fetch();
			if(channelsRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			// fetch record
			Result<ChannelAutoModRecord> channelAutoModRecords = sqlContext.selectFrom(Tables.CHANNEL_AUTO_MOD).where(Tables.CHANNEL_AUTO_MOD.CHANNEL_ID.eq(channelId)).fetch();
			if(channelAutoModRecords.isEmpty()){
				channelAutoModRecords = sqlContext.insertInto(Tables.CHANNEL_AUTO_MOD, Tables.CHANNEL_AUTO_MOD.CHANNEL_ID).values(channelId).returning().fetch();
			}
			if(channelAutoModRecords.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			ChannelAutoModRecord channelAutoModRecord = channelAutoModRecords.get(0);
			// json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", guildId)
				.put("channelId", channelAutoModRecord.getChannelId())
				.put("filterContentWords", channelAutoModRecord.getFilterContentWords())
				.put("filterContentURLs", channelAutoModRecord.getFilterContentUrls())
				.put("filterBehaviourSpam", channelAutoModRecord.getFilterBehaviourSpam())
				.put("filterBehaviourRaid", channelAutoModRecord.getFilterBehaviourRaid());
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());

		}catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildChannelAutoMod#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildChannelAutoMod#GET ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void put(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long channelId = Long.parseLong(ctx.pathParam("channelId"));
			// fetch channel
			Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.GUILD_ID.eq(guildId).and(Tables.CHANNELS.CHANNEL_ID.eq(channelId))).fetch();
			if(channelsRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			// fetch record
			Result<ChannelAutoModRecord> channelAutoModRecords = sqlContext.selectFrom(Tables.CHANNEL_AUTO_MOD).where(Tables.CHANNEL_AUTO_MOD.CHANNEL_ID.eq(channelId)).fetch();
			if(channelAutoModRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			ChannelAutoModRecord channelAutoModRecord = channelAutoModRecords.get(0);
			// update
			JSONObject newData = new JSONObject(ctx.body());

			channelAutoModRecord.setFilterContentWords(newData.getInt("filterContentWords"));
			channelAutoModRecord.setFilterContentUrls(newData.getInt("filterContentURLs"));
			channelAutoModRecord.setFilterBehaviourSpam(newData.getInt("filterBehaviourSpam"));
			channelAutoModRecord.setFilterBehaviourRaid(newData.getInt("filterBehaviourRaid"));

			sqlContext.executeUpdate(channelAutoModRecord);
			// json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", guildId)
				.put("channelId", channelAutoModRecord.getChannelId())
				.put("filterContentWords", channelAutoModRecord.getFilterContentWords())
				.put("filterContentURLs", channelAutoModRecord.getFilterContentUrls())
				.put("filterBehaviourSpam", channelAutoModRecord.getFilterBehaviourSpam())
				.put("filterBehaviourRaid", channelAutoModRecord.getFilterBehaviourRaid());
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_CHANNEL_AUTO_MOD").put("action", "UPDATE").put("guildId", guildId).put("channelId", channelId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildChannelAutoMod#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildChannelAutoMod#PUT ", e);
			throw new BadRequestResponse();
		}
	}

}
