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

package de.netbeacon.xenia.backend.processor.root.data.message;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.clients.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import kotlin.NotImplementedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMessage extends RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(DataMessage.class);

    public DataMessage(SQLConnectionPool sqlConnectionPool, WebsocketProcessor websocketProcessor) {
        super("message", sqlConnectionPool, websocketProcessor);
    }

    @Override
    public void get(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            throw new NotImplementedError();
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataMessage#GET ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataMessage#GET ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void put(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            throw new NotImplementedError();
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataMessage#PUT ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataMessage#PUT ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void post(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            throw new NotImplementedError();
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataMessage#POST ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataMessage#POST ", e);
            throw new BadRequestResponse();
        }
    }

    @Override
    public void delete(Client client, Context ctx) {
        try(var con = getSqlConnectionPool().getConnection(); var sqlContext = getSqlConnectionPool().getContext(con)){
            throw new NotImplementedError();
        }catch (HttpResponseException e){
            if(e instanceof InternalServerErrorResponse){
                logger.error("An Error Occurred Processing DataMESSAGE#DELETE ", e);
            }
            throw e;
        }catch (NullPointerException e){
            // dont log
            throw new BadRequestResponse();
        }catch (Exception e){
            logger.warn("An Error Occurred Processing DataMessage#DELETE ", e);
            throw new BadRequestResponse();
        }
    }
}