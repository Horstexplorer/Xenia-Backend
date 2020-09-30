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

package de.netbeacon.xenia.backend.security;

import de.netbeacon.utils.ratelimiter.RateLimiter;
import de.netbeacon.utils.shutdownhook.IShutdown;
import de.netbeacon.xenia.backend.clients.ClientManager;
import de.netbeacon.xenia.backend.clients.objects.Client;
import io.javalin.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SecurityManager implements IShutdown {

    private final File file;
    private final ClientManager clientManager;

    private final HashSet<String> blockedIPs = new HashSet<>();
    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    public SecurityManager(ClientManager clientManager, File data){
        this.clientManager = clientManager;
        this.file = data;
    }


    public Client authorizeConnection(SecuritySettings securitySettings, Context ctx){
        try{
            // check ip block
            if(blockedIPs.contains(ctx.ip())){
                throw new ForbiddenResponse();
            }
            // check auth if specified
            Client client = null;
            if(ctx.header("userid") != null && ctx.header("token") != null){
                if(securitySettings.getRequiredAuthType() == SecuritySettings.AuthType.Basic){
                    throw new UnauthorizedResponse(); // wrong auth method
                }
                // get client id & token
                long clientId;
                try{ clientId = Long.parseLong(ctx.header("userid")); }
                catch (Exception e){ throw new BadRequestResponse(); }
                String token = ctx.header("token");
                // get client & verify token
                client = clientManager.getClient(clientId);
                if(client == null || !client.getClientAuth().verifyToken(token)){
                    throw new UnauthorizedResponse();
                }
            }else if(ctx.basicAuthCredentialsExist()){
                if(securitySettings.getRequiredAuthType() == SecuritySettings.AuthType.Token){
                    throw new UnauthorizedResponse(); // wrong auth method
                }
                // get clientId & pass
                long clientId;
                try{ clientId = Long.parseLong(ctx.basicAuthCredentials().getUsername()); }
                catch (Exception e){ throw new BadRequestResponse(); }
                String password = ctx.basicAuthCredentials().getPassword();
                // get client & verify passwd
                client = clientManager.getClient(clientId);
                if(client == null || !client.getClientAuth().verifyPassword(password)){
                    throw new UnauthorizedResponse();
                }
            }else {
                // no auth specified
                if(securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional){
                    throw new ForbiddenResponse();
                }
            }
            // check client type
            if(securitySettings.getRequiredClientType() != SecuritySettings.ClientType.Any && securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional && client != null){
                if(client.getClientType() != securitySettings.getRequiredClientType()){
                    throw new ForbiddenResponse();
                }
            }
            return client;
        }catch (HttpResponseException e){
            if(!rateLimiterMap.containsKey(ctx.ip())){
                RateLimiter rateLimiter = new RateLimiter(TimeUnit.MINUTES, 5);
                rateLimiter.setMaxUsages(35);
                rateLimiterMap.put(ctx.ip(), rateLimiter);
            }
            if(!rateLimiterMap.get(ctx.ip()).takeNice()){
                throw new HttpResponseException(429, "Too Many Requests", new HashMap<>());
            }
            throw e;
        }
    }


    public SecurityManager loadFromFile() throws IOException {
        String contentS = new String(Files.readAllBytes(file.toPath()));
        if(contentS.isBlank()){
            return this;
        }
        JSONObject content = new JSONObject(contentS);
        // get blocked ips
        JSONArray bip = content.getJSONArray("blockedIPs");
        for(int i = 0; i < bip.length(); i++)
            blockedIPs.add(bip.getString(i));

        return this;
    }

    public SecurityManager writeToFile() throws IOException {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))){
            JSONObject jsonObject = new JSONObject().put("blockedIPS", blockedIPs);
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        return this;
    }



    @Override
    public void onShutdown() throws Exception {
        writeToFile();
    }
}
