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

package de.netbeacon.xenia.backend.processor.root.data.guild.channel;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.channel.message.DataGuildChannelMessage;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.ChannelsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildChannel extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildChannel.class);

    public DataGuildChannel(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("channel", sqlConnectionPool, websocketProcessor, new DataGuildChannelMessage(sqlConnectionPool, websocketProcessor));
    }

    private static final long DISCORD_USER_PERM_FILTER = 335544321; // interact, web_use, guild_channel_ov

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            if(((DiscordClient)client).getInternalRole().equalsIgnoreCase("admin")){
                return this;
            }
            if(!(context.method().equalsIgnoreCase("get") || context.method().equalsIgnoreCase("put"))){
                throw new ForbiddenResponse();
            }
            try(var con = getSqlConnectionPool().getConnection()) {
                var sqlContext = getSqlConnectionPool().getContext(con);
                Result<Record> records = sqlContext.select()
                        .from(Tables.MEMBERS_ROLES)
                        .join(Tables.VROLES)
                        .on(Tables.MEMBERS_ROLES.ROLE_ID.eq(Tables.VROLES.VROLE_ID))
                        .where(
                                Tables.MEMBERS_ROLES.GUILD_ID.eq(Long.parseLong(context.pathParam("guildId")))
                                        .and(Tables.MEMBERS_ROLES.USER_ID.eq(client.getClientId()))
                                        .and(bitAnd(DISCORD_USER_PERM_FILTER, Tables.VROLES.VROLE_ID).eq(DISCORD_USER_PERM_FILTER))
                        )
                        .fetch();
                if(records.isEmpty()){
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
            if(!ctx.pathParamMap().containsKey("channelId")){
                Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.GUILD_ID.eq(guildId)).fetch();
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("channels", jsonArray);
                for(ChannelsRecord channelsRecord : channelsRecords){
                    jsonArray.put(new JSONObject()
                            .put("guildId", channelsRecord.getGuildId())
                            .put("channelId", channelsRecord.getChannelId())
                            .put("creationTimestamp", channelsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .put("accessRestriction", channelsRecord.getAccessRestriction())
                            .put("channelMode", channelsRecord.getChannelMode())
                            .put("channelType", channelsRecord.getChannelType())
                            .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive())
                            .put("tmpLoggingChannelId", channelsRecord.getTmpLoggingChannelId()));
                }
            }else{
                long channelId = Long.parseLong(ctx.pathParam("channelId"));
                Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId).and(Tables.CHANNELS.GUILD_ID.eq(guildId))).fetch();
                if(channelsRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                ChannelsRecord channelsRecord = channelsRecords.get(0);
                jsonObject
                        .put("guildId", channelsRecord.getGuildId())
                        .put("channelId", channelsRecord.getChannelId())
                        .put("creationTimestamp", channelsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .put("accessRestriction", channelsRecord.getAccessRestriction())
                        .put("channelMode", channelsRecord.getChannelMode())
                        .put("channelType", channelsRecord.getChannelType())
                        .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive())
                        .put("tmpLoggingChannelId", channelsRecord.getTmpLoggingChannelId());
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
            logger.warn("An Error Occurred Processing DataGuildChannel#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            // fetch
            Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId).and(Tables.CHANNELS.GUILD_ID.eq(guildId))).fetch();
            if(channelsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            ChannelsRecord channelsRecord = channelsRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update data
            channelsRecord.setAccessRestriction(newData.getBoolean("accessRestriction"));
            channelsRecord.setChannelMode(newData.getString("channelMode"));
            channelsRecord.setChannelType(newData.getString("channelType"));
            channelsRecord.setTmpLoggingActive(newData.getBoolean("tmpLoggingActive"));
            // update with db
            sqlContext.executeUpdate(channelsRecord);
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", channelsRecord.getGuildId())
                    .put("channelId", channelsRecord.getChannelId())
                    .put("creationTimestamp", channelsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("accessRestriction", channelsRecord.getAccessRestriction())
                    .put("channelMode", channelsRecord.getChannelMode())
                    .put("channelType", channelsRecord.getChannelType())
                    .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive())
                    .put("tmpLoggingChannelId", channelsRecord.getTmpLoggingChannelId());
            // result
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_CHANNEL").put("action", "UPDATE").put("guildId", guildId).put("channelId", channelId);
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
            logger.warn("An Error Occurred Processing DataGuildChannel#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            // insert
            Result<ChannelsRecord> channelsRecords = sqlContext.insertInto(Tables.CHANNELS, Tables.CHANNELS.CHANNEL_ID, Tables.CHANNELS.GUILD_ID).values(channelId, guildId).returning().fetch();
            if(channelsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            ChannelsRecord channelsRecord = channelsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", channelsRecord.getGuildId())
                    .put("channelId", channelsRecord.getChannelId())
                    .put("creationTimestamp", channelsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("accessRestriction", channelsRecord.getAccessRestriction())
                    .put("channelMode", channelsRecord.getChannelMode())
                    .put("channelType", channelsRecord.getChannelType())
                    .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive())
                    .put("tmpLoggingChannelId", channelsRecord.getTmpLoggingChannelId());
            // result
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_CHANNEL").put("action", "CREATE").put("guildId", guildId).put("channelId", channelId);
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
            logger.warn("An Error Occurred Processing DataGuildChannel#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            int mod = sqlContext.deleteFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId).and(Tables.CHANNELS.GUILD_ID.eq(guildId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_CHANNEL").put("action", "DELETE").put("guildId", guildId).put("channelId", channelId);
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
            logger.warn("An Error Occurred Processing DataGuildChannel#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
