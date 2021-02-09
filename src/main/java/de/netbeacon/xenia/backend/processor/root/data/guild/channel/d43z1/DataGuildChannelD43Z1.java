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

package de.netbeacon.xenia.backend.processor.root.data.guild.channel.d43z1;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.D43z1ChannelsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildChannelD43Z1 extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildChannelD43Z1.class);

    public DataGuildChannelD43Z1(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor) {
        super("d43z1", sqlConnectionPool, websocketProcessor);
    }

    private static final long DISCORD_USER_PERM_FILTER = 536870913; // interact, guild_set_ov

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            if(((DiscordClient)client).getInternalRole().equalsIgnoreCase("admin")){
                return this;
            }
            if(!(context.method().equalsIgnoreCase("get") || context.method().equalsIgnoreCase("put"))){
                throw new ForbiddenResponse();
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
                if(((((memberGuildRelation.get(Tables.GUILDS.GUILD_SETTINGS).intValue() >> 0) & 1) == 0) && memberGuildRelation.get(Tables.MEMBERS.META_IS_ADMINISTRATOR)) || memberGuildRelation.get(Tables.MEMBERS.META_IS_OWNER)){
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
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            Result<D43z1ChannelsRecord> d43z1ChannelsRecords = sqlContext.selectFrom(Tables.D43Z1_CHANNELS).where(Tables.D43Z1_CHANNELS.GUILD_ID.eq(guildId).and(Tables.D43Z1_CHANNELS.CHANNEL_ID.eq(channelId))).fetch();
            if(d43z1ChannelsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            D43z1ChannelsRecord d43z1ChannelsRecord = d43z1ChannelsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", d43z1ChannelsRecord.getGuildId())
                    .put("channelId", d43z1ChannelsRecord.getChannelId())
                    .put("contextPoolUUID", d43z1ChannelsRecord.getContextPoolUuid().toString())
                    .put("selfLearning", d43z1ChannelsRecord.getSelfLearning());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannelD43Z1#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelD43Z1#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            JSONObject newData = new JSONObject(ctx.body());
            Result<D43z1ChannelsRecord> d43z1ChannelsRecords = sqlContext.selectFrom(Tables.D43Z1_CHANNELS).where(Tables.D43Z1_CHANNELS.GUILD_ID.eq(guildId).and(Tables.D43Z1_CHANNELS.CHANNEL_ID.eq(channelId))).fetch();
            if(d43z1ChannelsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            D43z1ChannelsRecord d43z1ChannelsRecord = d43z1ChannelsRecords.get(0);
            // update values
            d43z1ChannelsRecord.setContextPoolUuid(UUID.fromString(newData.getString("contextPoolUUID")));
            d43z1ChannelsRecord.setSelfLearning(newData.getBoolean("selfLearning"));
            // update db
            sqlContext.executeUpdate(d43z1ChannelsRecord);

            JSONObject jsonObject = new JSONObject()
                    .put("guildId", d43z1ChannelsRecord.getGuildId())
                    .put("channelId", d43z1ChannelsRecord.getChannelId())
                    .put("contextPoolUUID", d43z1ChannelsRecord.getContextPoolUuid().toString())
                    .put("selfLearning", d43z1ChannelsRecord.getSelfLearning());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_CHANNEL_D43Z1").put("action", "UPDATE").put("guildId", guildId).put("channelId", channelId);
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannelD43Z1#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelD43Z1#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            JSONObject newData = new JSONObject(ctx.body());

            Result<D43z1ChannelsRecord> d43z1ChannelsRecords = sqlContext.insertInto(Tables.D43Z1_CHANNELS, Tables.D43Z1_CHANNELS.GUILD_ID, Tables.D43Z1_CHANNELS.CHANNEL_ID, Tables.D43Z1_CHANNELS.CONTEXT_POOL_UUID)
                    .values(newData.getLong("guildId"), newData.getLong("channelId"), UUID.fromString(newData.getString("selfLearning"))).returning().fetch();
            if(d43z1ChannelsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            D43z1ChannelsRecord d43z1ChannelsRecord = d43z1ChannelsRecords.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", d43z1ChannelsRecord.getGuildId())
                    .put("channelId", d43z1ChannelsRecord.getChannelId())
                    .put("contextPoolUUID", d43z1ChannelsRecord.getContextPoolUuid().toString())
                    .put("selfLearning", d43z1ChannelsRecord.getSelfLearning());
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_CHANNEL_D43Z1").put("action", "CREATE").put("guildId", guildId).put("channelId", channelId);
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannelD43Z1#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelD43Z1#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            int mod = sqlContext.deleteFrom(Tables.D43Z1_CHANNELS).where(Tables.D43Z1_CHANNELS.GUILD_ID.eq(guildId).and(Tables.D43Z1_CHANNELS.CHANNEL_ID.eq(channelId))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_CHANNEL_D43Z1").put("action", "DELETE").put("guildId", guildId).put("channelId", channelId);
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildChannelD43Z1#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannelD43Z1#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
