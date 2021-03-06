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

public abstract class WSProcessor{

	private final String action;
	protected WSProcessorCore wsProcessorCore;

	public WSProcessor(String action){
		this.action = action.toLowerCase();
	}

	protected void registerOn(WSProcessorCore wsProcessorCore){
		this.wsProcessorCore = wsProcessorCore;
	}

	public abstract WSResponse process(WSRequest wsRequest);

	public String getAction(){
		return action;
	}

}
