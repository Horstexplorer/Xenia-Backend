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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.polls;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.misc.polls.vote.DataGuildMiscPollsVotes;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.PollsEntriesRecord;
import de.netbeacon.xenia.joop.tables.records.PollsOptionsRecord;
import de.netbeacon.xenia.joop.tables.records.PollsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;

public class DataGuildMiscPolls extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscPolls.class);

    public DataGuildMiscPolls(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("polls", sqlConnectionPool, websocketProcessor, new DataGuildMiscPollsVotes(sqlConnectionPool, websocketProcessor));
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("pollId")){
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("polls", jsonArray);
                Result<PollsRecord> pollsRecords = sqlContext.selectFrom(Tables.POLLS).where(Tables.POLLS.GUILD_ID.eq(guildId).and(Tables.POLLS.POLL_IS_ACTIVE)).fetch();
                for(PollsRecord pollsRecord : pollsRecords){
                    Result<PollsOptionsRecord> optionsRecords = sqlContext.selectFrom(Tables.POLLS_OPTIONS).where(Tables.POLLS_OPTIONS.POLL_ID.eq(pollsRecord.getPollId())).fetch();
                    Result<PollsEntriesRecord> entriesRecords = sqlContext.selectFrom(Tables.POLLS_ENTRIES).where(Tables.POLLS_ENTRIES.POLL_ID.eq(pollsRecord.getPollId())).fetch();
                    JSONArray options = new JSONArray();
                    for(PollsOptionsRecord pollsOptionsRecord : optionsRecords){
                        int id = pollsOptionsRecord.getPollOptionId();
                        int count = 0;
                        for(PollsEntriesRecord pollsEntriesRecord : entriesRecords){
                            if(id == pollsEntriesRecord.getPollOptionId()){
                                count++;
                            }
                        }
                        options.put(new JSONObject().put("optionId", id).put("count", count).put("description", pollsOptionsRecord.getPollOptionDescription()));
                    }
                    jsonArray.put(new JSONObject()
                            .put("pollId", pollsRecord.getPollId())
                            .put("creationTimestamp", pollsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .put("guildId", pollsRecord.getGuildId())
                            .put("channelId", pollsRecord.getChannelId())
                            .put("userId", pollsRecord.getUserId())
                            .put("closeTimestamp", pollsRecord.getPollCloseTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .put("options", options));
                }
            }else{
                long pollId = Long.parseLong(ctx.pathParam("pollId"));
                Result<PollsRecord> pollsRecords = sqlContext.selectFrom(Tables.POLLS).where(Tables.POLLS.GUILD_ID.eq(guildId).and(Tables.POLLS.POLL_ID.eq(pollId).and(Tables.POLLS.POLL_IS_ACTIVE))).fetch();
                if(pollsRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                PollsRecord pollsRecord = pollsRecords.get(0);
                Result<PollsOptionsRecord> optionsRecords = sqlContext.selectFrom(Tables.POLLS_OPTIONS).where(Tables.POLLS_OPTIONS.POLL_ID.eq(pollId)).fetch();
                Result<PollsEntriesRecord> entriesRecords = sqlContext.selectFrom(Tables.POLLS_ENTRIES).where(Tables.POLLS_ENTRIES.POLL_ID.eq(pollId)).fetch();
                JSONArray options = new JSONArray();
                for(PollsOptionsRecord pollsOptionsRecord : optionsRecords){
                    int id = pollsOptionsRecord.getPollOptionId();
                    int count = 0;
                    for(PollsEntriesRecord pollsEntriesRecord : entriesRecords){
                        if(id == pollsEntriesRecord.getPollOptionId()){
                            count++;
                        }
                    }
                    options.put(new JSONObject().put("optionId", id).put("count", count).put("description", pollsOptionsRecord.getPollOptionDescription()));
                }
                jsonObject
                        .put("pollId", pollsRecord.getPollId())
                        .put("creationTimestamp", pollsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .put("guildId", pollsRecord.getGuildId())
                        .put("channelId", pollsRecord.getChannelId())
                        .put("userId", pollsRecord.getUserId())
                        .put("closeTimestamp", pollsRecord.getPollCloseTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .put("options", options);
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscPolls#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscPolls#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // get data
            JSONObject newData = new JSONObject(ctx.body());
            // create new poll
            Result<PollsRecord> pollsRecords = sqlContext.insertInto(Tables.POLLS, Tables.POLLS.GUILD_ID, Tables.POLLS.CHANNEL_ID, Tables.POLLS.USER_ID, Tables.POLLS.POLL_CLOSE_TIMESTAMP)
                    .values(guildId, newData.getLong("channelId"), newData.getLong("userID"), Instant.ofEpochMilli(newData.getLong("closeTimestamp")).atOffset(ZoneOffset.UTC).toLocalDateTime())
                    .returning().fetch();
            if(pollsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            PollsRecord pollsRecord = pollsRecords.get(0);
            // create new options
            JSONArray newOptions = newData.getJSONArray("options");
            for(int i = 0; i < newOptions.length(); i++){
                JSONObject option = newOptions.getJSONObject(i);
                int mod = sqlContext.insertInto(Tables.POLLS_OPTIONS, Tables.POLLS_OPTIONS.POLL_ID, Tables.POLLS_OPTIONS.POLL_OPTION_ID, Tables.POLLS_OPTIONS.POLL_OPTION_DESCRIPTION)
                        .values(pollsRecord.getPollId(), option.getInt("optionId"), option.getString("description")).execute();
                if(mod == 0){
                    sqlContext.executeDelete(pollsRecord);
                    throw new BadRequestResponse();
                }
            }
            // json
            JSONObject jsonObject = new JSONObject();
            Result<PollsOptionsRecord> optionsRecords = sqlContext.selectFrom(Tables.POLLS_OPTIONS).where(Tables.POLLS_OPTIONS.POLL_ID.eq(pollsRecord.getPollId())).fetch();
            Result<PollsEntriesRecord> entriesRecords = sqlContext.selectFrom(Tables.POLLS_ENTRIES).where(Tables.POLLS_ENTRIES.POLL_ID.eq(pollsRecord.getPollId())).fetch();
            JSONArray options = new JSONArray();
            for(PollsOptionsRecord pollsOptionsRecord : optionsRecords){
                int id = pollsOptionsRecord.getPollOptionId();
                int count = 0;
                for(PollsEntriesRecord pollsEntriesRecord : entriesRecords){
                    if(id == pollsEntriesRecord.getPollOptionId()){
                        count++;
                    }
                }
                options.put(new JSONObject().put("optionId", id).put("count", count).put("description", pollsOptionsRecord.getPollOptionDescription()));
            }
            jsonObject
                    .put("pollId", pollsRecord.getPollId())
                    .put("creationTimestamp", pollsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("guildId", pollsRecord.getGuildId())
                    .put("channelId", pollsRecord.getChannelId())
                    .put("userId", pollsRecord.getUserId())
                    .put("closeTimestamp", pollsRecord.getPollCloseTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("options", options);
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_TAG").put("action", "CREATE").put("guildId", guildId).put("pollId", pollsRecord.getPollId());
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscPolls#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscPolls#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long pollId = Long.parseLong(ctx.pathParam("pollId"));
            int mod = sqlContext.update(Tables.POLLS).set(Tables.POLLS.POLL_IS_ACTIVE, false).where(Tables.POLLS.GUILD_ID.eq(guildId).and(Tables.POLLS.POLL_ID.eq(pollId).and(Tables.POLLS.POLL_IS_ACTIVE))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            // respond
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_POLL").put("action", "DELETE").put("guildId", guildId).put("pollId", pollId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscPolls#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscPolls#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
