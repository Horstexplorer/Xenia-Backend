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

package de.netbeacon.xenia.backend.processor.ws.processor;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.SecondaryWebsocketProcessor;
import kotlin.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WSProcessorCore implements IShutdown{

	private final ConcurrentHashMap<String, WSProcessor> wsProcessorRegister = new ConcurrentHashMap<>();
	private final BlockingQueue<WSRequest> outgoingRequestQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<WSRequest> incomingRequestQueue = new LinkedBlockingQueue<>();
	private final ConcurrentHashMap<String, Pair<WSRequest, List<WSResponse>>> wsResponseCache = new ConcurrentHashMap<>();
	private SecondaryWebsocketProcessor secondaryWebsocketProcessor;
	private final Logger logger = LoggerFactory.getLogger(WSProcessorCore.class);
	private final ExecutorService executorService = Executors.newFixedThreadPool(2);

	public WSProcessorCore(){
		// incoming request processor
		executorService.execute(() -> {
			try{
				while(true){
					try{
						WSRequest wsRequest = incomingRequestQueue.take();
						if(!wsProcessorRegister.containsKey(wsRequest.getAction())){
							continue;
						}
						WSResponse wsResponse = wsProcessorRegister.get(wsRequest.getAction()).process(wsRequest);
						if(wsResponse == null){
							continue;
						}
						// send response
						Client recipient = secondaryWebsocketProcessor.findClient(wsResponse.getRecipient());
						if(recipient == null){
							return;
						}
						// we need to do the switch on our own as we work around the swp
						JSONObject jsonObject = wsResponse.asJSON().put("sender", 0L);
						jsonObject.remove("recipient");
						secondaryWebsocketProcessor.unicast(new WebsocketProcessor.WsMessage(jsonObject), recipient);
					}
					catch(InterruptedException e){
						throw e;
					}
					catch(Exception e){
						logger.warn("Processing Incoming Request Threw An Exception", e);
					}
				}
			}
			catch(Exception e){
				logger.warn("Received Interrupt For Incoming Request Processor - Shutting Down");
			}
		});
		// outgoing request processor
		executorService.execute(() -> {
			try{
				while(true){
					try{
						WSRequest wsRequest = outgoingRequestQueue.take();
						// send request
						if(wsRequest.getRequestMode().equals(WSRequest.Mode.BROADCAST)){
							JSONObject jsonObject = wsRequest.asJSON().put("sender", 0L); // we need to do the switch on our own as we work around the swp
							secondaryWebsocketProcessor.broadcast(new WebsocketProcessor.WsMessage(jsonObject));
						}
						else{
							Client recipient = secondaryWebsocketProcessor.findClient(wsRequest.getRecipient());
							if(recipient == null){
								return;
							}
							// we need to do the switch on our own as we work around the swp
							JSONObject jsonObject = wsRequest.asJSON().put("sender", 0L);
							jsonObject.remove("recipient");
							secondaryWebsocketProcessor.unicast(new WebsocketProcessor.WsMessage(jsonObject), recipient);
						}
					}
					catch(InterruptedException e){
						throw e;
					}
					catch(Exception e){
						logger.warn("Processing Outgoing Request Threw An Exception");
					}
				}
			}
			catch(Exception e){
				logger.warn("Received Interrupt For Outgoing Request Processor - Shutting Down");
			}
		});
	}

	public List<WSResponse> process(WSRequest wsRequest){
		try{
			// prepare the response cache (if needed)
			if(!wsRequest.exitOn.equals(WSRequest.ExitOn.INSTANT)){
				wsResponseCache.put(wsRequest.getRequestId(), new Pair<>(wsRequest, new ArrayList<>()));
			}
			// here we have a shiny new ws request which needs to be enqueued
			outgoingRequestQueue.add(wsRequest);
			// get the current timestamp
			long tss = System.currentTimeMillis();
			// should we stay or should we go *music*
			if(wsRequest.exitOn.equals(WSRequest.ExitOn.INSTANT)){
				return new ArrayList<>();
			}
			synchronized(wsRequest){
				wsRequest.wait(wsRequest.getTimeout());
			}
			// check if we ran into a timeout
			if(System.currentTimeMillis() - tss + 1 > wsRequest.getTimeout() && !wsRequest.getExitOn().equals(WSRequest.ExitOn.TIMEOUT)){
				wsResponseCache.remove(wsRequest.getRequestId());
				throw new RuntimeException("Request Timed Out");
			}
			// return result and clean up
			return wsResponseCache.remove(wsRequest.getRequestId()).component2();
		}
		catch(InterruptedException e){
			// clean up
			wsResponseCache.remove(wsRequest.getRequestId());
			throw new RuntimeException("Request Interrupted");
		}
	}

	public WSProcessorCore registerProcessors(WSProcessor... wsProcessors){
		for(WSProcessor wsProcessor : wsProcessors){
			if(wsProcessorRegister.containsKey(wsProcessor.getAction())){
				logger.warn("WSProcessor With The Same Action ID Already Registered - Overwriting");
			}
			wsProcessorRegister.put(wsProcessor.getAction(), wsProcessor);
		}
		return this;
	}

	public void setWSP(SecondaryWebsocketProcessor secondaryWebsocketProcessor){
		this.secondaryWebsocketProcessor = secondaryWebsocketProcessor;
	}

	public void handle(JSONObject message){
		try{
			if(secondaryWebsocketProcessor == null){
				return;
			}
			// check if this is a response we are awaiting ( an entry for the requestId exists within the response cache )
			if(message.getString("requestMode").equalsIgnoreCase("response") && wsResponseCache.containsKey(message.getString("requestId"))){
				// this is a response we are waiting for
				var p = wsResponseCache.get(message.getString("requestId"));
				p.component2().add(new WSResponse(message, WSResponse.IO.IN));
				if(p.component1().getExitOn().equals(WSRequest.ExitOn.FIRST_RESULT)){
					synchronized(p.component1()){
						p.component1().notify();
					}
				}
			}
			else{
				// this is a request we need to take care of
				incomingRequestQueue.put(new WSRequest(message, WSRequest.IO.IN, null, -1));
			}
		}
		catch(Exception e){
			logger.error("Something Went Wrong Handling An Incoming Message: " + message.toString(), e);
		}
	}

	@Override
	public void onShutdown() throws Exception{
		executorService.shutdown();
	}

}
