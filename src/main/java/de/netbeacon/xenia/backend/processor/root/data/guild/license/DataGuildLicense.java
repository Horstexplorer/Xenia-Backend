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

package de.netbeacon.xenia.backend.processor.root.data.guild.license;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.client.objects.imp.DiscordClient;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.GuildsRecord;
import de.netbeacon.xenia.jooq.tables.records.LicenseTypesRecord;
import de.netbeacon.xenia.jooq.tables.records.LicensesRecord;
import io.javalin.http.*;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.jooq.impl.DSL.bitAnd;


public class DataGuildLicense extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildLicense.class);

    public DataGuildLicense(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("license", sqlConnectionPool, websocketProcessor);
    }

    private static final long DISCORD_USER_PERM_FILTER = 1073741825; // interact, guild_owner_ov

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
            // fetch data
            Result<Record> recordResults = sqlContext.select().from(Tables.GUILDS).leftJoin(Tables.LICENSES).on(Tables.GUILDS.LICENSE_ID.eq(Tables.LICENSES.LICENSE_ID)).leftJoin(Tables.LICENSE_TYPES).on(Tables.LICENSES.LICENSE_TYPE.eq(Tables.LICENSE_TYPES.LICENSE_TYPE_ID)).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(recordResults.isEmpty()){
                throw new NotFoundResponse();
            }
            Record record = recordResults.get(0);
            // json
            JSONObject jsonObject = new JSONObject();
            // check for when no license has been found - return default stats then
            if(record.get(Tables.GUILDS.LICENSE_ID) == null){
                Result<LicenseTypesRecord> recordResult2 = sqlContext.selectFrom(Tables.LICENSE_TYPES).where(Tables.LICENSE_TYPES.LICENSE_TYPE_ID.eq(0)).fetch(); // should be the default
                if(recordResult2.isEmpty()){
                    throw new InternalServerErrorResponse();
                }
                Record record2 = recordResult2.get(0);
                jsonObject
                        .put("licenseName", record2.get(Tables.LICENSE_TYPES.LICENSE_NAME))
                        .put("licenseDescription", record2.get(Tables.LICENSE_TYPES.LICENSE_DESCRIPTION))
                        .put("activationTimestamp", -1)
                        .put("durationDays", -1)
                        .put("perks", new JSONObject()
                                .put("channelLogging", record2.get(Tables.LICENSE_TYPES.PERK_CHANNEL_LOGGING_C))
                                .put("guildRoles", record2.get(Tables.LICENSE_TYPES.PERK_GUILD_ROLES_C))
                                .put("miscTags", record2.get(Tables.LICENSE_TYPES.PERK_MISC_TAGS_C))
                                .put("miscNotifications", record2.get(Tables.LICENSE_TYPES.PERK_MISC_NOTIFICATIONS_C))
                        );
            }else{
                jsonObject
                        .put("licenseName", record.get(Tables.LICENSE_TYPES.LICENSE_NAME))
                        .put("licenseDescription", record.get(Tables.LICENSE_TYPES.LICENSE_DESCRIPTION))
                        .put("activationTimestamp", record.get(Tables.LICENSES.LICENSE_ACTIVATION_TIMESTAMP).toInstant(ZoneOffset.UTC).toEpochMilli())
                        .put("durationDays", record.get(Tables.LICENSES.LICENSE_DURATION_DAYS))
                        .put("perks", new JSONObject()
                                .put("channelLogging", record.get(Tables.LICENSE_TYPES.PERK_CHANNEL_LOGGING_C))
                                .put("guildRoles", record.get(Tables.LICENSE_TYPES.PERK_GUILD_ROLES_C))
                                .put("miscTags", record.get(Tables.LICENSE_TYPES.PERK_MISC_TAGS_C))
                                .put("miscNotifications", record.get(Tables.LICENSE_TYPES.PERK_MISC_NOTIFICATIONS_C))
                        );
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildLicense#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildLicense#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            String licenseKey = ctx.queryParam("licenseKey");
            if(licenseKey == null){
                throw new BadRequestResponse();
            }
            // fetch
            Result<LicensesRecord> licensesResult = sqlContext.selectFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_KEY.eq(licenseKey).and(Tables.LICENSES.LICENSE_CLAIMED.eq(false))).fetch();
            Result<GuildsRecord> guildsRecords = sqlContext.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(licensesResult.isEmpty() || guildsRecords.isEmpty()){
                throw new NotFoundResponse();
            }
            LicensesRecord licensesRecord = licensesResult.get(0);
            GuildsRecord guildsRecord = guildsRecords.get(0);
            // claim
            licensesRecord.setLicenseActivationTimestamp(LocalDateTime.now());
            licensesRecord.setLicenseClaimed(true);
            sqlContext.executeUpdate(licensesRecord);
            // link to guild
            guildsRecord.setLicenseId(licensesRecord.getLicenseId());
            sqlContext.executeUpdate(guildsRecord);
            // fetch new stats
            Result<Record> recordResults = sqlContext.select().from(Tables.GUILDS).leftJoin(Tables.LICENSES).on(Tables.GUILDS.LICENSE_ID.eq(Tables.LICENSES.LICENSE_ID)).leftJoin(Tables.LICENSE_TYPES).on(Tables.LICENSES.LICENSE_TYPE.eq(Tables.LICENSE_TYPES.LICENSE_TYPE_ID)).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(recordResults.isEmpty()){
                throw new NotFoundResponse();
            }
            Record record = recordResults.get(0);
            JSONObject jsonObject = new JSONObject()
                    .put("licenseName", record.get(Tables.LICENSE_TYPES.LICENSE_NAME))
                    .put("licenseDescription", record.get(Tables.LICENSE_TYPES.LICENSE_DESCRIPTION))
                    .put("activationTimestamp", record.get(Tables.LICENSES.LICENSE_ACTIVATION_TIMESTAMP).toInstant(ZoneOffset.UTC).toEpochMilli())
                    .put("durationDays", record.get(Tables.LICENSES.LICENSE_DURATION_DAYS))
                    .put("perks", new JSONObject()
                            .put("channelLogging", record.get(Tables.LICENSE_TYPES.PERK_CHANNEL_LOGGING_C))
                            .put("guildRoles", record.get(Tables.LICENSE_TYPES.PERK_GUILD_ROLES_C))
                            .put("miscTags", record.get(Tables.LICENSE_TYPES.PERK_MISC_TAGS_C))
                            .put("miscNotifications", record.get(Tables.LICENSE_TYPES.PERK_MISC_NOTIFICATIONS_C))
                    );
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
            // send ws notification
            WebsocketProcessor.WsMessage wsMessage = new WebsocketProcessor.WsMessage();
            wsMessage.get().put("type", "GUILD_LICENSE").put("action", "UPDATE").put("guildId", guildId);
            getWebsocketProcessor().broadcast(wsMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildLicense#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildLicense#PUT ", e);
            throw new BadRequestResponse();
        }
    }
}
