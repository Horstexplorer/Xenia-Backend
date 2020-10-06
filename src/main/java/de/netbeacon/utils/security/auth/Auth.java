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

package de.netbeacon.utils.security.auth;

import de.netbeacon.utils.json.serial.IJSONSerializable;
import de.netbeacon.utils.json.serial.JSONSerializationException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Easy handling of login auth
 *
 * @author horstexplorer
 */
public class Auth implements IJSONSerializable {

    private String passwordHash;
    private String tokenHash;
    private long lastTokenUse;

    /**
     * Creates a new instance of this class
     */
    public Auth(){}

    /**
     * Creates a new instance of this class
     *
     * @param jsonObject serialized copy of the object
     */
    public Auth(JSONObject jsonObject){
        if(jsonObject.has("passwordHash")){
            passwordHash = jsonObject.getString("passwordHash");
        }
        if(jsonObject.has("tokenHash") && jsonObject.has("lastTokenUse")){
            tokenHash = jsonObject.getString("tokenHash");
            lastTokenUse = jsonObject.getLong("lastTokenUse");
        }
    }

    /**
     * Set the password to a given value
     *
     * @param password new password
     */
    public void setPassword(String password){
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Check if a password matches the stored hash
     *
     * @param password to test
     * @return true if matches
     */
    public boolean verifyPassword(String password){
        if(passwordHash != null && !passwordHash.isBlank()){
            return BCrypt.checkpw(password, passwordHash);
        }
        return false;
    }

    /**
     * Returns an login token which invalidates after 1h of not using it
     *
     * @return login token
     */
    public String getToken(){
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES*2);
        byteBuffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        String token = Base64.getEncoder().encodeToString(byteBuffer.array());
        this.tokenHash = BCrypt.hashpw(token, BCrypt.gensalt(4));
        this.lastTokenUse = System.currentTimeMillis();
        return token;
    }

    /**
     * Verifies a given login token
     *
     * @param token login token
     * @return true if it is valid
     */
    public boolean verifyToken(String token){
        if(tokenHash != null && !tokenHash.isBlank() && (lastTokenUse+3600000) > System.currentTimeMillis()){
            boolean valid = BCrypt.checkpw(token, tokenHash);
            if(valid){
                lastTokenUse = System.currentTimeMillis();
            }
            return valid;
        }
        return false;
    }

    @Override
    public JSONObject asJSON(){
        return new JSONObject().put("passwordHash", passwordHash).put("tokenHash", tokenHash).put("lastTokenUse", lastTokenUse);
    }

    @Override
    public void fromJSON(JSONObject jsonObject) throws JSONSerializationException {
        if(jsonObject.has("passwordHash")){
            passwordHash = jsonObject.getString("passwordHash");
        }
        if(jsonObject.has("tokenHash") && jsonObject.has("lastTokenUse")){
            tokenHash = jsonObject.getString("tokenHash");
            lastTokenUse = jsonObject.getLong("lastTokenUse");
        }
    }
}
