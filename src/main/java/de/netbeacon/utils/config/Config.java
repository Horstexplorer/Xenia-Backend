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

package de.netbeacon.utils.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Reads json config files
 *
 * @author horstexplorer
 */
public class Config{

	private final JSONObject configJSON;

	/**
	 * Creates a new config from a given file
	 *
	 * @param file File containing the settings in json format
	 *
	 * @throws IOException if file does not exist or is not readable
	 */
	public Config(File file) throws IOException{
		if(!file.exists()){
			throw new IOException("Config File Not Found");
		}
		else{
			configJSON = new JSONObject(new String(Files.readAllBytes(file.toPath())));
		}
	}

	/**
	 * Returns a string value for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public String getString(String key){
		try{
			return configJSON.getString(key);
		}
		catch(Exception e){
			return "";
		}
	}

	/**
	 * Returns a long value for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public long getLong(String key){
		try{
			return configJSON.getLong(key);
		}
		catch(Exception e){
			return 0L;
		}
	}

	/**
	 * Returns an int value for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public int getInt(String key){
		try{
			return configJSON.getInt(key);
		}
		catch(Exception e){
			return 0;
		}
	}

	/**
	 * Returns a bool value for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public boolean getBoolean(String key){
		try{
			return configJSON.getBoolean(key);
		}
		catch(Exception e){
			return false;
		}
	}

	/**
	 * Returns an int array for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public int[] getIntArray(String key){
		try{
			JSONArray jsonArray = configJSON.getJSONArray(key);
			int[] ia = new int[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++){
				ia[i] = jsonArray.getInt(i);
			}
			return ia;
		}
		catch(Exception e){
			return new int[0];
		}
	}

	/**
	 * Returns an long array for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public long[] getLongArray(String key){
		try{
			JSONArray jsonArray = configJSON.getJSONArray(key);
			long[] ia = new long[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++){
				ia[i] = jsonArray.getLong(i);
			}
			return ia;
		}
		catch(Exception e){
			return new long[0];
		}
	}

	/**
	 * Returns an string array for the given key
	 *
	 * @param key key
	 *
	 * @return value
	 */
	public String[] getStringArray(String key){
		try{
			JSONArray jsonArray = configJSON.getJSONArray(key);
			String[] ia = new String[jsonArray.length()];
			for(int i = 0; i < jsonArray.length(); i++){
				ia[i] = jsonArray.getString(i);
			}
			return ia;
		}
		catch(Exception e){
			return new String[0];
		}
	}

}
