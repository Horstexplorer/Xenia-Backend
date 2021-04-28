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

package de.netbeacon.utils.sql.connectionpool;

/**
 * Contains settings for the sql connection pool
 */
public class SQLConnectionPoolSettings{

	private final int minCon;
	private final int maxCon;
	private final long conTimeout;
	private final long idleTimeout;
	private final long maxLifetime;

	/**
	 * Creates a new instance of this class
	 * <p>
	 * This will set the default values for
	 * conTimeout = 10000
	 * idleTimeout = 600000
	 * maxLifetime = 1800000
	 *
	 * @param minCon min connection count when idle
	 * @param maxCon max connection count when used
	 */
	public SQLConnectionPoolSettings(int minCon, int maxCon){
		this.minCon = minCon;
		this.maxCon = maxCon;
		this.conTimeout = 10000;
		this.idleTimeout = 600000;
		this.maxLifetime = 1800000;
	}

	/**
	 * Creates a new instance of this class
	 *
	 * @param minCon      min connection count when idle
	 * @param maxCon      max connection count when used
	 * @param conTimeout  connection timeout in ms
	 * @param idleTimeout idle timeout in ms
	 * @param maxLifetime max connection lifetime
	 */
	public SQLConnectionPoolSettings(int minCon, int maxCon, long conTimeout, long idleTimeout, long maxLifetime){
		this.minCon = minCon;
		this.maxCon = maxCon;
		this.conTimeout = conTimeout;
		this.idleTimeout = idleTimeout;
		this.maxLifetime = maxLifetime;
	}

	/**
	 * Returns the min connection count
	 *
	 * @return int
	 */
	public int getMinCon(){
		return minCon;
	}

	/**
	 * Returns the max connection count
	 *
	 * @return int
	 */
	public int getMaxCon(){
		return maxCon;
	}

	/**
	 * Returns the connection timeout
	 *
	 * @return long
	 */
	public long getConTimeout(){
		return conTimeout;
	}

	/**
	 * Returns the idle timeout
	 *
	 * @return long
	 */
	public long getIdleTimeout(){
		return idleTimeout;
	}

	/**
	 * Returns the max lifetime of a connection
	 *
	 * @return long
	 */
	public long getMaxLifetime(){
		return maxLifetime;
	}

}
