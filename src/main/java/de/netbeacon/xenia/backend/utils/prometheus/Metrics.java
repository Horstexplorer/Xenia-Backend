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

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class Metrics{

	// GENERAL

	public static final Counter HTTP_REQUESTS = Counter.build()
		.name("xenia_backend_http_requests")
		.help("Amount of http requests received")
		.labelNames("endpoint", "type", "status")
		.register();

	public static final Gauge WS_CLIENT_CONNECTIONS = Gauge.build()
		.name("xenia_backend_ws_client_connections")
		.help("Amount of clients connected to the websocket")
		.labelNames("socket")
		.register();

	public static final Counter WS_MESSAGES = Counter.build()
		.name("xenia_backend_ws_messages")
		.help("Total amount of messages sent via the websocket")
		.labelNames("way", "action")
		.register();

}
