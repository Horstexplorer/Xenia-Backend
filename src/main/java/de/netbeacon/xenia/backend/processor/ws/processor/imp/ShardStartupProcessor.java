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

package de.netbeacon.xenia.backend.processor.ws.processor.imp;

import de.netbeacon.xenia.backend.processor.ws.processor.WSProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.processor.ws.processor.WSResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ShardStartupProcessor extends WSProcessor{

	private final Object notifyStarted = new Object();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final BlockingQueue<ShardStartupTask> shardStartupTasksQ = new LinkedBlockingQueue<>();

	public ShardStartupProcessor(){
		super("shardstartup");
		Executors.newSingleThreadExecutor().execute(() -> {
			try{
				while(true){
					try{
						// take and send
						var next = shardStartupTasksQ.take();
						Thread.sleep(5000);
						WSRequest wsRequest = new WSRequest.Builder()
							.mode(WSRequest.Mode.UNICAST)
							.recipient(next.location)
							.action("shardstartup")
							.exitOn(WSRequest.ExitOn.INSTANT)
							.payload(new JSONObject()
								.put("shardId", next.shardId())
							)
							.build();
						wsProcessorCore.process(wsRequest);
						// wait for shard to start, will skip wait after 60 seconds
						synchronized(notifyStarted){
							notifyStarted.wait(60000);
						}
					}
					catch(InterruptedException e){
						logger.warn("An issue occurred starting a shard ", e);
					}
				}
			}catch(Exception e){
				logger.warn("An issue occurred reacting on shard starts ", e);
			}
		});
	}

	@Override
	public WSResponse process(WSRequest wsRequest){
		JSONObject payload = wsRequest.getPayload();
		if("enqueue".equalsIgnoreCase(payload.getString("task"))){
			shardStartupTasksQ.add(new ShardStartupTask(
				payload.getInt("shardId"),
				wsRequest.getSender()
			));
		}else if("started".equalsIgnoreCase(payload.getString("task"))){
			synchronized(notifyStarted){
				notifyStarted.notify();
			}
		}
		return null;
	}

	public record ShardStartupTask(int shardId, long location) {}
}
