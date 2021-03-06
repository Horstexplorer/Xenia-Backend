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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.notification;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.NotificationsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildMiscNotification extends RequestProcessor{

	private static final long DISCORD_USER_PERM_FILTER = 33; // interact, notif_ov

	public DataGuildMiscNotification(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("notifications", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(client.getClientType().equals(ClientType.DISCORD)){
			if(((DiscordClient) client).getInternalRole().equalsIgnoreCase("admin")){
				return this;
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
			JSONObject jsonObject = new JSONObject();
			if(!ctx.pathParamMap().containsKey("notificationId")){
				Result<NotificationsRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATIONS).where(Tables.NOTIFICATIONS.GUILD_ID.eq(guildId)).fetch();
				JSONArray jsonArray = new JSONArray();
				jsonObject.put("notifications", jsonArray);
				for(NotificationsRecord notificationRecord : notificationRecords){
					jsonArray.put(new JSONObject()
						.put("notificationId", notificationRecord.getNotificationId())
						.put("creationTimestamp", notificationRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
						.put("guildId", notificationRecord.getGuildId())
						.put("channelId", notificationRecord.getChannelId())
						.put("userId", notificationRecord.getUserId())
						.put("notificationTarget", notificationRecord.getNotificationTarget().toInstant(ZoneOffset.UTC).toEpochMilli())
						.put("notificationMessage", notificationRecord.getNotificationMessage())
					);
				}
			}
			else{
				long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
				Result<NotificationsRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATIONS).where(Tables.NOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.NOTIFICATIONS.NOTIFICATION_ID.eq(notificationId))).fetch();
				if(notificationRecords.isEmpty()){
					throw new NotFoundResponse();
				}
				NotificationsRecord notificationRecord = notificationRecords.get(0);
				jsonObject
					.put("notificationId", notificationRecord.getNotificationId())
					.put("creationTimestamp", notificationRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
					.put("guildId", notificationRecord.getGuildId())
					.put("channelId", notificationRecord.getChannelId())
					.put("userId", notificationRecord.getUserId())
					.put("notificationTarget", notificationRecord.getNotificationTarget().toInstant(ZoneOffset.UTC).toEpochMilli())
					.put("notificationMessage", notificationRecord.getNotificationMessage());
			}
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMiscNotification#GET ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void put(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
			Result<NotificationsRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATIONS).where(Tables.NOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.NOTIFICATIONS.NOTIFICATION_ID.eq(notificationId))).fetch();
			if(notificationRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			NotificationsRecord notificationRecord = notificationRecords.get(0);
			// get new data
			JSONObject newData = new JSONObject(ctx.body());
			// update values
			notificationRecord.setNotificationTarget(Instant.ofEpochMilli(newData.getLong("notificationTarget")).atOffset(ZoneOffset.UTC).toLocalDateTime());
			notificationRecord.setNotificationMessage(newData.getString("notificationMessage"));
			// update db
			sqlContext.executeUpdate(notificationRecord);

			JSONObject jsonObject = new JSONObject()
				.put("notificationId", notificationRecord.getNotificationId())
				.put("creationTimestamp", notificationRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("guildId", notificationRecord.getGuildId())
				.put("channelId", notificationRecord.getChannelId())
				.put("userId", notificationRecord.getUserId())
				.put("notificationTarget", notificationRecord.getNotificationTarget().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("notificationMessage", notificationRecord.getNotificationMessage());
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "UPDATE").put("guildId", guildId).put("notificationId", notificationRecord.getNotificationId());
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMiscNotification#PUT ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void post(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			// get new data
			JSONObject newData = new JSONObject(ctx.body());
			// create new object
			Result<NotificationsRecord> notificationRecords = sqlContext.insertInto(Tables.NOTIFICATIONS, Tables.NOTIFICATIONS.GUILD_ID, Tables.NOTIFICATIONS.CHANNEL_ID, Tables.NOTIFICATIONS.USER_ID, Tables.NOTIFICATIONS.NOTIFICATION_TARGET, Tables.NOTIFICATIONS.NOTIFICATION_MESSAGE).values(guildId, newData.getLong("channelId"), newData.getLong("userId"), Instant.ofEpochMilli(newData.getLong("notificationTarget")).atOffset(ZoneOffset.UTC).toLocalDateTime(), newData.getString("notificationMessage")).returning().fetch();
			if(notificationRecords.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			NotificationsRecord notificationRecord = notificationRecords.get(0);
			JSONObject jsonObject = new JSONObject()
				.put("notificationId", notificationRecord.getNotificationId())
				.put("creationTimestamp", notificationRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("guildId", notificationRecord.getGuildId())
				.put("channelId", notificationRecord.getChannelId())
				.put("userId", notificationRecord.getUserId())
				.put("notificationTarget", notificationRecord.getNotificationTarget().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("notificationMessage", notificationRecord.getNotificationMessage());
			// respond
			ctx.status(202);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "CREATE").put("guildId", guildId).put("notificationId", notificationRecord.getNotificationId());
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMiscNotification#POST ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void delete(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
			int mod = sqlContext.deleteFrom(Tables.NOTIFICATIONS).where(Tables.NOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.NOTIFICATIONS.NOTIFICATION_ID.eq(notificationId))).execute();
			if(mod == 0){
				throw new NotFoundResponse();
			}
			ctx.status(204);
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "DELETE").put("guildId", guildId).put("notificationId", notificationId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMiscNotification#DELETE ", e);
			throw new BadRequestResponse();
		}
	}

}
