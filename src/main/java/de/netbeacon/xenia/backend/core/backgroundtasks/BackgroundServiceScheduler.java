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

package de.netbeacon.xenia.backend.core.backgroundtasks;

import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.SecondaryWebsocketProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundServiceScheduler implements IShutdown{

	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

	public void schedule(Task task, long interval, boolean repeat){
		if(repeat){
			scheduledExecutorService.scheduleAtFixedRate(task::execute, interval, interval, TimeUnit.MILLISECONDS);
		}
		else{
			scheduledExecutorService.schedule(task::execute, interval, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void onShutdown() throws Exception{
		scheduledExecutorService.shutdown();
	}

	public abstract static class Task{

		private final SQLConnectionPool sqlConnectionPool;
		private final PrimaryWebsocketProcessor primaryWebsocketProcessor;
		private final SecondaryWebsocketProcessor secondaryWebsocketProcessor;
		private final Logger logger = LoggerFactory.getLogger(getClass().getName());

		public Task(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor primaryWebsocketProcessor, SecondaryWebsocketProcessor secondaryWebsocketProcessor){
			this.sqlConnectionPool = sqlConnectionPool;
			this.primaryWebsocketProcessor = primaryWebsocketProcessor;
			this.secondaryWebsocketProcessor = secondaryWebsocketProcessor;
		}

		protected SQLConnectionPool getSqlConnectionPool(){
			return sqlConnectionPool;
		}

		protected PrimaryWebsocketProcessor getPrimaryWebsocketProcessor(){
			return primaryWebsocketProcessor;
		}

		protected SecondaryWebsocketProcessor getSecondaryWebsocketProcessor(){
			return secondaryWebsocketProcessor;
		}

		void execute(){
			try{
				onExecution();
			}
			catch(Exception e){
				logger.warn("Background task threw an uncaught exception: ", e);
			}
		}

		abstract void onExecution();

	}

}
