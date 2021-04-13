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

package de.netbeacon.xenia.backend.core.backgroundtasks;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import dev.mlnr.blh.api.BLHBuilder;
import dev.mlnr.blh.api.BotList;
import dev.mlnr.blh.api.BotListHandler;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BotListStatsUpdate extends BackgroundServiceScheduler.Task{

    private final BotListHandler botListHandler;

    public BotListStatsUpdate(SQLConnectionPool sqlConnectionPool) throws IOException {
        super(sqlConnectionPool, null, null);

        Map<BotList, String> botLists = new HashMap<>();
        // load from config
        JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(new File("./xenia-backend/config/botlists.json").toPath())));
        for(String key : jsonObject.keySet()){
            botLists.put(BotList.valueOf(key), jsonObject.getString(key));
        }
        botListHandler = new BLHBuilder(botLists).build();
    }

    @Override
    void onExecution() {

    }
}
