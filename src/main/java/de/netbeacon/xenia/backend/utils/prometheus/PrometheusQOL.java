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

package de.netbeacon.xenia.backend.utils.prometheus;

import de.netbeacon.utils.shutdownhook.IShutdown;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

public class PrometheusQOL implements IShutdown {

    public final HTTPServer httpServer;

    public PrometheusQOL(int port) throws IOException {
        DefaultExports.initialize();
        this.httpServer = new HTTPServer(port);
    }

    @Override
    public void onShutdown() throws Exception {
        httpServer.stop();
    }
}
