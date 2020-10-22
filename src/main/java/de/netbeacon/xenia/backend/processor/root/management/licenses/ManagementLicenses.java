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

package de.netbeacon.xenia.backend.processor.root.management.licenses;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.LicensesRecord;
import io.javalin.http.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementLicenses extends RequestProcessor {

    public final Logger logger = LoggerFactory.getLogger(ManagementLicenses.class);

    public ManagementLicenses(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("licenses", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            LicensesRecord licensesRecord;
            if(ctx.queryParam("licenseId") != null){
                Result<LicensesRecord> licensesRecords = sqlContext.selectFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_ID.eq(Integer.parseInt(ctx.queryParam("licenseId")))).fetch();
                if(licensesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                licensesRecord = licensesRecords.get(0);
            }else if(ctx.queryParam("licenseKey") != null){
                Result<LicensesRecord> licensesRecords = sqlContext.selectFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_KEY.eq(ctx.queryParam("licenseKey"))).fetch();
                if(licensesRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                licensesRecord = licensesRecords.get(0);
            }else{
                throw new BadRequestResponse();
            }
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("licenseID", licensesRecord.getLicenseId())
                    .put("licenseType", licensesRecord.getLicenseType())
                    .put("licenseDuration", licensesRecord.getLicenseDurationDays())
                    .put("licenseKey", licensesRecord.getLicenseKey());
            // return
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing ManagementLicenses#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            // create new license
            int licenseType = 1;
            if(ctx.queryParam("licenseType") != null){
                licenseType = Integer.parseInt(ctx.queryParam("licenseType"));
            }
            int licenseDuration = 30;
            if(ctx.queryParam("licenseDuration") != null){
                licenseDuration = Integer.parseInt(ctx.queryParam("licenseDuration"));
            }
            String licenseKey = RandomStringUtils.randomAlphanumeric(64);
            // insert
            sqlContext.insertInto(Tables.LICENSES, Tables.LICENSES.LICENSE_KEY, Tables.LICENSES.LICENSE_DURATION_DAYS, Tables.LICENSES.LICENSE_TYPE).values(licenseKey, licenseDuration, licenseType).execute();
            // fetch
            Result<LicensesRecord> licensesRecords = sqlContext.selectFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_KEY.eq(licenseKey)).fetch();
            if(licensesRecords.isEmpty()){
                throw new InternalServerErrorResponse();
            }
            LicensesRecord licensesRecord = licensesRecords.get(0);
            // json
            JSONObject jsonObject = new JSONObject()
                    .put("licenseID", licensesRecord.getLicenseId())
                    .put("licenseType", licensesRecord.getLicenseType())
                    .put("licenseDuration", licensesRecord.getLicenseDurationDays())
                    .put("licenseKey", licensesRecord.getLicenseKey());
            // return
            ctx.status(202);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing ManagementLicenses#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            int mod = 0;
            if(ctx.queryParam("licenseId") != null){
                mod = sqlContext.deleteFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_ID.eq(Integer.parseInt("licenseId"))).execute();
            }else if(ctx.queryParam("licenseKey") != null){
                mod = sqlContext.deleteFrom(Tables.LICENSES).where(Tables.LICENSES.LICENSE_KEY.eq(ctx.pathParam("licenseKey"))).execute();
            }else{
                throw new BadRequestResponse();
            }
            if(mod == 0){
                throw new NotFoundResponse();
            }
            // return
            ctx.status(200);
        }catch (HttpResponseException e){
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing ManagementLicenses#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}
