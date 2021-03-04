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

package de.netbeacon.xenia.backend.processor.root.info.metrics;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InfoMetrics extends RequestProcessor {

    private final CollectorRegistry collectorRegistry;

    public InfoMetrics(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor) {
        super("metrics", sqlConnectionPool, websocketProcessor);
        this.collectorRegistry = CollectorRegistry.defaultRegistry;
    }

    @Override
    public void get(Client client, Context ctx) {
        String contentType = TextFormat.chooseContentType(ctx.header("Accept"));
        Set<String> includedParam = ctx.pathParamMap().containsKey("name[]") ? new HashSet<>(ctx.queryParams("name[]")) : Collections.emptySet();
        ctx.status(200);
        ctx.contentType(contentType);
        try{
            Writer writer = new BufferedWriter(ctx.res.getWriter());
            TextFormat.writeFormat(contentType, writer, collectorRegistry.filteredMetricFamilySamples(includedParam));
        }catch (IOException e){
            throw new InternalServerErrorResponse(e.getMessage());
        }
    }
}
