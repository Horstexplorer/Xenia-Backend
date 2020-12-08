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

package de.netbeacon.xenia.backend.client.objects.imp;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import de.netbeacon.xenia.joop.Tables;
import de.netbeacon.xenia.joop.tables.records.OauthRecord;
import org.jooq.Result;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

public class DiscordClient extends Client {

    private final SQLConnectionPool sqlConnectionPool;
    private LocalDateTime validUntil = LocalDateTime.now().minusHours(1);
    private String authHash = "";

    public static DiscordClient create(long clientId, SQLConnectionPool sqlConnectionPool){
        return new DiscordClient(clientId, sqlConnectionPool);
    }

    private DiscordClient(long clientId, SQLConnectionPool sqlConnectionPool) {
        super(clientId, ClientType.DISCORD);
        this.sqlConnectionPool = sqlConnectionPool;
        try(var con = sqlConnectionPool.getConnection()){
            var sqlContext = getSqlConnectionPool().getContext(con);
            Result<OauthRecord> oauthRecords = sqlContext.selectFrom(Tables.OAUTH).where(Tables.OAUTH.USER_ID.eq(clientId)).fetch();
            if(!oauthRecords.isEmpty()){
                return;
            }
            OauthRecord oauthRecord = oauthRecords.get(0);
            validUntil = oauthRecord.getLocalAuthInvalidationTime();
            authHash = oauthRecord.getLocalAuthHash();
        }catch (Exception ignore){}
    }

    public SQLConnectionPool getSqlConnectionPool(){
        return sqlConnectionPool;
    }

    @Override
    public boolean verifyAuth(SecuritySettings.AuthType authType, String credentials) {
        if(SecuritySettings.AuthType.DISCORD.equals(authType)){
            return !LocalDateTime.now().isAfter(validUntil) && BCrypt.checkpw(credentials, authHash);
        }
        return false;
    }
}
