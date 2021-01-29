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

package de.netbeacon.xenia.backend.processor.root.data.client.discordbot.languagepack;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import de.netbeacon.xenia.jooq.tables.records.TranslationLanguagesRecord;
import de.netbeacon.xenia.jooq.tables.records.TranslationsRecord;
import io.javalin.http.*;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordBotLanguagePack extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DiscordBotLanguagePack.class);

    public DiscordBotLanguagePack(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("languagepack", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        if(client.getClientType().equals(ClientType.DISCORD)){
            throw new ForbiddenResponse();
        }
        return this;
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            JSONObject jsonObject = new JSONObject();
            if(!ctx.pathParamMap().containsKey("language")){
                Result<TranslationLanguagesRecord> translationLanguagesRecords = sqlContext.selectFrom(Tables.TRANSLATION_LANGUAGES).fetch();
                JSONArray jsonArray = new JSONArray();
                for(TranslationLanguagesRecord translationLanguagesRecord : translationLanguagesRecords){
                    jsonArray.put(new JSONObject().put("languageId", translationLanguagesRecord.getLanguageId()).put("languageName", translationLanguagesRecord.getLanguageName()));
                }
                jsonObject.put("languages", jsonArray);
            }else{
                String language = ctx.pathParam("language");
                Result<TranslationsRecord> translationsRecords = sqlContext.selectFrom(Tables.TRANSLATIONS).where(Tables.TRANSLATIONS.TRANSLATION_LANGUAGE_ID.eq(language)).fetch();
                if(translationsRecords.isEmpty()){
                    throw new NotFoundResponse();
                }
                JSONObject jsonObject1 = new JSONObject();
                for(TranslationsRecord translationRecord : translationsRecords){
                    jsonObject1.put(translationRecord.getTranslationKey(), translationRecord.getTranslation());
                }
                jsonObject.put("translations", jsonObject1);
            }
            // respond
            ctx.status(200);
            ctx.header("Content-Type", "application/json");
            ctx.result(jsonObject.toString());
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DiscordBotLanguagePack#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DiscordBotLanguagePack#GET ", e);
            throw new BadRequestResponse();
        }
    }
}
