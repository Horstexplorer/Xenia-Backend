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
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.NotificationRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;

public class DataGuildMiscNotification extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscNotification.class);

    public DataGuildMiscNotification(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("notifications", sqlConnectionPool, websocketProcessor);
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
            if(!ctx.pathParamMap().containsKey("notificationId")){
                Result<NotificationRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATION).where(Tables.NOTIFICATION.GUILD_ID.eq(guildId)).fetch();
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("notifications", jsonArray);
                for(NotificationRecord notificationRecord : notificationRecords){
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
            }else{
                long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
                Result<NotificationRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATION).where(Tables.NOTIFICATION.GUILD_ID.eq(guildId).and(Tables.NOTIFICATION.NOTIFICATION_ID.eq(notificationId))).fetch();
                if(notificationRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                NotificationRecord notificationRecord = notificationRecords.get(0);
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
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscNotification#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
            Result<NotificationRecord> notificationRecords = sqlContext.selectFrom(Tables.NOTIFICATION).where(Tables.NOTIFICATION.GUILD_ID.eq(guildId).and(Tables.NOTIFICATION.NOTIFICATION_ID.eq(notificationId))).fetch();
            if(notificationRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            NotificationRecord notificationRecord = notificationRecords.get(0);
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
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "UPDATE").put("guildId", guildId).put("notificationId", notificationRecord.getNotificationId());
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscNotification#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // create new object
            Result<NotificationRecord> notificationRecords = sqlContext.insertInto(Tables.NOTIFICATION, Tables.NOTIFICATION.GUILD_ID, Tables.NOTIFICATION.CHANNEL_ID, Tables.NOTIFICATION.USER_ID, Tables.NOTIFICATION.NOTIFICATION_TARGET, Tables.NOTIFICATION.NOTIFICATION_MESSAGE).values(guildId, newData.getLong("channelId"), newData.getLong("userId"), Instant.ofEpochMilli(newData.getLong("notificationTarget")).atOffset(ZoneOffset.UTC).toLocalDateTime(), newData.getString("notificationMessage")).returning().fetch();
            if(notificationRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            NotificationRecord notificationRecord = notificationRecords.get(0);
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
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "CREATE").put("guildId", guildId).put("notificationId", notificationRecord.getNotificationId());
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscNotification#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
            int mod = sqlContext.deleteFrom(Tables.NOTIFICATION).where(Tables.NOTIFICATION.GUILD_ID.eq(guildId).and(Tables.NOTIFICATION.NOTIFICATION_ID.eq(notificationId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_NOTIFICATION").put("action", "DELETE").put("guildId", guildId).put("notificationId", notificationId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscNotification#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
