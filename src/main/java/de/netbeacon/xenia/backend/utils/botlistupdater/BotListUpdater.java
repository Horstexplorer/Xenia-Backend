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

package de.netbeacon.xenia.backend.utils.botlistupdater;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.jooq.Tables;
import dev.mlnr.blh.core.api.BLHBuilder;
import dev.mlnr.blh.core.api.BotList;
import dev.mlnr.blh.core.api.BotListHandler;
import dev.mlnr.blh.core.api.IBLHUpdater;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BotListUpdater implements IBLHUpdater, IShutdown {

    private final SQLConnectionPool sqlConnectionPool;
    private final long botId;
    private int lastCount = 0;
    private final BotListHandler botListHandler;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BotListUpdater(SQLConnectionPool sqlConnectionPool) throws IOException {
        this.sqlConnectionPool = sqlConnectionPool;
        JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(new File("./xenia-backend/config/botlists").toPath())));
        this.botId = jsonObject.getLong("botId");
        JSONObject botLists = jsonObject.getJSONObject("botLists");
        Map<BotList, String> botListMap = new HashMap<>();
        for(String key : botLists.keySet()){
            botListMap.put(BotList.valueOf(key), botLists.getString(key));
        }
        this.botListHandler = new BLHBuilder(this, botListMap).setAutoPostDelay(15, TimeUnit.MINUTES).build();
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getServerCount() {
        try(var con = sqlConnectionPool.getConnection()){
            var sqlContext = sqlConnectionPool.getContext(con);
            lastCount = sqlContext.fetchCount(Tables.GUILDS);
        }catch (Exception e){
            logger.warn("An Error Occurred Getting The Server Count ", e);
        }
        return lastCount;
    }

    @Override
    public void onShutdown() throws Exception {
        // maybe needed at some point
    }
}
