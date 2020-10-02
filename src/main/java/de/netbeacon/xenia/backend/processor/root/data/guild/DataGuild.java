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

package de.netbeacon.xenia.backend.processor.root.data.guild;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.data.guild.channel.DataGuildChannel;
import de.netbeacon.xenia.backend.processor.root.data.guild.license.DataGuildLicense;
import de.netbeacon.xenia.backend.processor.root.data.guild.member.DataGuildMember;
import de.netbeacon.xenia.backend.processor.root.data.guild.role.DataGuildRole;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.GuildsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

public class DataGuild extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuild.class);

    public DataGuild(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("guild", sqlConnectionPool, websocketProcessor,
                new DataGuildMember(sqlConnectionPool, websocketProcessor),
                new DataGuildChannel(sqlConnectionPool, websocketProcessor),
                new DataGuildRole(sqlConnectionPool, websocketProcessor),
                new DataGuildLicense(sqlConnectionPool, websocketProcessor)
        );
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // fetch
            Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(guildsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            GuildsRecord guildsRecord = guildsRecords.get(0);
            // build json
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", guildsRecord.getGuildId())
                    .put("creationTimestamp", guildsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("preferredLanguage", guildsRecord.getPreferredLanguage());
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
            logger.warn("An Error Occurred Processing DataGuild#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // fetch
            Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(guildsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            GuildsRecord guildsRecord = guildsRecords.get(0);
            // get new data
            JSONObject newData = new JSONObject(ctx.body());
            // update data
            guildsRecord.setPreferredLanguage(newData.getString("preferredLanguage"));
            // update db
            sqlContext.executeUpdate(guildsRecord);
            // build json
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", guildsRecord.getGuildId())
                    .put("creationTimestamp", guildsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("preferredLanguage", guildsRecord.getPreferredLanguage());
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
            logger.warn("An Error Occurred Processing DataGuild#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            // insert
            sqlContext.insertInto(Tables.GUILDS, Tables.GUILDS.GUILD_ID).values(guildId).execute();
            // fetch
            Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(guildsRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            GuildsRecord guildsRecord = guildsRecords.get(0);
            // build json
            JSONObject jsonObject = new JSONObject()
                    .put("guildId", guildsRecord.getGuildId())
                    .put("creationTimestamp", guildsRecord.getCreationTimestamp().toEpochSecond(ZoneOffset.UTC))
                    .put("preferredLanguage", guildsRecord.getPreferredLanguage());
            // respond
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuild#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            int mod = sqlContext.deleteFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).execute();
            if(mod == 0){
                throw new NotFoundResponse();
            }
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuild#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
