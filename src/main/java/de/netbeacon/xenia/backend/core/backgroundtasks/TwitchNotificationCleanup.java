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

package de.netbeacon.xenia.backend.core.backgroundtasks;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TwitchnotificationsRecord;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class TwitchNotificationCleanup extends BackgroundServiceScheduler.Task{

	private final Logger logger = LoggerFactory.getLogger(TwitchNotificationCleanup.class);

	public TwitchNotificationCleanup(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor){
		super(sqlConnectionPool, primaryWebsocketProcessor, null);
	}

	@Override
	void onExecution(){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			Result<TwitchnotificationsRecord> records = sqlContext.deleteFrom(Tables.TWITCHNOTIFICATIONS).where(Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_TWITCH_CHANNEL_ID.isNull().and(Tables.TWITCHNOTIFICATIONS.CREATION_TIMESTAMP.lessOrEqual(LocalDateTime.now().minusMinutes(10)))).returning().fetch();
			for(TwitchnotificationsRecord record : records){
				WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
				wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "DELETE").put("guildId", record.getGuildId()).put("twitchNotificationId", record.getTwitchnotificationId());
				getPrimaryWebsocketProcessor().broadcast(wsMessage);
			}
		}
		catch(Exception e){
			logger.warn("Failed To Clean Up -Dead- Notifications: ", e);
		}
	}

}
