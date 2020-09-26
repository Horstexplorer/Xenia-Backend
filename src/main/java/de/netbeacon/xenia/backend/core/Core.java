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

import de.netbeacon.utils.config.Config;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.utils.shutdownhook.ShutdownHook;
import de.netbeacon.utils.sql.auth.SQLAuth;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.utils.sql.connectionpool.SQLConnectionPoolSettings;
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
            // prepare auth

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
                                });
                            });
                        });
                        path("setup", ()->{
                            path("bot", ()->{
                                get(ctx -> {
                                    // get setup data
                                });
                            });
                        });
                        path("data", ()->{
                            path("user", ()->{
                                path(":userId", ()->{
                                    get(ctx -> {
                                        // get user data
                                    });
                                    put(ctx -> {
                                        // update user data
                                    });
                                    post(ctx -> {
                                        // create new
                                    });
                                    delete(ctx -> {
                                        // delete guild
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
                                            });
                                            put(ctx -> {
                                                // update member data
                                            });
                                            post(ctx -> {
                                                // create new
                                            });
                                            delete(ctx -> {
                                                // delete member
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all members
                                        });
                                    });
                                    // role
                                    path("role", ()->{
                                        path(":roleId", ()->{
                                            get(ctx -> {
                                                // get role data
                                            });
                                            put(ctx -> {
                                                // get update role data
                                            });
                                            post(ctx -> {
                                                // create new
                                            });
                                            delete(ctx -> {
                                                // delete role
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all roles
                                        });
                                    });
                                    // channel
                                    path("channel", ()->{
                                        path(":channelId", ()->{
                                            get(ctx -> {
                                                // get channel data
                                            });
                                            put(ctx -> {
                                                // update channel data
                                            });
                                            post(ctx -> {
                                                // create new
                                            });
                                            delete(ctx -> {
                                                // delete channel
                                            });
                                        });
                                        get(ctx -> {
                                            // get data of all channels
                                        });
                                    });
                                });
                            });
                        });
                        path("info", ()->{
                            path("public", ()->{
                                get(ctx -> {
                                    // get public stats
                                });
                            });
                            path("private", ()->{
                                get(ctx -> {
                                    // get private stats
                                });
                            });
                        });
                        get("/", ctx -> {
                            ctx.result("Xenia-Backend");
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
            shutdownHook.addShutdownAble(new JavalinWrap(javalin));
            // ok
            logger.warn("! Backend Running !");
        }catch (Exception e){
            logger.error("! An Error Occurred Starting The Backend !", e);
            System.exit(-1);
        }
    }

}
