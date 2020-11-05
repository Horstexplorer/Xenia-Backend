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

package de.netbeacon.xenia.backend.processor.root.data.guild.misc.polls.vote;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.joop.Tables;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGuildMiscPollsVotes extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataGuildMiscPollsVotes.class);

    public DataGuildMiscPollsVotes(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("votes", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public RequestProcessor preProcessor(Client client, Context context) {
        return this;
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            long guildId = Long.parseLong(ctx.pathParam("guildId"));
            long pollId = Long.parseLong(ctx.pathParam("pollId"));
            int optionId = Integer.parseInt(ctx.pathParam("optionId"));
            long userId = Long.parseLong(ctx.pathParam("userId"));

            int mod = sqlContext.insertInto(Tables.POLLS_ENTRIES, Tables.POLLS_ENTRIES.POLL_ID, Tables.POLLS_ENTRIES.POLL_OPTION_ID, Tables.POLLS_ENTRIES.USER_ID).values(pollId, optionId, userId).execute();
            if(mod == 0){
                throw new BadRequestResponse();
            }
            // respond
            ctx.status(202);
            // send ws notification
            WebsocketProcessor.BroadcastMessage broadcastMessage = new WebsocketProcessor.BroadcastMessage();
            broadcastMessage.get().put("type", "GUILD_MISC_TAG").put("action", "UPDATE").put("guildId", guildId).put("pollId", pollId);
            getWebsocketProcessor().broadcast(broadcastMessage, client);
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataGuildMiscPollsVotes#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataGuildMiscPollsVotes#PUT ", e);
            throw new BadRequestResponse();
        }
    }
}
