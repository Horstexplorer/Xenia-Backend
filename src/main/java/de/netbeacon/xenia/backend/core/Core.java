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
import de.netbeacon.xenia.backend.core.backgroundtasks.BackgroundServiceScheduler;
import de.netbeacon.xenia.backend.core.backgroundtasks.LicenseCheck;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.WebsocketProcessor;
import de.netbeacon.xenia.backend.processor.root.Root;
import de.netbeacon.xenia.backend.security.SecurityManager;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import de.netbeacon.xenia.backend.utils.oauth.DiscordOAuthHandler;
import io.javalin.Javalin;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Core {

    private final static Logger logger = LoggerFactory.getLogger(Core.class);

    public static void main(String...args){
        logger.info("\n"+
                "__   __           _             ______            _                  _ \n" +
                "\\ \\ / /          (_)            | ___ \\          | |                | |\n" +
                " \\ V /  ___ _ __  _  __ _ ______| |_/ / __ _  ___| | _____ _ __   __| |\n" +
                " /   \\ / _ \\ '_ \\| |/ _` |______| ___ \\/ _` |/ __| |/ / _ \\ '_ \\ / _` |\n" +
                "/ /^\\ \\  __/ | | | | (_| |      | |_/ / (_| | (__|   <  __/ | | | (_| |\n" +
                "\\/   \\/\\___|_| |_|_|\\__,_|      \\____/ \\__,_|\\___|_|\\_\\___|_| |_|\\__,_|\n" +
                "                                                                       ");
        try{
            logger.warn("! Starting Backend !");
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
                logger.warn("No Client Found. Added System Client\nUserId: "+admin.getClientId()+" Password: "+password);
            }
            // add to shutdown hook
            shutdownHook.addShutdownAble(clientManager);
            // prepare security manager
            SecurityManager securityManager = new SecurityManager(clientManager, new File("./xenia-backend/config/security")).loadFromFile();
            // prepare security settings
            SecuritySettings regularDataAccessSetting = new SecuritySettings(SecuritySettings.AuthType.TOKEN_OR_DISCORD, ClientType.ANY)
                    .putRateLimiterSetting(ClientType.DISCORD, TimeUnit.MINUTES, 1, 120L)
                    .putRateLimiterSetting(ClientType.BOT, TimeUnit.MINUTES, 1, 200000L);
            SecuritySettings tokenRequestSetting = new SecuritySettings(SecuritySettings.AuthType.BASIC, ClientType.INTERNAL);
            SecuritySettings tokenRenewSetting = new SecuritySettings(SecuritySettings.AuthType.TOKEN, ClientType.INTERNAL);
            SecuritySettings discordAuthSetting = new SecuritySettings(SecuritySettings.AuthType.OPTIONAL, ClientType.ANY); // no auth required, accepts oauth data
            SecuritySettings botSetupSetting = new SecuritySettings(SecuritySettings.AuthType.TOKEN, ClientType.BOT);
            SecuritySettings managementSetting = new SecuritySettings(SecuritySettings.AuthType.TOKEN, ClientType.SYSTEM);
            SecuritySettings websocketSetting = new SecuritySettings(SecuritySettings.AuthType.TOKEN, ClientType.INTERNAL);
            // add to shutdown hook
            shutdownHook.addShutdownAble(securityManager);
            // prepare websocket connection handler
            WebsocketProcessor websocketProcessor = new WebsocketProcessor();
            // prepare processor
            RequestProcessor processor = new Root(clientManager, connectionPool, websocketProcessor);
            // prepare oAuth handler
            DiscordOAuthHandler.createInstance(config.getLong("discord_client_id"), config.getString("discord_client_secret"),"https://web.xenia.netbeacon.de/oauth");
            // start background tasks
            BackgroundServiceScheduler backgroundServiceScheduler = new BackgroundServiceScheduler();
            shutdownHook.addShutdownAble(backgroundServiceScheduler);
            backgroundServiceScheduler.schedule(new LicenseCheck(connectionPool, websocketProcessor), 30000, true);
            // prepare javalin
            Javalin javalin = Javalin
                    .create(cnf -> {
                        cnf.enforceSsl = true;
                    })
                    .routes(()->{
                        path("auth", ()->{
                            path("discord", ()->{
                                get(ctx -> {
                                    Client client = securityManager.authorizeConnection(discordAuthSetting, ctx);
                                    processor.next("auth").next("discord").preProcessor(client, ctx).get(client, ctx); // verify oauth and hand over local auth token
                                });
                            });
                            path("token", ()->{
                                path("renew", ()->{
                                    get(ctx -> {
                                        Client client = securityManager.authorizeConnection(tokenRenewSetting, ctx);
                                        processor.next("auth").next("renew").preProcessor(client, ctx).get(client, ctx); // renew token by using it
                                    });
                                });
                                get(ctx -> {
                                    Client client = securityManager.authorizeConnection(tokenRequestSetting, ctx);
                                    processor.next("auth").next("token").preProcessor(client, ctx).get(client, ctx); // get token with password
                                });
                            });
                        });
                        path("setup", ()->{
                            path("bot", ()->{
                                get(ctx -> {
                                    Client client = securityManager.authorizeConnection(botSetupSetting, ctx);
                                    processor.next("setup").next("bot").preProcessor(client, ctx).get(client, ctx); // get setup data
                                });
                            });
                        });
                        path("data", ()->{
                            path("users", ()->{
                                path(":userId", ()->{
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
                            path("guilds", ()->{
                                path(":guildId", ()->{
                                    // member data
                                    path("members", ()->{
                                        path(":userId", ()->{
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
                                    path("roles", ()->{
                                        path(":roleId", ()->{
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
                                        get(ctx -> {
                                            Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                            processor.next("data").next("guild").next("role").get(client, ctx); // get data of all roles
                                        });
                                    });
                                    // channel
                                    path("channels", ()->{
                                        path(":channelId", ()->{
                                            path("messages", ()->{
                                                path(":messageId", ()->{
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
                                                    Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                                    processor.next("data").next("guild").next("channel").next("message").preProcessor(client, ctx).get(client, ctx); // get full guild data
                                                });
                                            });
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
                                        get(ctx -> {
                                            Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                            processor.next("data").next("guild").next("channel").preProcessor(client, ctx).get(client, ctx); // get data of all channels
                                        });
                                    });
                                    // license
                                    path("license", ()->{
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
                                    path("misc", ()->{
                                        // tags
                                        path("tags", ()->{
                                            path(":tagName", ()->{
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
                                            get(ctx -> {
                                                Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                                processor.next("data").next("guild").next("misc").next("tags").preProcessor(client, ctx).get(client, ctx); // get full tag data
                                            });
                                        });
                                        // notifications
                                        path("notifications", ()->{
                                            path(":notificationId", ()->{
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
                                            get(ctx -> {
                                                Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                                processor.next("data").next("guild").next("misc").next("notifications").preProcessor(client, ctx).get(client, ctx); // get full notification data
                                            });
                                        });
                                    });
                                    // guild
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
                        });
                        path("management", ()->{
                            path("clients", ()->{
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
                            path("licenses", ()->{
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
                        path("info", ()->{
                            path("public", ()->{
                                get(ctx -> {
                                    Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                    processor.next("info").next("public").preProcessor(client, ctx).get(client, ctx); // get public stats
                                });
                            });
                            path("private", ()->{
                                get(ctx -> {
                                    Client client = securityManager.authorizeConnection(regularDataAccessSetting, ctx);
                                    processor.next("info").next("private").preProcessor(client, ctx).get(client, ctx); // get private stats
                                });
                            });
                        });
                        path("ws", ()->{
                            ws(wsHandler -> {
                                wsHandler.onConnect(wsCon->{
                                    websocketProcessor.register(wsCon, securityManager.authorizeWsConnection(websocketSetting, wsCon));
                                });
                                wsHandler.onClose(websocketProcessor::remove);
                                wsHandler.onError(websocketProcessor::remove);
                            });
                        });
                        get("/", ctx -> {
                            ctx.html("<h1> Xenia-Backend </h1>\n"+"Running: "+AppInfo.get("buildVersion")+"_"+ AppInfo.get("buildNumber"));
                        });
                    })
                    .after(ctx -> {
                        ctx.header("Server", "Xenia-Backend");
                    });

            // create helper class for javalin
            class JavalinWrap implements IShutdown {
                private final Javalin javalin;
                public JavalinWrap(Javalin javalin){
                    this.javalin = javalin;
                }
                @Override
                public void onShutdown() throws Exception {
                    javalin.stop();
                }
            }
            // start javalin
            javalin.start(config.getInt("web_port"));
            // add to shutdown hook
            shutdownHook.addShutdownAble(new JavalinWrap(javalin));
            // ok
            logger.warn("! Backend Running !");
        }catch (Exception e){
            logger.error("! An Error Occurred Starting The Backend !", e);
            System.exit(-1);
        }
    }
}
