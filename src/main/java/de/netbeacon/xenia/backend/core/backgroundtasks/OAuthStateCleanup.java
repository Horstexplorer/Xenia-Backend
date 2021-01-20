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

package de.netbeacon.xenia.backend.core.backgroundtasks;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import io.javalin.http.BadRequestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OAuthStateCleanup extends BackgroundServiceScheduler.Task{

    private final Logger logger = LoggerFactory.getLogger(OAuthStateCleanup.class);

    public OAuthStateCleanup(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor) {
        super(sqlConnectionPool, primaryWebsocketProcessor, null);
    }

    @Override
    void onExecution() {
        try(var con = getSqlConnectionPool().getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            sqlContext.deleteFrom(Tables.OAUTH_STATES)
                    .where(Tables.OAUTH_STATES.CREATION_TIMESTAMP.lessThan(LocalDateTime.now().minusMinutes(10)))
                    .execute();
        }catch (Exception e){
            logger.warn("An Error Occurred Running OAuthStateCleanup ", e);
            throw new BadRequestResponse();
        }
    }
}
