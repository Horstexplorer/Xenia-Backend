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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.twitchnotifications;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TwitchnotificationsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildMiscTwitchNotifications extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscTwitchNotifications.class);

    public DataGuildMiscTwitchNotifications(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("twitchnotifications", sqlConnectionPool, websocketProcessor);
    }

    private static final long DISCORD_USER_PERM_FILTER = 513; // interact, twitch_manage

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            if(((DiscordClient)client).getInternalRole().equalsIgnoreCase("admin")){
                return this;
            }
            long guildId = Long.parseLong(context.pathParam("guildId"));
            try(var con = getSqlConnectionPool().getConnection()) {
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
                if((!memberGuildRelation.get(Tables.GUILDS.USE_VPERMS) && memberGuildRelation.get(Tables.MEMBERS.META_IS_ADMINISTRATOR)) || memberGuildRelation.get(Tables.MEMBERS.META_IS_OWNER)){
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
            }catch (HttpResponseException e){
                if(e instanceof InternalServerErrorResponse){
                    logger.error("An Error Occurred Processing DataGuild#PRE ", e);
                }
                throw e;
            }catch (NullPointerException e){
                throw new BadRequestResponse();
            }catch (Exception e){
                logger.warn("An Error Occurred Processing DataGuild#PRE ", e);
                throw new BadRequestResponse();
            }
        }
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("notificationId")){
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("twitchNotifications", jsonArray);
                Result<TwitchnotificationsRecord> twitchRecords = sqlContext.selectFrom(Tables.TWITCHNOTIFICATIONS).where(Tables.TWITCHNOTIFICATIONS.GUILD_ID.eq(guildId)).fetch();
                for(TwitchnotificationsRecord twitchRecord : twitchRecords){
                    jsonArray.put(new JSONObject()
                            .put("twitchNotificationId", twitchRecord.getTwitchnotificationId())
                            .put("creationTimestamp", twitchRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .put("guildId", twitchRecord.getGuildId())
                            .put("channelId", twitchRecord.getChannelId())
                            .put("twitchChannelId", twitchRecord.getTwitchnotificationTwitchChannelId())
                            .put("twitchChannelName", twitchRecord.getTwitchnotificationTwitchChannelName() != null ? twitchRecord.getTwitchnotificationTwitchChannelName() : JSONObject.NULL)
                            .put("notificationMessage", twitchRecord.getTwitchnotificationCustomMessage())
                    );
                }
            }else{
                long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
                Result<TwitchnotificationsRecord> twitchnotificationsRecords = sqlContext.selectFrom(Tables.TWITCHNOTIFICATIONS.where(Tables.TWITCHNOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID.eq(notificationId)))).fetch();
                if(twitchnotificationsRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                TwitchnotificationsRecord twitchnotificationsRecord = twitchnotificationsRecords.get(0);
                jsonObject
                        .put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId())
                        .put("creationTimestamp", twitchnotificationsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .put("guildId", twitchnotificationsRecord.getGuildId())
                        .put("channelId", twitchnotificationsRecord.getChannelId())
                        .put("twitchChannelId", twitchnotificationsRecord.getTwitchnotificationTwitchChannelId())
                        .put("twitchChannelName", twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() != null ? twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() : JSONObject.NULL)
                        .put("notificationMessage", twitchnotificationsRecord.getTwitchnotificationCustomMessage());
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTwitchNotification#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTwitchNotification#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
            // get data
            JSONObject newData = new JSONObject(ctx.body());
            Result<TwitchnotificationsRecord> twitchnotificationsRecords = sqlContext.selectFrom(Tables.TWITCHNOTIFICATIONS.where(Tables.TWITCHNOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID.eq(notificationId)))).fetch();
            if(twitchnotificationsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            TwitchnotificationsRecord twitchnotificationsRecord = twitchnotificationsRecords.get(0);
            // update values
            twitchnotificationsRecord.setTwitchnotificationCustomMessage(newData.getString("notificationMessage"));
            // update db
            sqlContext.executeUpdate(twitchnotificationsRecord);
            // return values
            JSONObject jsonObject = new JSONObject()
                    .put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId())
                    .put("creationTimestamp", twitchnotificationsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("guildId", twitchnotificationsRecord.getGuildId())
                    .put("channelId", twitchnotificationsRecord.getChannelId())
                    .put("twitchChannelId", twitchnotificationsRecord.getTwitchnotificationTwitchChannelId())
                    .put("twitchChannelName", twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() != null ? twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() : JSONObject.NULL)
                    .put("notificationMessage", twitchnotificationsRecord.getTwitchnotificationCustomMessage());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "UPDATE").put("guildId", guildId).put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId());
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTwitchNotification#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTwitchNotification#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            //long notificationId = Long.parseLong(ctx.pathParam("notificationId")); set by the backend
            // get data
            JSONObject initData = new JSONObject(ctx.body());
            // insert
            Result<TwitchnotificationsRecord> twitchnotificationsRecords = sqlContext
                    .insertInto(Tables.TWITCHNOTIFICATIONS, Tables.TWITCHNOTIFICATIONS.GUILD_ID, Tables.TWITCHNOTIFICATIONS.CHANNEL_ID, Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_TWITCH_CHANNEL_NAME, Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_CUSTOM_MESSAGE)
                    .values(guildId, initData.getLong("channelId"), initData.getString("twitchNotificationId"), initData.getString("notificationMessage"))
                    .returning()
                    .fetch();
            if(twitchnotificationsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            TwitchnotificationsRecord twitchnotificationsRecord = twitchnotificationsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId())
                    .put("creationTimestamp", twitchnotificationsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("guildId", twitchnotificationsRecord.getGuildId())
                    .put("channelId", twitchnotificationsRecord.getChannelId())
                    .put("twitchChannelId", twitchnotificationsRecord.getTwitchnotificationTwitchChannelId())
                    .put("twitchChannelName", twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() != null ? twitchnotificationsRecord.getTwitchnotificationTwitchChannelName() : JSONObject.NULL)
                    .put("notificationMessage", twitchnotificationsRecord.getTwitchnotificationCustomMessage());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "CREATE").put("guildId", guildId).put("twitchNotificationId", twitchnotificationsRecord.getTwitchnotificationId());
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTwitchNotification#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTwitchNotification#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long notificationId = Long.parseLong(ctx.pathParam("notificationId"));
            int mod = sqlContext.deleteFrom(Tables.TWITCHNOTIFICATIONS).where(Tables.TWITCHNOTIFICATIONS.GUILD_ID.eq(guildId).and(Tables.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID.eq(notificationId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TWITCHNOTIFICATION").put("action", "DELETE").put("guildId", guildId).put("twitchNotificationId", notificationId);
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTwitchNotification#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
