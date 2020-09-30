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

package de.netbeacon.utils.ratelimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Calculates if an action has been performed x times within a given time interval
 *
 * @author horstexplorer
 */
public class RateLimiter {

    private final TimeUnit refillUnit;
    private final long refillUnitNumbers;
    private final long nsWindowSize;
    private long filler;
    private long maxUsages;
    private long nsPerUsage;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * This creates a new RateLimiter object
     *
     * @param refillUnit time unit in which the next value has to be interpreted
     * @param refillUnitNumbers number of timeunits it should take to fully refill the bucket
     */
    public RateLimiter(TimeUnit refillUnit, long refillUnitNumbers){
        this.refillUnit = refillUnit;
        this.refillUnitNumbers = refillUnitNumbers;
        nsWindowSize = refillUnit.toNanos(Math.abs(refillUnitNumbers));
    }

    /*                  GET                 */

    /**
     * Returns the TimeUnit used to calculate the window size
     *
     * @return TimeUnit
     */
    public TimeUnit getRefillUnit(){
        return refillUnit;
    }

    /**
     * Returns the number of TimeUnits used to calculate toe window size
     *
     * @return long
     */
    public long getRefillUnitNumbers(){
        return refillUnitNumbers;
    }

    /**
     * Returns the setting on how many usages are allowed within each refill cycle
     *
     * @return long
     */
    public long getMaxUsages(){
        return maxUsages;
    }

    /**
     * Calculates an estimate on how many usages are probably left within the refill cycle
     *
     * @return long
     */
    public long getRemainingUsages(){
        try{
            reentrantLock.lock();
            long current = System.nanoTime();
            if(filler < current){
                filler = current;
            }
            long div = Math.max(current + nsWindowSize - filler, 0);
            return (div / nsPerUsage);
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Returns an estimated timestamp at which the bucket should be completely refilled
     *
     * @return long
     */
    public long getRefillTime(){
        try{
            reentrantLock.lock();
            return System.currentTimeMillis()+((nsWindowSize-(getRemainingUsages()*nsPerUsage))/1000000);
        }finally {
            reentrantLock.unlock();
        }
    }

    /*                  SET                 */

    /**
     * Used to set the number of usages within each refill cycle
     *
     * @param maxUsages long
     */
    public void setMaxUsages(long maxUsages){
        try{
            reentrantLock.lock();
            this.maxUsages = maxUsages;
            nsPerUsage = nsWindowSize / maxUsages;
        }finally {
            reentrantLock.unlock();
        }
    }

    /*                  CHECK                   */

    /**
     * Increases the usage by one, returns true if this usage fits into the limit
     *
     * This will count up until double of the limit is reached </br>
     *
     * @return boolean
     */
    public boolean takeNice(){
        try{
            reentrantLock.lock();
            long current = System.nanoTime();
            // lower limit
            if(filler < current){
                filler = current;
            }
            // add take to filler
            filler += nsPerUsage;
            // upper limit
            if(filler > current+(nsWindowSize*2)){
                filler = current+(nsWindowSize*2);
            }
            // check if filler fits inside the window
            return (current+nsWindowSize) >= filler;
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Increases the usage by one
     *
     * This will count up until double of the limit is reached </br>
     *
     * @throws RateLimitException if the usage wont fit into the limit
     */
    public void take() throws RateLimitException {
        try{
            reentrantLock.lock();
            long current = System.nanoTime();
            // lower limit
            if(filler < current){
                filler = current;
            }
            // add take to filler
            filler += nsPerUsage;
            // upper limit
            if(filler > current+(nsWindowSize*2)){
                filler = current+(nsWindowSize*2);
            }
            // check if filler fits inside the window
            if((current+nsWindowSize) < filler){
                throw new RateLimitException("Ratelimit Exceeded");
            }
        }finally {
            reentrantLock.unlock();
        }
    }

    /*              Exception                   */

    /**
     * Helper class for exceptions
     */
    public static class RateLimitException extends Exception {
        public RateLimitException(String msg){
            super(msg);
        }
    }
}