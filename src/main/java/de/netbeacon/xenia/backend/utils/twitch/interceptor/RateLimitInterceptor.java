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

package de.netbeacon.xenia.backend.utils.twitch.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class ensures the handling of requests following the rate limits as okhttp interceptor
 */
public class RateLimitInterceptor implements Interceptor{

    private final AtomicLong maxRateLimit = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong remainingRateLimit = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong rateLimitReset = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger slowdown = new AtomicInteger(0);
    private final Random random = new Random();

    private final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        // get the request
        Request request = chain.request();
        switch (slowdown.get()){
            case 1:
                try { TimeUnit.MILLISECONDS.sleep(random.nextInt(100)+100); } catch (InterruptedException ignore) {}
                break;
            case 2:
                try { TimeUnit.MILLISECONDS.sleep(random.nextInt(250)+900); } catch (InterruptedException ignore) {}
                break;
            case 3:
                try { TimeUnit.MILLISECONDS.sleep(random.nextInt(500)+2900); } catch (InterruptedException ignore) {}
                break;
            default:
                break;
        }
        // proceed and get the response
        Response response = chain.proceed(request);
        String maxRateLimitHeader = response.header("Ratelimit-Limit");
        if(maxRateLimitHeader != null){
            try{
                maxRateLimit.set(Long.parseLong(maxRateLimitHeader));
            }catch (Exception e){
                logger.warn("Invalid Max-Ratelimit Header Recieved");
            }
        }
        String remainingRateLimitHeader = response.header("Ratelimit-Remaining");
        if(remainingRateLimitHeader != null){
            try{
                remainingRateLimit.set(Long.parseLong(remainingRateLimitHeader));
            }catch (Exception e){
                logger.warn("Invalid Remaining-Ratelimit Header Received");
            }
        }
        String rateLimitResetHeader = response.header("Ratelimit-Reset");
        if(rateLimitResetHeader != null){
            try{
                rateLimitReset.set(Long.parseLong(rateLimitResetHeader));
            }catch (Exception e){
                logger.warn("Invalid Remaining-Ratelimit Header Received");
            }
        }
        int responseCode = response.code();
        if(responseCode == 429){
            logger.warn("Hit Rate Limit. Server Returned 429");
            slowdown.set(2);
            // retry after some time
            response = chain.proceed(response.request());
        }else if(responseCode == 503){
            logger.warn("Service Overloaded. Server Returned 503");
            slowdown.set(3);
            // retry after more time
            response = chain.proceed(response.request());
        }else if((((double)remainingRateLimit.get()/(double)maxRateLimit.get()) < 0.05) && slowdown.get() == 0){
            logger.warn("Slowing Down To Not Hit A Rate Limit (<5% left)");
            slowdown.set(1);
        }else if((((double)remainingRateLimit.get()/(double)maxRateLimit.get()) > 0.2) && slowdown.get() != 0){
            logger.warn("Rate Limit Restored (>20% left)");
            slowdown.set(0);
        }
        return response;
    }
}
