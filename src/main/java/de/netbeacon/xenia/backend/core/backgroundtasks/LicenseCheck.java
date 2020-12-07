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

package de.netbeacon.xenia.backend.core.backgroundtasks;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.GuildsRecord;
import io.javalin.http.BadRequestResponse;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;

public class LicenseCheck extends BackgroundServiceScheduler.Task{

    private final Logger logger = LoggerFactory.getLogger(LicenseCheck.class);

    public LicenseCheck(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super(sqlConnectionPool, websocketProcessor);
    }

    @Override
    void onExecution() {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            List<Long> clearForIDs = new ArrayList<>();
            Result<Record> records = sqlContext.select().from(Tables.GUILDS).join(Tables.LICENSES).on(Tables.GUILDS.LICENSE_ID.eq(Tables.LICENSES.LICENSE_ID)).where(Tables.GUILDS.LICENSE_ID.isNotNull()).fetch();
            for(Record record : records){
                LocalDateTime start = record.get(Tables.LICENSES.LICENSE_ACTIVATION_TIMESTAMP);
                int duration = record.get(Tables.LICENSES.LICENSE_DURATION_DAYS);
                if(LocalDateTime.now().isAfter(start.plusDays(duration))){
                    clearForIDs.add(record.get(Tables.GUILDS.GUILD_ID));
                }
            }
            Result<GuildsRecord> records1 = sqlContext.update(Tables.GUILDS).set(Tables.GUILDS.LICENSE_ID, inline(null, Tables.GUILDS.LICENSE_ID)).where(Tables.GUILDS.GUILD_ID.in(clearForIDs)).returning().fetch();
            for(GuildsRecord guildsRecord : records1){
                // send ws notification
                WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
                broadcastMessage.get().put("type", "GUILD_LICENSE").put("action", "UPDATE").put("guildId", guildsRecord.getGuildId());
                getWebsocketProcessor().broadcast(broadcastMessage);
            }
        }catch (Exception e){
            logger.warn("An Error Occurred Running LicenseCheck ", e);
            throw new BadRequestResponse();
        }
    }
}
