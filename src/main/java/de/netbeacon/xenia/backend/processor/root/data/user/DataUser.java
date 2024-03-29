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

package de.netbeacon.xenia.backend.processor.root.data.user;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.UsersRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.json.JSONObject;

import java.time.ZoneOffset;


public class DataUser extends RequestProcessor{

	public DataUser(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("user", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		if(client.getClientType().equals(ClientType.DISCORD)){
			if(((DiscordClient) client).getInternalRole().equalsIgnoreCase("admin")){
				return this;
			}
			if(!context.method().equalsIgnoreCase("get") || client.getClientId() != Long.parseLong(context.pathParam("userId"))){
				throw new ForbiddenResponse();
			}
		}
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long userId = Long.parseLong(ctx.pathParam("userId"));
			Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
			if(usersRecordResult.isEmpty()){
				throw new NotFoundResponse();
			}
			UsersRecord usersRecord = usersRecordResult.get(0);
			// fluffy json
			JSONObject jsonObject = new JSONObject()
				.put("userId", usersRecord.getUserId())
				.put("creationTimestamp", usersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("internalRole", usersRecord.getInternalRole())
				.put("preferredLanguage", usersRecord.getPreferredLanguage())
				.put("trustFactor", usersRecord.getTrustFactor())
				.put("meta", new JSONObject()
					.put("username", usersRecord.getMetaUsername())
					.put("iconUrl", (usersRecord.getMetaIconurl() != null) ? usersRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataUser#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataUser#GET ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void put(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long userId = Long.parseLong(ctx.pathParam("userId"));
			Result<UsersRecord> usersRecordResult = sqlContext.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
			if(usersRecordResult.isEmpty()){
				throw new NotFoundResponse();
			}
			UsersRecord usersRecord = usersRecordResult.get(0);
			// get new data
			JSONObject newData = new JSONObject(ctx.body());
			// update values
			usersRecord.setInternalRole(newData.getString("internalRole"));
			usersRecord.setPreferredLanguage(newData.getString("preferredLanguage"));
			usersRecord.setTrustFactor(newData.getLong("trustFactor"));
			JSONObject meta = newData.getJSONObject("meta");
			usersRecord.setMetaUsername(meta.getString("username"));
			usersRecord.setMetaIconurl(meta.get("iconUrl") != JSONObject.NULL ? meta.getString("iconUrl") : null);
			// update db
			sqlContext.executeUpdate(usersRecord);
			// fluffy json
			JSONObject jsonObject = new JSONObject()
				.put("userId", usersRecord.getUserId())
				.put("creationTimestamp", usersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("internalRole", usersRecord.getInternalRole())
				.put("preferredLanguage", usersRecord.getPreferredLanguage())
				.put("trustFactor", usersRecord.getTrustFactor())
				.put("meta", new JSONObject()
					.put("username", usersRecord.getMetaUsername())
					.put("iconUrl", (usersRecord.getMetaIconurl() != null) ? usersRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "USER").put("action", "UPDATE").put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataUser#PUT ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataUser#PUT ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void post(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long userId = Long.parseLong(ctx.pathParam("userId"));

			Result<UsersRecord> usersRecordResult;
			if(ctx.queryParamMap().containsKey("goc") && Boolean.parseBoolean(ctx.queryParam("goc"))){
				usersRecordResult = sqlContext.transactionResult(transactionConfig -> {
					var withTransaction = DSL.using(transactionConfig);
					Result<UsersRecord> usersRecordResultL = withTransaction.insertInto(Tables.USERS, Tables.USERS.USER_ID).values(userId).onConflict(Tables.USERS.USER_ID).doNothing().returning().fetch();
					if(usersRecordResultL.isEmpty()){ // if there are no records the entry should already exist so we just need to fetch it
						usersRecordResultL = withTransaction.selectFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).fetch();
					}
					return usersRecordResultL;
				});
			}else{
				usersRecordResult = sqlContext.insertInto(Tables.USERS, Tables.USERS.USER_ID).values(userId).returning().fetch();
			}
			if(usersRecordResult.isEmpty()){
				throw new InternalServerErrorResponse();
			}
			UsersRecord usersRecord = usersRecordResult.get(0);
			// fluffy json
			JSONObject jsonObject = new JSONObject()
				.put("userId", usersRecord.getUserId())
				.put("creationTimestamp", usersRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				.put("internalRole", usersRecord.getInternalRole())
				.put("preferredLanguage", usersRecord.getPreferredLanguage())
				.put("trustFactor", usersRecord.getTrustFactor())
				.put("meta", new JSONObject()
					.put("username", usersRecord.getMetaUsername())
					.put("iconUrl", (usersRecord.getMetaIconurl() != null) ? usersRecord.getMetaIconurl() : JSONObject.NULL)
				);
			// respond
			ctx.status(202);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
			// send ws notification
			PrimaryWebsocketProcessor.WsMessage wsMessage = new PrimaryWebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "USER").put("action", "CREATE").put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataUser#POST ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataUser#POST ", e);
			throw new BadRequestResponse();
		}
	}

	@Override
	public void delete(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			long userId = Long.parseLong(ctx.pathParam("userId"));
			int mod = sqlContext.deleteFrom(Tables.USERS).where(Tables.USERS.USER_ID.eq(userId)).execute();
			if(mod == 0){
				throw new NotFoundResponse();
			}
			ctx.status(204);
			// send ws notification
			PrimaryWebsocketProcessor.WsMessage wsMessage = new PrimaryWebsocketProcessor.WsMessage();
			wsMessage.get().put("type", "USER").put("action", "DELETE").put("userId", userId);
			getWebsocketProcessor().broadcast(wsMessage, client);
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing DataUser#DELETE ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing DataUser#DELETE ", e);
			throw new BadRequestResponse();
		}
	}

}
