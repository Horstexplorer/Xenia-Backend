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
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.SecondaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.utils.twitch.TwitchWrap;
import de.netbeacon.xenia.backend.utils.twitch.TwitchWrapQOL;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TwitchnotificationsRecord;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TwitchNotificationProcessor extends BackgroundServiceScheduler.Task{

    private final TwitchWrap wrap;
    private final Logger logger = LoggerFactory.getLogger(TwitchNotificationProcessor.class);
    private final ConcurrentHashMap<Long, Boolean> streamStateHashMap = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TwitchNotificationProcessor(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor, SecondaryWebsocketProcessor secondaryWebsocketProcessor, TwitchWrap twitchWrap) {
        super(sqlConnectionPool, primaryWebsocketProcessor, secondaryWebsocketProcessor);
        this.wrap = twitchWrap;
    }

    @Override
    void onExecution() {
        try{
            if(running.get()){
                logger.warn("Skipping Stream Refresh - Action Is Still Running");
                return;
            }
            running.set(true);
            // retrieve all stream ids
            try(var con = getSqlConnectionPool().getConnection()) {
                var sqlContext = getSqlConnectionPool().getContext(con);
                Result<TwitchnotificationsRecord> twitchnotificationsRecordResult = sqlContext.selectFrom(Tables.TWITCHNOTIFICATIONS).fetch();
                Set<Long> channelIds = new HashSet<>();
                HashMap<Long, List<TwitchnotificationsRecord>> tempStorage = new HashMap<>();
                for(TwitchnotificationsRecord record : twitchnotificationsRecordResult){
                    if(record.getTwitchnotificationTwitchChannelId() == null){
                        return; // hasn't been init yet
                    }
                    channelIds.add(record.getTwitchnotificationTwitchChannelId());
                    if(!tempStorage.containsKey(record.getTwitchnotificationTwitchChannelId())){
                        tempStorage.put(record.getTwitchnotificationTwitchChannelId(), new ArrayList<>());
                    }
                    tempStorage.get(record.getTwitchnotificationTwitchChannelId()).add(record);
                }
                // remove no longer existing entries
                streamStateHashMap.keySet().stream().filter(k -> !channelIds.contains(k)).collect(Collectors.toList()).forEach(streamStateHashMap::remove);
                // request data for all streams existing
                List<TwitchWrapQOL.StreamResponse> streamResponseList = TwitchWrapQOL.getStreamOf(channelIds, wrap);
                // prepare something
                Set<Long> usedIds = new HashSet<>();
                // check against the cached state
                streamResponseList.forEach(response -> {
                            usedIds.add(response.getChannelId());
                            if((!streamStateHashMap.containsKey(response.getChannelId()) && response.isLive()) || (streamStateHashMap.get(response.getChannelId()) != response.isLive() && response.isLive())){
                                streamStateHashMap.put(response.getChannelId(), response.isLive());
                                // trigger notification
                                triggerNotification(tempStorage.get(response.getChannelId()), response);
                            }else {
                                // reset the state as we dont know for sure
                                streamStateHashMap.put(response.getChannelId(), response.isLive());
                            }
                        });
                // reset streams that are offline
                streamStateHashMap.keySet().stream().filter(k -> !usedIds.contains(k)).forEach(k -> streamStateHashMap.put(k, false));
            }
        }catch (Exception e){
            logger.error("A Critical Error While Checking Twitch Streams: ", e);
        }finally {
            running.set(false);
        }
    }

    private void triggerNotification(List<TwitchnotificationsRecord> temp, TwitchWrapQOL.StreamResponse response){
        for(TwitchnotificationsRecord twitchnotificationsRecord : temp){
            // payload
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", twitchnotificationsRecord.getGuildId())
                    .put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId())
                    .put("data", new JSONObject()
                            .put("channelName", response.getChannelName())
                            .put("streamTitle", response.getStreamTitle())
                            .put("game", response.getGame())
                    );
            // request
            WSRequest wsRequest = new WSRequest.Builder()
                    .action("twitchnotify")
                    .exitOn(WSRequest.ExitOn.INSTANT)
                    .mode(WSRequest.Mode.BROADCAST)
                    .payload(jsonObject)
                    .build();
            // as we dont know where it belongs we broadcast it
            getSecondaryWebsocketProcessor().getWsProcessorCore().process(wsRequest);
        }
    }
}
