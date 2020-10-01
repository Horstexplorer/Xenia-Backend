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
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.ChannelsRecord;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

public class DataGuildChannel extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildChannel.class);

    public DataGuildChannel(SQLConnectionPool sqlConnectionPool) {
        super("channel", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            // fetch
            Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch();
            if(channelsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            ChannelsRecord channelsRecord = channelsRecords.get(0);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("channelId", channelsRecord.getChannelId())
                    .put("guildId", channelsRecord.getGuildId())
                    .put("creationTimestamp", channelsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("accessRestricted", channelsRecord.getAccessRestriction())
                    .put("channelType", channelsRecord.getChannelType())
                    .put("channelMode", channelsRecord.getChannelMode())
                    .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive());
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannel#Delete", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            // fetch
            Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch();
            if(channelsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            ChannelsRecord channelsRecord = channelsRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            if(newData.getLong("channelId") != channelId){
                throw new BadRequestResponse("Invalid Data");
            }
            // update values
            channelsRecord.setAccessRestriction(newData.getBoolean("accessRestricted"));
            channelsRecord.setChannelType(newData.getString("channelType"));
            channelsRecord.setChannelMode(newData.getString("channelMode"));
            channelsRecord.setTmpLoggingActive(newData.getBoolean("tmpLoggingActive"));
            // update db
            sqlContext.executeUpdate(channelsRecord);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("channelId", channelsRecord.getChannelId())
                    .put("guildId", channelsRecord.getGuildId())
                    .put("creationTimestamp", channelsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("accessRestricted", channelsRecord.getAccessRestriction())
                    .put("channelType", channelsRecord.getChannelType())
                    .put("channelMode", channelsRecord.getChannelMode())
                    .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive());
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannel#Put", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // insert
            sqlContext.insertInto(Tables.CHANNELS, Tables.CHANNELS.CHANNEL_ID, Tables.CHANNELS.GUILD_ID).values(channelId, guildId).execute();
            // fetch
            Result<ChannelsRecord> channelsRecords = sqlContext.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch();
            if(channelsRecords.isEmpty()){
                logger.error("An Error Occurred Processing DataGuildChannel#Post: Fetch Returned No Result After Creating It");
                throw new NotFoundResponse();
            }
            ChannelsRecord channelsRecord = channelsRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            if(newData.getLong("channelId") != channelId){
                throw new BadRequestResponse("Invalid Data");
            }
            // update values
            channelsRecord.setAccessRestriction(newData.getBoolean("accessRestricted"));
            channelsRecord.setChannelType(newData.getString("channelType"));
            channelsRecord.setChannelMode(newData.getString("channelMode"));
            channelsRecord.setTmpLoggingActive(newData.getBoolean("tmpLoggingActive"));
            // update db
            sqlContext.executeUpdate(channelsRecord);
            // build fluffy json
            JSONObject jsonObject = new JSONObject()
                    .put("channelId", channelsRecord.getChannelId())
                    .put("guildId", channelsRecord.getGuildId())
                    .put("creationTimestamp", channelsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("accessRestricted", channelsRecord.getAccessRestriction())
                    .put("channelType", channelsRecord.getChannelType())
                    .put("channelMode", channelsRecord.getChannelMode())
                    .put("tmpLoggingActive", channelsRecord.getTmpLoggingActive());
            // return response
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannel#Post", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            int mod = sqlContext.deleteFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildChannel#Delete", e);
            throw new BadRequestResponse();
        }
    }
}
