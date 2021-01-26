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
import de.netbeacon.xenia.jooq.tables.records.MessagesRecord;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class MessageCleanup extends BackgroundServiceScheduler.Task{

    private final Logger logger = LoggerFactory.getLogger(MessageCleanup.class);

    public MessageCleanup(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor) {
        super(sqlConnectionPool, primaryWebsocketProcessor, null);
    }

    @Override
    void onExecution() {
        try(var con = getSqlConnectionPool().getConnection()) {
            var sqlContext = getSqlConnectionPool().getContext(con);
            Result<MessagesRecord> messagesRecords = sqlContext.deleteFrom(Tables.MESSAGES).where(Tables.MESSAGES.CREATION_TIMESTAMP_DISCORD.lessThan(LocalDateTime.now().minusDays(60))).returning().fetch();
            for(MessagesRecord record : messagesRecords){
                WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
                wsMessage.get().put("type", "GUILD_MESSAGE").put("action", "DELETE").put("guildId", record.getGuildId()).put("channelId", record.getChannelId()).put("messageId",record.getMessageId());
                getPrimaryWebsocketProcessor().broadcast(wsMessage);
            }
        }catch (Exception e){
            logger.warn("Failed To Clean Up Old Messages",e);
        }
    }
}
