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

package de.netbeacon.utils.sql.auth;

/**
 * Can be used to store the login data for preferably an sql db (preferably postgres)
 *
 * @author horstexplorer
 */
public class SQLAuth{

	private final String url;
	private final String username;
	private final String password;

	/**
	 * Creates a new instance of this class
	 *
	 * @param host     of the db
	 * @param port     of the db
	 * @param db       name
	 * @param username username
	 * @param password password
	 */
	public SQLAuth(String host, int port, String db, String username, String password){
		this.url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
		this.username = username;
		this.password = password;
	}

	/**
	 * Creates a new instance of this class
	 *
	 * @param url      jdbc url
	 * @param username username
	 * @param password password
	 */
	public SQLAuth(String url, String username, String password){
		this.url = url;
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the jdbc url for this auth
	 * <p>
	 * If not specified manually this returns the url for postgres
	 *
	 * @return String
	 */
	public String getUrl(){
		return url;
	}

	/**
	 * Returns the username
	 *
	 * @return String
	 */
	public String getUsername(){
		return username;
	}

	/**
	 * Returns the password
	 *
	 * @return String
	 */
	public String getPassword(){
		return password;
	}

}
