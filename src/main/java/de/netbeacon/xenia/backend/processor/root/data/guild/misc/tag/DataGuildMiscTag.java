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
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TagsRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

import static org.jooq.impl.DSL.bitAnd;

public class DataGuildMiscTag extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscTag.class);

    public DataGuildMiscTag(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor) {
        super("tags", sqlConnectionPool, websocketProcessor);
    }

    private static final long DISCORD_USER_PERM_FILTER = 9; // interact, tag_ov

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
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("tagName")){
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("tags", jsonArray);
                Result<TagsRecord> tagsRecords = sqlContext.selectFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId)).fetch();
                for(TagsRecord tagsRecord : tagsRecords){
                    jsonArray.put(new JSONObject()
                            .put("tagName", tagsRecord.getTagName())
                            .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
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
                        .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
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
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
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
                    .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("guildId", tagsRecord.getGuildId())
                    .put("userId", tagsRecord.getUserId())
                    .put("tagContent", tagsRecord.getTagContent());
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TAG").put("action", "UPDATE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(wsMessage, client);
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
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
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
                    .put("creationTimestamp", tagsRecord.getCreationTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("guildId", tagsRecord.getGuildId())
                    .put("userId", tagsRecord.getUserId())
                    .put("tagContent", tagsRecord.getTagContent());
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TAG").put("action", "CREATE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(wsMessage, client);
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
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String tagName = ctx.pathParam("tagName");
            int mod = sqlContext.deleteFrom(Tables.TAGS).where(Tables.TAGS.GUILD_ID.eq(guildId).and(Tables.TAGS.TAG_NAME.eq(tagName))).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_MISC_TAG").put("action", "DELETE").put("guildId", guildId).put("tagName", tagName);
            getWebsocketProcessor().broadcast(wsMessage, client);
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
