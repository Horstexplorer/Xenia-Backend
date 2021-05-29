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

package de.netbeacon.utils.appender;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.netbeacon.utils.config.Config;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Can be used to log warnings and errors directly to discord via webhook
 *
 * @author horstexplorer
 */
public class DiscordWebhookAppender extends AppenderSkeleton{

	private File configFile;
	private String username;
	private boolean started = false;

	private WebhookClient webhookClient;
	private final Queue<LogContainer> eventCache = new LinkedList<>();
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Creates a new instance of this class (does not start the logger)
	 */
	public DiscordWebhookAppender(){}

	@Override
	protected void append(LoggingEvent event){
		if(event.getLevel().equals(Level.INFO) || event.getLevel().equals(Level.WARN) || event.getLevel().equals(Level.ERROR) || event.getLevel().equals(Level.FATAL)){
			eventCache.add(new LogContainer(event));
		}
	}

	@Override
	public void close(){
		this.closed = true;
		this.started = false;
		webhookClient.close();
		scheduledExecutorService.shutdownNow();
	}

	@Override
	public boolean requiresLayout(){
		return false;
	}

	/**
	 * Set the path to the config file
	 *
	 * @param fileName
	 */
	public synchronized void setFile(String fileName){
		this.configFile = new File(fileName);
	}

	/**
	 * Set the username shown in the webhook
	 *
	 * @param username
	 */
	public synchronized void setUser(String username){
		this.username = username;
	}

	/**
	 * Actually just starts the logger lol
	 *
	 * @param start
	 */
	public synchronized void setStart(boolean start){
		if(start && !started){
			if(configFile == null || username == null){
				System.err.println("No config file / username set");
				return;
			}
			started = true;
			try{
				Config config = new Config(configFile);
				String webhookURL = config.getString("webhookURL");
				webhookClient = WebhookClient.withUrl(webhookURL);
				scheduledExecutorService.scheduleAtFixedRate(() -> {
					if(eventCache.isEmpty()){
						return;
					}
					WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder()
						.setUsername(username);
					int i = 0;
					while(i++ < 10 && !eventCache.isEmpty()){
						LogContainer logContainer = eventCache.remove();
						String msg = logContainer.getMessage();
						WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder()
							.setColor(logContainer.getLevel() == Level.INFO ? Color.WHITE.getRGB() : (logContainer.getLevel() == Level.WARN ? Color.ORANGE.getRGB() : logContainer.getLevel() == Level.ERROR ? Color.RED.getRGB() : Color.BLACK.getRGB()))
							.setTitle(new WebhookEmbed.EmbedTitle(logContainer.getLogger().substring(logContainer.getLogger().lastIndexOf(".") + 1), null))
							.addField(new WebhookEmbed.EmbedField(false, "Message", msg.substring(0, Math.min(msg.length(), 1024))))
							.addField(new WebhookEmbed.EmbedField(true, "Level", logContainer.getLevel().toString()))
							.addField(new WebhookEmbed.EmbedField(true, "Timestamp", String.valueOf(logContainer.getTimestamp())))
							.addField(new WebhookEmbed.EmbedField(true, "Logger", logContainer.getLogger()))
							.setFooter(new WebhookEmbed.EmbedFooter("Additional logs cached: " + eventCache.size(), null));
						if(logContainer.getStacktrace() != null){
							var st = logContainer.getStacktrace();
							webhookEmbedBuilder.setDescription(st.substring(0, Math.min(st.length(), 1850)) + ".....");
						}
						webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
					}
					webhookClient.send(webhookMessageBuilder.build());
				}, 5, 5, TimeUnit.SECONDS);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static class LogContainer{

		private final Level level;
		private final String logger;
		private final String message;
		private final long timestamp;
		private String stacktrace;

		public LogContainer(LoggingEvent loggingEvent){
			this.level = loggingEvent.getLevel();
			this.logger = loggingEvent.getLoggerName();
			this.message = loggingEvent.getRenderedMessage();
			this.timestamp = loggingEvent.getTimeStamp();
			if(loggingEvent.getThrowableInformation() != null){
				stacktrace = ExceptionUtils.getStackTrace(loggingEvent.getThrowableInformation().getThrowable());
			}
		}

		public Level getLevel(){
			return level;
		}

		public String getLogger(){
			return logger;
		}

		public String getMessage(){
			return message;
		}

		public long getTimestamp(){
			return timestamp;
		}

		public String getStacktrace(){
			return stacktrace;
		}

	}

}
