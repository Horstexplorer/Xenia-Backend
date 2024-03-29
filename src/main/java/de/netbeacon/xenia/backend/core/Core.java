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

package de.netbeacon.xenia.backend.core;

import de.netbeacon.utils.appinfo.AppInfo;
import de.netbeacon.utils.config.Config;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.shutdownhook.ShutdownHook;
import de.netbeacon.utils.sql.auth.SQLAuth;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPoolSettings;
import de.netbeacon.xenia.backend.client.ClientManager;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.client.objects.ClientType;
import de.netbeacon.xenia.backend.core.backgroundtasks.*;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.root.Root;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.SecondaryWebsocketProcessor;
import de.netbeacon.xenia.backend.processor.ws.processor.WSProcessorCore;
import de.netbeacon.xenia.backend.processor.ws.processor.WSRequest;
import de.netbeacon.xenia.backend.processor.ws.processor.imp.*;
import de.netbeacon.xenia.backend.security.SecurityManager;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import de.netbeacon.xenia.backend.utils.botlistupdater.BotListUpdater;
import de.netbeacon.xenia.backend.utils.oauth.DiscordOAuthHandler;
import de.netbeacon.xenia.backend.utils.prometheus.Metrics;
import de.netbeacon.xenia.backend.utils.twitch.TwitchWrap;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Core{

	private static final Logger logger = LoggerFactory.getLogger(Core.class);

	public static void main(String... args){
		logger.info("\n" +
			"__   __           _             ______            _                  _ \n" +
			"\\ \\ / /          (_)            | ___ \\          | |                | |\n" +
			" \\ V /  ___ _ __  _  __ _ ______| |_/ / __ _  ___| | _____ _ __   __| |\n" +
			" /   \\ / _ \\ '_ \\| |/ _` |______| ___ \\/ _` |/ __| |/ / _ \\ '_ \\ / _` |\n" +
			"/ /^\\ \\  __/ | | | | (_| |      | |_/ / (_| | (__|   <  __/ | | | (_| |\n" +
			"\\/   \\/\\___|_| |_|_|\\__,_|      \\____/ \\__,_|\\___|_|\\_\\___|_| |_|\\__,_|\n" +
			"                                                                       ");
		try{
			logger.info("! Starting Backend !");
			// register shutdown hook
			ShutdownHook shutdownHook = new ShutdownHook();
			// prepare config
			Config config = new Config(new File("./xenia-backend/config/sys"));
			// prepare SQL connection
			SQLAuth sqlAuth = new SQLAuth(config.getString("sql_host"), config.getInt("sql_port"), config.getString("sql_db"), config.getString("sql_usr"), config.getString("sql_pw"));
			SQLConnectionPoolSettings sqlcpSettings = new SQLConnectionPoolSettings(config.getInt("sql_pool_min_con"), config.getInt("sql_pool_max_con"), config.getLong("sql_pool_con_timeout"), config.getLong("sql_pool_idle_timeout"), config.getLong("sql_pool_max_lifetime"));
			SQLConnectionPool connectionPool = new SQLConnectionPool(sqlcpSettings, sqlAuth);
			// add to shutdown hook
			shutdownHook.addShutdownAble(connectionPool);
			// prepare clients
			ClientManager clientManager = new ClientManager(new File("./xenia-backend/config/clients"), connectionPool).loadFromFile();
			if(clientManager.size() == 0){
				// add admin account
				String password = RandomStringUtils.randomAlphanumeric(64);
				Client admin = clientManager.createLocalClient(ClientType.SYSTEM, "System", password);
				logger.warn("No Client Found. Added System Client\nUserId: " + admin.getClientId() + " Password: " + password);
			}
			// add to shutdown hook
			shutdownHook.addShutdownAble(clientManager);
			// prepare security manager
			SecurityManager securityManager = new SecurityManager(clientManager, new File("./xenia-backend/config/security")).loadFromFile();
			// prepare security settings
			SecuritySettings openDataAccessSetting = new SecuritySettings(SecuritySettings.AuthType.OPTIONAL, ClientType.ANY);
			SecuritySettings regularDataAccessSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.ANY)
				.putRateLimiterSetting(ClientType.DISCORD, TimeUnit.MINUTES, 1, 60L)
				.putRateLimiterSetting(ClientType.BOT, TimeUnit.MINUTES, 1, 200000L);
			SecuritySettings tokenRequestSetting = new SecuritySettings(SecuritySettings.AuthType.BASIC, ClientType.INTERNAL);
			SecuritySettings tokenRenewSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.INTERNAL);
			SecuritySettings discordAuthReqSetting = new SecuritySettings(SecuritySettings.AuthType.OPTIONAL, ClientType.ANY); // no auth required, accepts oauth data
			SecuritySettings discordAuthSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.DISCORD)
				.putRateLimiterSetting(ClientType.DISCORD, TimeUnit.MINUTES, 1, 60L);
			SecuritySettings botSetupSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.BOT);
			SecuritySettings botPrivateStatSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.BOT);
			SecuritySettings frontendQoLSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.DISCORD)
				.putRateLimiterSetting(ClientType.DISCORD, TimeUnit.MINUTES, 1, 60L);
			SecuritySettings managementSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.SYSTEM);
			SecuritySettings websocketSetting = new SecuritySettings(SecuritySettings.AuthType.BEARER, ClientType.INTERNAL);
			SecuritySettings metricsSetting = new SecuritySettings(SecuritySettings.AuthType.BASIC, ClientType.METRICS);
			// add to shutdown hook
			shutdownHook.addShutdownAble(securityManager);
			// prepare twitch wrap
			TwitchWrap twitchWrap = new TwitchWrap(config.getString("twitch_user_id"), config.getString("twitch_user_secret"));
			// prepare websocket connection handler
			PrimaryWebsocketProcessor primaryWebsocketProcessor = new PrimaryWebsocketProcessor();
			shutdownHook.addShutdownAble(primaryWebsocketProcessor);
			WSProcessorCore wsProcessorCore = new WSProcessorCore()
				.registerProcessors(
					new HeartbeatProcessor(),
					new IdentifyProcessor(),
					new StatisticsProcessor(),
					new TwitchNotificationAccelerator(connectionPool, primaryWebsocketProcessor, twitchWrap),
					new ShardStartupProcessor()
				);
			SecondaryWebsocketProcessor secondaryWebsocketProcessor = new SecondaryWebsocketProcessor(wsProcessorCore);
			shutdownHook.addShutdownAble(secondaryWebsocketProcessor);
			// prepare processor
			RequestProcessor processor = new Root(clientManager, connectionPool, primaryWebsocketProcessor);
			// prepare oAuth handler
			DiscordOAuthHandler.createInstance(config.getLong("discord_client_id"), config.getString("discord_client_secret"), config.getString("discord_redirect_url"));
			// prepare BotListUpdater
			shutdownHook.addShutdownAble(new BotListUpdater(connectionPool));
			// start PrometheusQOL
			DefaultExports.initialize();
			// start background tasks
			BackgroundServiceScheduler backgroundServiceScheduler = new BackgroundServiceScheduler();
			shutdownHook.addShutdownAble(backgroundServiceScheduler);
			backgroundServiceScheduler.schedule(new LicenseCheck(connectionPool, primaryWebsocketProcessor), 30000, true);
			backgroundServiceScheduler.schedule(new RatelimiterCleaner(securityManager), 120000, true);
			backgroundServiceScheduler.schedule(new OAuthStateCleanup(connectionPool, primaryWebsocketProcessor), 120000, true);
			backgroundServiceScheduler.schedule(new TwitchNotificationProcessor(connectionPool, primaryWebsocketProcessor, secondaryWebsocketProcessor, twitchWrap), 300000, true);
			backgroundServiceScheduler.schedule(new TwitchNotificationCleanup(connectionPool, primaryWebsocketProcessor), 300000, true);
			backgroundServiceScheduler.schedule(new MessageCleanup(connectionPool, primaryWebsocketProcessor), 1800000, true);
			// prepare javalin
			Javalin javalin = Javalin
				.create(cnf -> {
					cnf.enforceSsl = true;
					cnf.enableCorsForOrigin("https://xenia.netbeacon.de/", "http://localhost/", "http://127.0.0.1/");
				})
				.routes(() -> {
					path("auth", () -> {
						path("discord", () -> {
							path("verify", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(discordAuthSetting, ctx);
									processor.next("auth").next("discord").next("verify").preProcessor(client, ctx).get(client, ctx);
								});
							});
							path("renew", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(discordAuthSetting, ctx);
									processor.next("auth").next("discord").next("renew").preProcessor(client, ctx).get(client, ctx);
								});
							});
							path("revoke", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(discordAuthSetting, ctx);
									processor.next("auth").next("discord").next("revoke").preProcessor(client, ctx).get(client, ctx);
								});
							});
							path("prepare", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(discordAuthReqSetting, ctx);
									processor.next("auth").next("discord").next("prepare").preProcessor(client, ctx).get(client, ctx);
								});
							});
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(discordAuthReqSetting, ctx);
								processor.next("auth").next("discord").preProcessor(client, ctx).get(client, ctx); // verify oauth and hand over local auth token
							});
						});
						path("token", () -> {
							path("renew", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(tokenRenewSetting, ctx);
									processor.next("auth").next("token").next("renew").preProcessor(client, ctx).get(client, ctx); // renew token by using it
								});
							});
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(tokenRequestSetting, ctx);
								processor.next("auth").next("token").preProcessor(client, ctx).get(client, ctx); // get token with password
							});
						});
					});
					path("setup", () -> {
						path("bot", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(botSetupSetting, ctx);
								processor.next("setup").next("bot").preProcessor(client, ctx).get(client, ctx); // get setup data
							});
						});
					});
					path("data", () -> {
						path("users", () -> {
							path(":userId", () -> {
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("user").preProcessor(client, ctx).get(client, ctx); // get user data
								});
								put(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("user").preProcessor(client, ctx).put(client, ctx); // update user data
								});
								post(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("user").preProcessor(client, ctx).post(client, ctx); // create new
								});
								delete(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("user").preProcessor(client, ctx).delete(client, ctx); // delete user
								});
							});
						});
						path("guilds", () -> {
							path(":guildId", () -> {
								// member data
								path("members", () -> {
									path(":userId", () -> {
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("member").preProcessor(client, ctx).get(client, ctx); // get member data
										});
										put(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("member").preProcessor(client, ctx).put(client, ctx); // update member data
										});
										post(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("member").preProcessor(client, ctx).post(client, ctx); // create new
										});
										delete(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("member").preProcessor(client, ctx).delete(client, ctx); // delete member
										});
									});
									get(ctx -> {
										Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
										processor.next("data").next("guild").next("member").preProcessor(client, ctx).get(client, ctx); // get data of all members
									});
								});
								// role
								path("roles", () -> {
									path(":roleId", () -> {
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("role").get(client, ctx); // get role data
										});
										put(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("role").put(client, ctx); // get update role data
										});
										post(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("role").post(client, ctx); // create new
										});
										delete(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("role").delete(client, ctx); // delete role
										});
									});
									before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
									get(ctx -> {
										Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
										processor.next("data").next("guild").next("role").get(client, ctx); // get data of all roles
									});
								});
								// channel
								path("channels", () -> {
									path(":channelId", () -> {
										path("messages", () -> {
											path(":messageId", () -> {
												before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
												get(ctx -> {
													Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
													processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).get(client, ctx); // get full guild data
												});
												put(ctx -> {
													Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
													processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).put(client, ctx); // update guild data
												});
												post(ctx -> {
													Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
													processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).post(client, ctx); // create guild data
												});
												delete(ctx -> {
													Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
													processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).delete(client, ctx); // delete guild
												});
											});
											get(ctx -> {
												Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc();

												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).get(client, ctx); // get full guild data
											});
										});
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("channel").preProcessor(client, ctx).get(client, ctx); // get channel data
										});
										put(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("channel").preProcessor(client, ctx).put(client, ctx); // update channel data
										});
										post(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("channel").preProcessor(client, ctx).post(client, ctx); // create new
										});
										delete(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("channel").preProcessor(client, ctx).delete(client, ctx); // delete channel
										});
									});
									before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
									get(ctx -> {
										Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
										processor.next("data").next("guild").next("channel").preProcessor(client, ctx).get(client, ctx); // get data of all channels
									});
								});
								// license
								path("license", () -> {
									before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
									get(ctx -> {
										Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
										processor.next("data").next("guild").next("license").preProcessor(client, ctx).get(client, ctx); // get current license
									});
									put(ctx -> {
										Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
										processor.next("data").next("guild").next("license").preProcessor(client, ctx).put(client, ctx); // update current license
									});
								});
								// misc
								path("misc", () -> {
									// tags
									path("tags", () -> {
										path(":tagName", () -> {
											before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
											get(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).get(client, ctx); // get tag data
											});
											put(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).put(client, ctx); // edit tag
											});
											post(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).post(client, ctx); // create tag
											});
											delete(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).delete(client, ctx); // delete tag
											});
										});
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).get(client, ctx); // get full tag data
										});
									});
									// notifications
									path("notifications", () -> {
										path(":notificationId", () -> {
											before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
											get(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).get(client, ctx); // get notification data
											});
											put(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).put(client, ctx); // edit notification
											});
											post(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).post(client, ctx); // create notification
											});
											delete(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).delete(client, ctx); // delete notification
											});
										});
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).get(client, ctx); // get full notification data
										});
									});
									path("twitchnotifications", () -> {
										path(":notificationId", () -> {
											before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
											get(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("twitchnotifications").preProcessor(client, ctx).get(client, ctx); // get notification data
											});
											put(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("twitchnotifications").preProcessor(client, ctx).put(client, ctx); // edit notification
											});
											post(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("twitchnotifications").preProcessor(client, ctx).post(client, ctx); // create notification
											});
											delete(ctx -> {
												Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
												processor.next("data").next("guild").next("misc").next("twitchnotifications").preProcessor(client, ctx).delete(client, ctx); // delete notification
											});
										});
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
											processor.next("data").next("guild").next("misc").next("twitchnotifications").preProcessor(client, ctx).get(client, ctx); // get full notification data
										});
									});
								});
								// guild
								before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
								get(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("guild").preProcessor(client, ctx).get(client, ctx); // get full guild data
								});
								put(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("guild").preProcessor(client, ctx).put(client, ctx); // update guild data
								});
								post(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("guild").preProcessor(client, ctx).post(client, ctx); // create guild data
								});
								delete(ctx -> {
									Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
									processor.next("data").next("guild").preProcessor(client, ctx).delete(client, ctx); // delete guild
								});
							});
						});
						path("client", () -> {
							path("discordbot", () -> {

							});
							path("frontend", () -> {
								path("me", () -> {
									before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
									get(ctx -> {
										Client client = securityManager.authorizeConnection(frontendQoLSetting, ctx);
										processor.next("data").next("client").next("frontend").next("me").preProcessor(client, ctx).get(client, ctx);
									});
								});
								path("meta", () -> {
									path("guilds", () -> {
										before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
										get(ctx -> {
											Client client = securityManager.authorizeConnection(frontendQoLSetting, ctx);
											processor.next("data").next("client").next("frontend").next("meta_guilds").preProcessor(client, ctx).get(client, ctx);
										});
									});
								});
							});
						});
					});
					path("management", () -> {
						path("clients", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("clients").preProcessor(client, ctx).get(client, ctx); // get channel data
							});
							put(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("clients").preProcessor(client, ctx).put(client, ctx); // update channel data
							});
							post(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("clients").preProcessor(client, ctx).post(client, ctx); // create new
							});
							delete(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("clients").preProcessor(client, ctx).delete(client, ctx); // delete channel
							});
						});
						path("licenses", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("licenses").preProcessor(client, ctx).get(client, ctx); // get channel data
							});
							post(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("licenses").preProcessor(client, ctx).post(client, ctx); // create new
							});
							delete(ctx -> {
								Client client = securityManager.authorizeConnection(managementSetting, ctx);
								processor.next("management").next("licenses").preProcessor(client, ctx).delete(client, ctx); // delete channel
							});
						});
					});
					path("info", () -> {
						path("metrics", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(metricsSetting, ctx);
								processor.next("info").next("metrics").preProcessor(client, ctx).get(client, ctx); // get metrics
							});
						});
						path("ping", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							// we might receive invalid auth data so we dont even check for this here
							head(ctx -> ctx.status(200));
							get(ctx -> ctx.status(200));
						});
						path("public", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(openDataAccessSetting, ctx);
								processor.next("info").next("public").preProcessor(client, ctx).get(client, ctx); // get public stats
							});
						});
						path("private", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							get(ctx -> {
								Client client = securityManager.authorizeConnection(botPrivateStatSetting, ctx);
								processor.next("info").next("private").preProcessor(client, ctx).get(client, ctx); // get private stats
							});
						});
					});
					path("ws", () -> {
						before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
						ws(wsHandler -> {
							wsHandler.onConnect(wsCon -> {
								Metrics.WS_CLIENT_CONNECTIONS.labels("primary").inc();
								primaryWebsocketProcessor.register(wsCon, securityManager.authorizeWsConnection(websocketSetting, wsCon, primaryWebsocketProcessor));
							});
							wsHandler.onClose(ctx -> {
								Metrics.WS_CLIENT_CONNECTIONS.labels("primary").dec();
								primaryWebsocketProcessor.remove(ctx);
							});
							wsHandler.onError(ctx -> {
								Metrics.WS_CLIENT_CONNECTIONS.labels("primary").dec();
								primaryWebsocketProcessor.remove(ctx);
							});
							wsHandler.onMessage(ctx -> {
								primaryWebsocketProcessor.onMessage(ctx);
								Metrics.WS_MESSAGES.labels("in", "received").inc();
							});
						});
						path("secondary", () -> {
							before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
							ws(wsHandler -> {
								wsHandler.onConnect(wsCon -> {
									Metrics.WS_CLIENT_CONNECTIONS.labels("secondary").inc();
									secondaryWebsocketProcessor.register(wsCon, securityManager.authorizeWsConnection(websocketSetting, wsCon, secondaryWebsocketProcessor));
								});
								wsHandler.onClose(ctx -> {
									Metrics.WS_CLIENT_CONNECTIONS.labels("secondary").dec();
									secondaryWebsocketProcessor.remove(ctx);
								});
								wsHandler.onError(ctx -> {
									Metrics.WS_CLIENT_CONNECTIONS.labels("secondary").dec();
									secondaryWebsocketProcessor.remove(ctx);
								});
								wsHandler.onMessage(ctx -> {
									Metrics.WS_MESSAGES.labels("in", "received").inc();
									secondaryWebsocketProcessor.onMessage(ctx);
								});
							});
						});
					});
					before(ctx -> Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), "*").inc());
					get("/", ctx -> {
						ctx.html("<h1> Xenia-Backend </h1>\n" + "Running: " + AppInfo.get("buildVersion") + "_" + AppInfo.get("buildNumber"));
					});
				})
				.exception(HttpResponseException.class, (exception, ctx) -> {
					Metrics.HTTP_REQUESTS.labels(ctx.matchedPath(), ctx.method(), String.valueOf(exception.getStatus())).inc();
					ctx.status(exception.getStatus());
				})
				.after(ctx -> {
					ctx.header("Server", "Xenia-Backend");
				});

			// create helper class for javalin
			class JavalinWrap implements IShutdown{

				private final Javalin javalin;

				public JavalinWrap(Javalin javalin){
					this.javalin = javalin;
				}

				@Override
				public void onShutdown() throws Exception{
					javalin.stop();
				}

			}
			// start javalin
			javalin.start(config.getInt("web_port"));
			// add to shutdown hook
			shutdownHook.addShutdownAble(new JavalinWrap(javalin));
			// prepare client notify in case the backend shuts down
			class ShutdownIRQ implements IShutdown{

				private final long delay = 5 * 1000;
				private final SecondaryWebsocketProcessor secondaryWebsocketProcessor;

				public ShutdownIRQ(SecondaryWebsocketProcessor secondaryWebsocketProcessor){
					this.secondaryWebsocketProcessor = secondaryWebsocketProcessor;
				}

				@Override
				public void onShutdown() throws Exception{
					logger.info("! Sending Shutdown IRQ !");
					WSRequest wsRequest = new WSRequest.Builder()
						.mode(WSRequest.Mode.BROADCAST)
						.recipient(0)
						.action("shutdownirq")
						.payload(new JSONObject()
							.put("at", System.currentTimeMillis() + delay)
						)
						.exitOn(WSRequest.ExitOn.INSTANT)
						.build();
					secondaryWebsocketProcessor.getWsProcessorCore().process(wsRequest);
					// sleep for the initial delay and a bit more to make sure that the clients have enough time to finish what they are doing
					TimeUnit.MILLISECONDS.sleep(delay + (delay / 2));
					logger.info("! Sending Shutdown IRQ Finished !");
				}

			}
			shutdownHook.addShutdownAble(new ShutdownIRQ(secondaryWebsocketProcessor));
			// ok
			logger.info("! Backend Running !");
		}
		catch(Exception e){
			logger.error("! An Error Occurred Starting The Backend !", e);
			System.exit(-1);
		}
	}

}
