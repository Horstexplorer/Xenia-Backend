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

package de.netbeacon.xenia.backend.processor.root.data.guild.member;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.MembersRecord;
import de.netbeacon.xenia.jooq.tables.records.MembersRolesRecord;
import de.netbeacon.xenia.jooq.tables.records.VrolesRecord;
import io.javalin.http.*;
import org.jooq.InsertValuesStep3;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.bitAnd;


public class DataGuildMember extends RequestProcessor{

	private static final long DISCORD_USER_PERM_FILTER = 536870913; // interact, guild_set_ov
	private final Logger logger = LoggerFactory.getLogger(DataGuildMember.class);

	public DataGuildMember(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("member", sqlConnectionPool, websocketProcessor);
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
			JSONObject jsonObject = new JSONObject();
			if(!ctx.pathParamMap().containsKey("userId")){
				Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.GUILD_ID.eq(guildId)).fetch();
				JSONArray jsonArray = new JSONArray();
				jsonObject.put("members", jsonArray);
				for(MembersRecord membersRecord : membersRecords){
					Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(membersRecord.getUserId()).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId))).fetch();
					JSONArray roles = new JSONArray();
					for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
						roles.put(membersRolesRecord.getRoleId());
					}
					jsonObject
						.put("guildId", membersRecord.getGuildId())
						.put("userId", membersRecord.getUserId())
						.put("creationTimestamp", membersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
						.put("roles", roles)
						.put("levelPoints", membersRecord.getLevelPoints())
						.put("meta", new JSONObject()
							.put("nickname", membersRecord.getMetaNickname())
							.put("isAdministrator", membersRecord.getMetaIsAdministrator())
							.put("isOwner", membersRecord.getMetaIsOwner())
						);
				}
			}
			else{
				long userId = Long.parseLong(ctx.pathParam("userId"));
				Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
				Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId))).fetch();
				if(membersRecords.isEmpty()){
					throw new NotFoundResponse();
				}
				MembersRecord membersRecord = membersRecords.get(0);
				JSONArray roles = new JSONArray();
				for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
					roles.put(membersRolesRecord.getRoleId());
				}
				jsonObject
					.put("guildId", membersRecord.getGuildId())
					.put("userId", membersRecord.getUserId())
					.put("creationTimestamp", membersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
					.put("roles", roles)
					.put("levelPoints", membersRecord.getLevelPoints())
					.put("meta", new JSONObject()
						.put("nickname", membersRecord.getMetaNickname())
						.put("isAdministrator", membersRecord.getMetaIsAdministrator())
						.put("isOwner", membersRecord.getMetaIsOwner())
					);
			}
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildMember#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMember#GET ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void put(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long userId = Long.parseLong(ctx.pathParam("userId"));
			// fetch
			Result<MembersRecord> membersRecords = sqlContext.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
			if(membersRecords.isEmpty()){
				throw new NotFoundResponse();
			}
			MembersRecord membersRecord = membersRecords.get(0);
			// get new data
			JSONObject newData = new JSONObject(ctx.body());
			// update data
			membersRecord.setLevelPoints(newData.getLong("levelPoints"));

			JSONObject meta = newData.getJSONObject("meta");
			membersRecord.setMetaNickname(meta.getString("nickname"));
			membersRecord.setMetaIsAdministrator(meta.getBoolean("isAdministrator"));
			membersRecord.setMetaIsOwner(meta.getBoolean("isOwner"));
			// update db
			sqlContext.executeUpdate(membersRecord);
			// update roles
			sqlContext.deleteFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId)));
			List<Long> newRoles = new ArrayList<>();
			for(int i = 0; i < newData.getJSONArray("roles").length(); i++){
				newRoles.add(newData.getJSONArray("roles").getLong(i));
			}
			Result<VrolesRecord> rolesRecords = sqlContext.selectFrom(Tables.VROLES).where(Tables.VROLES.VROLE_ID.in(newRoles).and(Tables.VROLES.GUILD_ID.eq(guildId))).fetch();
			InsertValuesStep3<MembersRolesRecord, Long, Long, Long> ivs = sqlContext.insertInto(Tables.MEMBERS_ROLES).columns(Tables.MEMBERS_ROLES.GUILD_ID, Tables.MEMBERS_ROLES.USER_ID, Tables.MEMBERS_ROLES.ROLE_ID);
			JSONArray jsonArray = new JSONArray();
			for(VrolesRecord rolesRecord : rolesRecords){
				ivs.values(guildId, userId, rolesRecord.getVroleId());
				jsonArray.put(rolesRecord.getVroleId());
			}
			ivs.execute();
			// json
			JSONObject jsonObject = new JSONObject()
				.put("guildId", membersRecord.getGuildId())
				.put("userId", membersRecord.getUserId())
				.put("creationTimestamp", membersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("roles", jsonArray)
				.put("levelPoints", membersRecord.getLevelPoints())
				.put("meta", new JSONObject()
					.put("nickname", membersRecord.getMetaNickname())
					.put("isAdministrator", membersRecord.getMetaIsAdministrator())
					.put("isOwner", membersRecord.getMetaIsOwner())
				);
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MEMBER").put("action", "UPDATE").put("guildId", guildId).put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildMember#PUT ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMember#PUT ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void post(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long userId = Long.parseLong(ctx.pathParam("userId"));

			Result<MembersRecord> membersRecords;
			if(ctx.queryParamMap().containsKey("goc") && Boolean.parseBoolean(ctx.queryParam("goc"))){
				membersRecords = sqlContext.transactionResult(transactionConfig -> {
					var withTransaction = DSL.using(transactionConfig);
					Result<MembersRecord> membersRecordsL = withTransaction.insertInto(Tables.MEMBERS, Tables.MEMBERS.USER_ID, Tables.MEMBERS.GUILD_ID).values(userId, guildId).onConflict(Tables.MEMBERS.GUILD_ID, Tables.MEMBERS.USER_ID).doNothing().returning().fetch();
					if(membersRecordsL.isEmpty()){ // if there are no records the entry should already exist so we just need to fetch it
						membersRecordsL = withTransaction.selectFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).fetch();
					}
					return membersRecordsL;
				});
			}else{
				membersRecords = sqlContext.insertInto(Tables.MEMBERS, Tables.MEMBERS.USER_ID, Tables.MEMBERS.GUILD_ID).values(userId, guildId).returning().fetch();
			}
			Result<MembersRolesRecord> membersRolesRecords = sqlContext.selectFrom(Tables.MEMBERS_ROLES).where(Tables.MEMBERS_ROLES.USER_ID.eq(userId).and(Tables.MEMBERS_ROLES.GUILD_ID.eq(guildId))).fetch();
			if(membersRecords.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			MembersRecord membersRecord = membersRecords.get(0);
			// json
			JSONArray jsonArray = new JSONArray();
			for(MembersRolesRecord membersRolesRecord : membersRolesRecords){
				jsonArray.put(membersRolesRecord.getRoleId());
			}
			JSONObject jsonObject = new JSONObject()
				.put("guildId", membersRecord.getGuildId())
				.put("userId", membersRecord.getUserId())
				.put("creationTimestamp", membersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("roles", jsonArray)
				.put("levelPoints", membersRecord.getLevelPoints())
				.put("meta", new JSONObject()
					.put("nickname", membersRecord.getMetaNickname())
					.put("isAdministrator", membersRecord.getMetaIsAdministrator())
					.put("isOwner", membersRecord.getMetaIsOwner())
				);
			// respond
			ctx.status(202);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MEMBER").put("action", "CREATE").put("guildId", guildId).put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildMember#POST ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMember#POST ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void delete(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long guildId = Long.parseLong(ctx.pathParam("guildId"));
			long userId = Long.parseLong(ctx.pathParam("userId"));
			int mod = sqlContext.deleteFrom(Tables.MEMBERS).where(Tables.MEMBERS.USER_ID.eq(userId).and(Tables.MEMBERS.GUILD_ID.eq(guildId))).execute();
			if(mod == 0){
				throw new NotFoundResponse();
			}
			ctx.status(204);
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "GUILD_MEMBER").put("action", "DELETE").put("guildId", guildId).put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataGuildMember#DELETE ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataGuildMember#DELETE ", e);
			throw new BadRequestResponse();
		}
	}

}
