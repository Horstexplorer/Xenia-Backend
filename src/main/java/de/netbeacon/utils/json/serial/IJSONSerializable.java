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

package de.netbeacon.utils.json.serial;

import org.json.JSONObject;

/**
 * Should be added to objects for easy serialization to and from json objects
 *
 * @author horstexplorer
 */
public interface IJSONSerializable{

	/**
	 * Returns a serialized copy of the object as json
	 *
	 * @return JSONObject as serialized copy
	 *
	 * @throws JSONSerializationException on exception
	 */
	public default JSONObject asJSON() throws JSONSerializationException{ return null; }

	/**
	 * Sets up an object from a given serialized copy
	 *
	 * @param jsonObject as serialized copy
	 *
	 * @throws JSONSerializationException on exception
	 */
	public default void fromJSON(JSONObject jsonObject) throws JSONSerializationException{}

	;

}
