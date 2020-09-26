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

package de.netbeacon.utils.shutdownhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author horstexplorer
 */
public class ShutdownHook {

    private final Deque<IShutdown> shutdownDeque = new ArrayDeque<>();
    private final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    /**
     * Creates a new instance of this class and registers a new shutdown hook
     */
    public ShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            logger.warn("! Shutdown Hook Executed !");
            while(!shutdownDeque.isEmpty()){
                IShutdown iShutdown = shutdownDeque.remove();
                try{
                    iShutdown.onShutdown();
                }catch (Exception e){
                    logger.error("Failed To Shutdown "+iShutdown.getClass()+" ", e);
                }
            }
            logger.warn("! Shutdown Hook Finished Execution !");
        }));
    }

    /**
     * Adds a new Object to be executed on this shutdown hook
     * Added objects will be shut down in reverse order
     *
     * @param iShutdown object
     */
    public void addShutdownAble(IShutdown iShutdown){
        shutdownDeque.add(iShutdown);
    }
}
