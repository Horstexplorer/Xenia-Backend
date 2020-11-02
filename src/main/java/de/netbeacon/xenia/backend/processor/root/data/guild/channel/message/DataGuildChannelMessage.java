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

package de.netbeacon.xenia.backend.processor.root.data.guild.channel.message;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.MessagesRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;

public class DataGuildChannelMessage extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildChannelMessage.class);

    public DataGuildChannelMessage(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("message", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("messageId")){
                JSONArray messages = new JSONArray();
                jsonObject.put("messages", messages);
                Result<MessagesRecord> messagesRecords;
                if(ctx.queryParam("limit") == null){
                    messagesRecords = sqlContext.selectFrom(Tables.MESSAGES).where(Tables.MESSAGES.GUILD_ID.eq(guildId).and(Tables.MESSAGES.CHANNEL_ID.eq(channelId))).fetch();
                }else{
                    messagesRecords = sqlContext.selectFrom(Tables.MESSAGES).where(Tables.MESSAGES.GUILD_ID.eq(guildId).and(Tables.MESSAGES.CHANNEL_ID.eq(channelId))).orderBy(Tables.MESSAGES.CREATION_TIMESTAMP_DISCORD.desc()).limit(Integer.parseInt(ctx.queryParam("limit"))).fetch();
                }
                for(MessagesRecord messagesRecord : messagesRecords){
                    messages.put(new JSONObject()
                            .put("guildId", messagesRecord.getGuildId())
                            .put("channelId", messagesRecord.getChannelId())
                            .put("messageId", messagesRecord.getMessageId())
                            .put("userId", messagesRecord.getUserId())
                            .put("creationTimestamp", messagesRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                            .put("creationTimestampDiscord", messagesRecord.getCreationTimestampDiscord().toEpochSecond(ZoneOffset.UTC))
                            .put("messageSalt", messagesRecord.getMessageSalt())
                            .put("messageContent", messagesRecord.getMessageContent())
                    );
                }
            }else{
                long messageId = Long.parseLong(ctx.pathParam("messageId"));
                Result<MessagesRecord> messagesRecords = sqlContext.selectFrom(Tables.MESSAGES).where(Tables.MESSAGES.GUILD_ID.eq(guildId).and(Tables.MESSAGES.CHANNEL_ID.eq(channelId).and(Tables.MESSAGES.MESSAGE_ID.eq(messageId)))).fetch();
                if(messagesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                MessagesRecord messagesRecord = messagesRecords.get(0);
                jsonObject
                        .put("guildId", messagesRecord.getGuildId())
                        .put("channelId", messagesRecord.getChannelId())
                        .put("messageId", messagesRecord.getMessageId())
                        .put("userId", messagesRecord.getUserId())
                        .put("creationTimestamp", messagesRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                        .put("creationTimestampDiscord", messagesRecord.getCreationTimestampDiscord().toEpochSecond(ZoneOffset.UTC))
                        .put("messageSalt", messagesRecord.getMessageSalt())
                        .put("messageContent", messagesRecord.getMessageContent());
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannel#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelMessage#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            long messageId = Long.parseLong(ctx.pathParam("messageId"));
            Result<MessagesRecord> messagesRecords = sqlContext.selectFrom(Tables.MESSAGES).where(Tables.MESSAGES.GUILD_ID.eq(guildId).and(Tables.MESSAGES.CHANNEL_ID.eq(channelId).and(Tables.MESSAGES.MESSAGE_ID.eq(messageId)))).fetch();
            if(messagesRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            MessagesRecord messagesRecord = messagesRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update values
            messagesRecord.setMessageSalt(newData.getString("messageSalt"));
            messagesRecord.setMessageContent(newData.getString("messageContent"));
            // update db
            sqlContext.executeUpdate(messagesRecord);

            JSONObject jsonObject = new JSONObject()
                    .put("guildId", messagesRecord.getGuildId())
                    .put("channelId", messagesRecord.getChannelId())
                    .put("messageId", messagesRecord.getMessageId())
                    .put("userId", messagesRecord.getUserId())
                    .put("creationTimestamp", messagesRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("creationTimestampDiscord", messagesRecord.getCreationTimestampDiscord().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("messageSalt", messagesRecord.getMessageSalt())
                    .put("messageContent", messagesRecord.getMessageContent());
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MESSAGE").put("action", "UPDATE").put("guildId", guildId).put("channelId", channelId).put("messageId", messageId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannel#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelMessage#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            long messageId = Long.parseLong(ctx.pathParam("messageId"));
            // get data & quick check it
            JSONObject newData = new JSONObject(ctx.body());
            if(newData.getLong("guildId") != guildId || newData.getLong("channelId") != channelId || newData.getLong("messageId") != messageId){
                throw new BadRequestResponse();
            }
            // insert
            Result<MessagesRecord> messagesRecords = sqlContext.insertInto(Tables.MESSAGES, Tables.MESSAGES.GUILD_ID, Tables.MESSAGES.CHANNEL_ID, Tables.MESSAGES.MESSAGE_ID, Tables.MESSAGES.USER_ID, Tables.MESSAGES.CREATION_TIMESTAMP_DISCORD, Tables.MESSAGES.MESSAGE_SALT, Tables.MESSAGES.MESSAGE_CONTENT).values(guildId, channelId, messageId, newData.getLong("userId"), Instant.ofEpochMilli(newData.getLong("creationTimestampDiscord")).atOffset(ZoneOffset.UTC).toLocalDateTime(), newData.getString("messageSalt"), newData.getString("messageContent")).returning().fetch();
            if(messagesRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            MessagesRecord messagesRecord = messagesRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", messagesRecord.getGuildId())
                    .put("channelId", messagesRecord.getChannelId())
                    .put("messageId", messagesRecord.getMessageId())
                    .put("userId", messagesRecord.getUserId())
                    .put("creationTimestamp", messagesRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("creationTimestampDiscord", messagesRecord.getCreationTimestampDiscord().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("messageSalt", messagesRecord.getMessageSalt())
                    .put("messageContent", messagesRecord.getMessageContent());
            // return
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MESSAGE").put("action", "CREATE").put("guildId", guildId).put("channelId", channelId).put("messageId", messageId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannel#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelMessage#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            long messageId = Long.parseLong(ctx.pathParam("messageId"));
            int mod = sqlContext.deleteFrom(Tables.MESSAGES).where(Tables.MESSAGES.GUILD_ID.eq(guildId).and(Tables.MESSAGES.CHANNEL_ID.eq(channelId).and(Tables.MESSAGES.MESSAGE_ID.eq(messageId)))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MESSAGE").put("action", "DELETE").put("guildId", guildId).put("channelId", channelId).put("messageId", messageId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannel#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelMessage#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
