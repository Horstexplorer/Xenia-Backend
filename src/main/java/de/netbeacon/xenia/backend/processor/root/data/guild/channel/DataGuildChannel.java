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
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZoneOffset;

public class DataGuildChannel extends RequestProcessor {

    public DataGuildChannel(SQLConnectionPool sqlConnectionPool) {
        super("channel", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            String channelIdS = ctx.pathParam("channelId");
            long channelId = Long.parseLong(channelIdS);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            JSONObject jsonObject = new JSONObject();
            if(channelIdS.isBlank()){
                // get a list of all channels for this guild
                Result<ChannelsRecord> channelsRecord = context.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.GUILD_ID.eq(guildId)).fetch();
                JSONArray jsonArray = new JSONArray();
                for(ChannelsRecord channelRecord : channelsRecord){
                    jsonArray.put(new JSONObject()
                            .put("channelID", channelRecord.getChannelId())
                            .put("creationTimestamp", channelRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                            .put("channelType", channelRecord.getChannelType())
                            .put("channelMode", channelRecord.getChannelMode())
                            .put("accessRestriction", channelRecord.getAccessRestriction())
                            .put("tmpLoggingActive", channelRecord.getTmpLoggingActive()));

                }
                jsonObject.put("channels", jsonArray);
            }else{
                // just get the selected channel
                ChannelsRecord channelRecord = context.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch().get(0);
                // build json
                jsonObject
                        .put("channelID", channelRecord.getChannelId())
                        .put("creationTimestamp", channelRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                        .put("channelType", channelRecord.getChannelType())
                        .put("channelMode", channelRecord.getChannelMode())
                        .put("accessRestriction", channelRecord.getAccessRestriction())
                        .put("tmpLoggingActive", channelRecord.getTmpLoggingActive());
            }
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (Exception e){
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            // fetch
            ChannelsRecord channelRecord = context.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch().get(0);
            // update
            JSONObject newData = new JSONObject(ctx.body());
            channelRecord.setChannelType(newData.getString("channelType"));
            channelRecord.setChannelMode(newData.getString("channelMode"));
            channelRecord.setAccessRestriction(newData.getBoolean("accessRestriction"));
            channelRecord.setTmpLoggingActive(newData.getBoolean("tmpLoggingActive"));
            context.executeUpdate(channelRecord);
            // build json
            JSONObject jsonObject = new JSONObject()
                    .put("channelID", channelRecord.getChannelId())
                    .put("creationTimestamp", channelRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("channelType", channelRecord.getChannelType())
                    .put("channelMode", channelRecord.getChannelMode())
                    .put("accessRestriction", channelRecord.getAccessRestriction())
                    .put("tmpLoggingActive", channelRecord.getTmpLoggingActive());
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (Exception e){
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            context.insertInto(Tables.CHANNELS, Tables.CHANNELS.CHANNEL_ID, Tables.CHANNELS.GUILD_ID).values(channelId, guildId).execute();
            // fetch
            ChannelsRecord channelRecord = context.selectFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).fetch().get(0);
            // build json
            JSONObject jsonObject = new JSONObject()
                    .put("channelID", channelRecord.getChannelId())
                    .put("creationTimestamp", channelRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("channelType", channelRecord.getChannelType())
                    .put("channelMode", channelRecord.getChannelMode())
                    .put("accessRestriction", channelRecord.getAccessRestriction())
                    .put("tmpLoggingActive", channelRecord.getTmpLoggingActive());
            // return
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (Exception e){
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long channelId = Long.parseLong(ctx.pathParam("channelId"));
            context.deleteFrom(Tables.CHANNELS).where(Tables.CHANNELS.CHANNEL_ID.eq(channelId)).execute();
            ctx.status(200);
        }catch (Exception e){
            throw new BadRequestResponse();
        }
    }
}
