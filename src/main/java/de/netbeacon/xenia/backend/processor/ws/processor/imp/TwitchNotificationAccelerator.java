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

package de.netbeacon.xenia.backend.processor.ws.processor.imp;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.processor.ws.processor.WSResponse;
import de.netbeacon.xenia.backend.utils.twitch.TwitchWrap;
import de.netbeacon.xenia.backend.utils.twitch.TwitchWrapQOL;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TwitchnotificationsRecord;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchNotificationAccelerator extends WSProcessor{

	private final TwitchWrap twitchWrap;
	private final PrimaryWebsocketProcessor primaryWebsocketProcessor;
	private final SQLConnectionPool sqlConnectionPool;
	private final Logger logger = LoggerFactory.getLogger(TwitchNotificationAccelerator.class);

	public TwitchNotificationAccelerator(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor, TwitchWrap twitchWrap){
		super("twitchaccelerator");
		this.sqlConnectionPool = sqlConnectionPool;
		this.twitchWrap = twitchWrap;
		this.primaryWebsocketProcessor = primaryWebsocketProcessor;
	}

	@Override
	public WSResponse process(WSRequest wsRequest){
		try{
			JSONObject payload = wsRequest.getPayload();
			long guildId = payload.getLong("guildId");
			long twitchNotificationId = payload.getLong("twitchNotificationId");
			// force an update of the record
			try(var con = sqlConnectionPool.getConnection()){
				var sqlContext = sqlConnectionPool.getContext(con);
				Result<TwitchnotificationsRecord> twitchnotificationsRecordResult = sqlContext.selectFrom(Tables.TWITCHNOTIFICATIONS).where(Tables.TWITCHNOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID.eq(twitchNotificationId))).fetch();
				if(twitchnotificationsRecordResult.isEmpty()){
					logger.warn("Requested Update For Notification (" + guildId + ") " + twitchNotificationId + " Which Does Not Exist");
					return null;
				}
				TwitchnotificationsRecord twitchnotificationsRecord = twitchnotificationsRecordResult.get(0);
				// fetch name
				try{
					TwitchWrapQOL.UserResponse userResponse = TwitchWrapQOL.getUserOf(twitchnotificationsRecord.getTwitchnotificationTwitchChannelName(), twitchWrap);
					if(userResponse.getUserID() == -1){
						throw new RuntimeException("Something Went Wrong");
					}
					// update and send notification
					twitchnotificationsRecord.setTwitchnotificationTwitchChannelId(userResponse.getUserID());
					sqlContext.executeUpdate(twitchnotificationsRecord);
					WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
					wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "UPDATE").put("guildId", guildId).put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId());
					primaryWebsocketProcessor.broadcast(wsMessage);
				}
				catch(Exception e){
					// delete and send notification
					sqlContext.executeDelete(twitchnotificationsRecord);
					WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
					wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "DELETE").put("guildId", guildId).put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId());
					primaryWebsocketProcessor.broadcast(wsMessage);
				}
			}
		}
		catch(Exception ignore){
		}
		return null;
	}

}
