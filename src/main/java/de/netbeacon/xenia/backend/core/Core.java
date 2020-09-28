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
import de.netbeacon.xenia.backend.clients.ClientManager;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.root.Root;
import de.netbeacon.xenia.backend.security.SecurityManager;
import de.netbeacon.xenia.backend.security.SecuritySettings;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

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
            ClientManager clientManager = new ClientManager(new File("./xenia-backend/config/clients")).loadFromFile();
            // add to shutdown hook
            shutdownHook.addShutdownAble(clientManager);
            // prepare security manager
            SecurityManager securityManager = new SecurityManager(clientManager, new File("./xenia-backend/config/security")).loadFromFile();
            // prepare security settings
            SecuritySettings tokenRequestSecSet = new SecuritySettings(SecuritySettings.AuthType.Basic, SecuritySettings.ClientType.Any);
            SecuritySettings botSetupSecSet = new SecuritySettings(SecuritySettings.AuthType.Token, SecuritySettings.ClientType.Bot);
            SecuritySettings dataSettingsSecSet = new SecuritySettings(SecuritySettings.AuthType.Token, SecuritySettings.ClientType.Any);
            SecuritySettings managementSecSet = new SecuritySettings(SecuritySettings.AuthType.Token, SecuritySettings.ClientType.Any);
            // add to shutdown hook
            shutdownHook.addShutdownAble(securityManager);
            // prepare processor
            RequestProcessor processor = new Root(connectionPool);
            // prepare javalin
            Javalin javalin = Javalin
                    .create(cnf -> {
                        cnf.enforceSsl = true;
                    })
                    .routes(()->{
                        path("auth", ()->{
                            path("token", ()->{
                                get(ctx -> {
                                   // get token with password
                                    processor.next("auth").next("token").get(securityManager.authorizeConnection(tokenRequestSecSet, ctx), ctx);
                                });
                            });
                        });
                        path("setup", ()->{
                            path("bot", ()->{
                                get(ctx -> {
                                    // get setup data
                                    processor.next("setup").next("bot").get(securityManager.authorizeConnection(botSetupSecSet, ctx), ctx);
                                });
                            });
                        });
                        path("data", ()->{
                            path("user", ()->{
                                path(":userId", ()->{
                                    get(ctx -> {
                                        // get user data
                                        processor.next("data").next("guild").next("user").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                    put(ctx -> {
                                        // update user data
                                        processor.next("data").next("guild").next("user").put(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                    post(ctx -> {
                                        // create new
                                        processor.next("data").next("guild").next("user").post(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                    delete(ctx -> {
                                        // delete user
                                        processor.next("data").next("guild").next("user").delete(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                });
                            });
                            path("guild", ()->{
                                path(":guildId", ()->{
                                    // member data
                                    path("member", ()->{
                                        path(":userId", ()->{
                                            get(ctx -> {
                                                // get member data
                                                processor.next("data").next("guild").next("member").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            put(ctx -> {
                                                // update member data
                                                processor.next("data").next("guild").next("member").put(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            post(ctx -> {
                                                // create new
                                                processor.next("data").next("guild").next("member").post(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            delete(ctx -> {
                                                // delete member
                                                processor.next("data").next("guild").next("member").delete(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all members
                                            processor.next("data").next("guild").next("member").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                        });
                                    });
                                    // role
                                    path("role", ()->{
                                        path(":roleId", ()->{
                                            get(ctx -> {
                                                // get role data
                                                processor.next("data").next("guild").next("role").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            put(ctx -> {
                                                // get update role data
                                                processor.next("data").next("guild").next("role").put(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            post(ctx -> {
                                                // create new
                                                processor.next("data").next("guild").next("role").post(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            delete(ctx -> {
                                                // delete role
                                                processor.next("data").next("guild").next("role").delete(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all roles
                                            processor.next("data").next("guild").next("role").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                        });
                                    });
                                    // channel
                                    path("channel", ()->{
                                        path(":channelId", ()->{
                                            get(ctx -> {
                                                // get channel data
                                                processor.next("data").next("guild").next("channel").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            put(ctx -> {
                                                // update channel data
                                                processor.next("data").next("guild").next("channel").put(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            post(ctx -> {
                                                // create new
                                                processor.next("data").next("guild").next("channel").post(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            delete(ctx -> {
                                                // delete channel
                                                processor.next("data").next("guild").next("channel").delete(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all channels
                                            processor.next("data").next("guild").next("channel").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                        });
                                    });
                                    path("license", ()->{
                                            get(ctx -> {
                                                // get current license
                                                processor.next("data").next("guild").next("license").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                            put(ctx -> {
                                                // update current license
                                                processor.next("data").next("guild").next("license").put(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                            });
                                    });
                                    get(ctx -> {
                                        // get full guild data
                                        processor.next("data").next("guild").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                    post(ctx -> {
                                        // create guild data
                                        processor.next("data").next("guild").post(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                    delete(ctx -> {
                                        // delete guild
                                        processor.next("data").next("guild").delete(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                    });
                                });
                            });
                        });
                        path("management", ()->{
                            path("clients", ()->{
                                get(ctx -> {
                                    // get channel data
                                    processor.next("management").next("clients").get(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                put(ctx -> {
                                    // update channel data
                                    processor.next("management").next("clients").put(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                post(ctx -> {
                                    // create new
                                    processor.next("management").next("clients").post(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                delete(ctx -> {
                                    // delete channel
                                    processor.next("management").next("clients").delete(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                            });
                            path("licenses", ()->{
                                get(ctx -> {
                                    // get channel data
                                    processor.next("management").next("licenses").get(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                put(ctx -> {
                                    // update channel data
                                    processor.next("management").next("licenses").put(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                post(ctx -> {
                                    // create new
                                    processor.next("management").next("licenses").post(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                                delete(ctx -> {
                                    // delete channel
                                    processor.next("management").next("licenses").delete(securityManager.authorizeConnection(managementSecSet, ctx), ctx);
                                });
                            });
                        });
                        path("info", ()->{
                            path("public", ()->{
                                get(ctx -> {
                                    // get public stats
                                    processor.next("info").next("public").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                });
                            });
                            path("private", ()->{
                                get(ctx -> {
                                    // get private stats
                                    processor.next("info").next("private").get(securityManager.authorizeConnection(dataSettingsSecSet, ctx), ctx);
                                });
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
