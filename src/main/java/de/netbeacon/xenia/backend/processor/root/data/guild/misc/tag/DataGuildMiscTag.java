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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.tag;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.TagsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

public class DataGuildMiscTag extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscTag.class);

    public DataGuildMiscTag(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("tags", sqlConnectionPool, websocketProcessor);
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
            if(!ctx.pathParamMap().containsKey("tagName")){
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("tags", jsonArray);
                Result<TagsRecord> tagsRecords = sqlContext.selectFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId)).fetch();
                for(TagsRecord tagsRecord : tagsRecords){
                    jsonArray.put(new JSONObject()
                            .put("tagName", tagsRecord.getTagName())
                            .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).getEpochSecond())
                            .put("guildId", tagsRecord.getGuildId())
                            .put("userId", tagsRecord.getUserId())
                            .put("tagContent", tagsRecord.getTagContent())
                    );
                }
            }else{
                String tagName = ctx.pathParam("tagName");
                Result<TagsRecord> tagsRecords = sqlContext.selectFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId).and(Tables.TAGS.TAG_NAME.eq(tagName))).fetch();
                if(tagsRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                TagsRecord tagsRecord = tagsRecords.get(0);
                jsonObject
                        .put("tagName", tagsRecord.getTagName())
                        .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).getEpochSecond())
                        .put("guildId", tagsRecord.getGuildId())
                        .put("userId", tagsRecord.getUserId())
                        .put("tagContent", tagsRecord.getTagContent());
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTag#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTag#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String tagName = ctx.pathParam("tagName");
            // fetch
            Result<TagsRecord> tagsRecords = sqlContext.selectFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId).and(Tables.TAGS.TAG_NAME.eq(tagName))).fetch();
            if(tagsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            TagsRecord tagsRecord = tagsRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update record
            tagsRecord.setTagContent(newData.getString("tagContent"));
            // update db
            sqlContext.executeUpdate(tagsRecord);

            JSONObject jsonObject = new JSONObject()
                    .put("tagName", tagsRecord.getTagName())
                    .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).getEpochSecond())
                    .put("guildId", tagsRecord.getGuildId())
                    .put("userId", tagsRecord.getUserId())
                    .put("tagContent", tagsRecord.getTagContent());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_TAG").put("action", "UPDATE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTag#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTag#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String tagName = ctx.pathParam("tagName");
            // get data
            JSONObject newData = new JSONObject(ctx.body());
            // insert
            Result<TagsRecord> tagsRecords = sqlContext.insertInto(Tables.TAGS, Tables.TAGS.TAG_NAME, Tables.TAGS.GUILD_ID, Tables.TAGS.USER_ID, Tables.TAGS.TAG_CONTENT).values(tagName, guildId, newData.getLong("userId"), newData.getString("tagContent")).returning().fetch();
            if(tagsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            TagsRecord tagsRecord = tagsRecords.get(0);

            JSONObject jsonObject = new JSONObject()
                    .put("tagName", tagsRecord.getTagName())
                    .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).getEpochSecond())
                    .put("guildId", tagsRecord.getGuildId())
                    .put("userId", tagsRecord.getUserId())
                    .put("tagContent", tagsRecord.getTagContent());
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_TAG").put("action", "CREATE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTag#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTag#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String tagName = ctx.pathParam("tagName");
            int mod = sqlContext.deleteFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId).and(Tables.TAGS.TAG_NAME.eq(tagName))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_TAG").put("action", "DELETE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscTag#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscTag#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}