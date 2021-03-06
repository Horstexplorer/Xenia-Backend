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

package de.netbeacon.xenia.backend.processor.root.info.ppublic;

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.jooq.Tables;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import org.json.JSONObject;

public class InfoPublic extends RequestProcessor{

	public InfoPublic(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor){
		super("public", sqlConnectionPool, websocketProcessor);
	}

	@Override
	public RequestProcessor preProcessor(Client client, Context context){
		return this;
	}

	@Override
	public void get(Client client, Context ctx){
		try(var con = getSqlConnectionPool().getConnection()){
			var sqlContext = getSqlConnectionPool().getContext(con);
			// the number of known users
			int users = sqlContext.fetchCount(Tables.USERS);
			// get the number of known guilds
			int guilds = sqlContext.fetchCount(Tables.GUILDS);
			// get the number of known members
			int members = sqlContext.fetchCount(Tables.MEMBERS);
			// build json
			JSONObject jsonObject = new JSONObject()
				.put("version", AppInfo.get("buildVersion") + "_" + AppInfo.get("buildNumber"))
				.put("guilds", guilds)
				.put("users", users)
				.put("members", members);
			// return
			ctx.status(200);
			ctx.header("Content-Type", "application/json");
			ctx.result(jsonObject.toString());
		}
		catch(HttpResponseException e){
			if(e instanceof InternalServerErrorResponse){
				logger.error("An Error Occurred Processing InfoPublic#GET ", e);
			}
			throw e;
		}
		catch(NullPointerException e){
			// dont log
			throw new BadRequestResponse();
		}
		catch(Exception e){
			logger.warn("An Error Occurred Processing InfoPublic#GET ", e);
			throw new BadRequestResponse();
		}
	}

}
