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

import de.netbeacon.xenia.backend.security.SecurityManager;

public class RatelimiterCleaner extends BackgroundServiceScheduler.Task{

    private final SecurityManager securityManager;
    public RatelimiterCleaner(SecurityManager securityManager) {
        super(null, null, null);
        this.securityManager = securityManager;
    }

    @Override
    void onExecution() {
        var noamap = securityManager.getNoAuthRateLimiterMap();
        noamap.forEach((k,v)->{
            if(v.getMaxUsages() == v.getRemainingUsages()){
                noamap.remove(k);
            }
        });
        var amap = securityManager.getAuthRateLimiterMap();
        amap.forEach((k,v)->{
            v.forEach((k2,v2)->{
                if(v2.getMaxUsages() == v2.getRemainingUsages()){
                    v.remove(k2);
                }
            });
            if(v.isEmpty()){
                amap.remove(k);
            }
        });
    }
}
