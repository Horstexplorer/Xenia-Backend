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
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.GuildsRecord;
import de.netbeacon.xenia.joop.tables.records.LicensesRecord;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DataGuildLicense extends RequestProcessor {

    public DataGuildLicense(SQLConnectionPool sqlConnectionPool) {
        super("license", sqlConnectionPool);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var context = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildid"));
            Result<Record> recordR = context.select()
                    .from(Tables.GUILDS)
                    .leftJoin(Tables.LICENSES).on(Tables.GUILDS.LICENSE_ID.eq(Tables.LICENSES.LICENSE_ID))
                    .leftJoin(Tables.LICENSE_TYPES).on(Tables.LICENSES.LICENSE_TYPE.eq(Tables.LICENSE_TYPES.LICENSE_TYPE_ID))
                    .where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            // get record
            if(recordR.isEmpty()){
                throw new NotFoundResponse();
            }
            Record record = recordR.get(0);
            // check if a license is set
            if(record.get(Tables.GUILDS.LICENSE_ID) == null){
                // get default values which should be used
                record = context.select().from(Tables.LICENSE_TYPES).where(Tables.LICENSE_TYPES.LICENSE_TYPE_ID.eq(0)).fetch().get(0);
            }
            // prepare json
            JSONObject jsonObject = new JSONObject()
                    .put("licenseName", record.get(Tables.LICENSE_TYPES.LICENSE_NAME))
                    .put("licenseDescription", record.get(Tables.LICENSE_TYPES.LICENSE_DESCRIPTION))
                    .put("activationTime", record.get(Tables.LICENSES.LICENSE_ACTIVATION_TIMESTAMP).toEpochSecond(ZoneOffset.UTC))
                    .put("durationDays", record.get(Tables.LICENSES.LICENSE_DURATION_DAYS))
                    .put("perks", new JSONObject()
                            .put("channelLoggingPcb", record.get(Tables.LICENSE_TYPES.PERK_CHANNEL_LOGGING_PCB))
                            .put("channelLoggingMc", record.get(Tables.LICENSE_TYPES.PERK_CHANNEL_LOGGING_MC))
                    );
            // send result
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
            long guildId = Long.parseLong(ctx.pathParam("guildid"));
            String licenseKey = ctx.queryParam("licenseKey");
            if(licenseKey == null){
                throw new NotFoundResponse();
            }
            // make sure the guild exists and is available
            Result<GuildsRecord> guildsRecordsR = context.selectFrom(Tables.GUILDS).where(Tables.GUILDS.GUILD_ID.eq(guildId)).fetch();
            if(guildsRecordsR.isEmpty()){
                throw new NotFoundResponse("Guild");
            }
            // check if key is valid
            Result<LicensesRecord> licensesRecordsR = context.selectFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_KEY.eq(licenseKey).and(Tables.LICENSES.LICENSE_CLAIMED.eq(false))).fetch();
            if(licensesRecordsR.isEmpty()){
                throw new NotFoundResponse("License");
            }
            // activate this license
            context.executeUpdate(licensesRecordsR.get(0).setLicenseClaimed(true).setLicenseActivationTimestamp(LocalDateTime.now()));
            // register the license for this guild
            context.executeUpdate(guildsRecordsR.get(0).setLicenseId(licensesRecordsR.get(0).getLicenseId()));
        }catch (Exception e){
            throw new BadRequestResponse();
        }
    }
}
