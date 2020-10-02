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
import io.javalin.websocket.WsContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(SecurityManager.class);

    public SecurityManager(ClientManager clientManager, File data){
        this.clientManager = clientManager;
        this.file = data;
    }


    public Client authorizeConnection(SecuritySettings securitySettings, Context ctx){
        // check if running behind proxy and we should use the sent ip
        String clientIP = ctx.header("X-Real-IP");
        if(clientIP == null || clientIP.isBlank()){
            clientIP = ctx.ip(); // use the default one
        }
        try{
            // check ip block
            if(blockedIPs.contains(clientIP)){
                throw new ForbiddenResponse();
            }
            // check auth if specified
            Client client = null;
            if(ctx.header("userid") != null && ctx.header("token") != null){
                if(securitySettings.getRequiredAuthType() == SecuritySettings.AuthType.Basic){
                    logger.warn("Client At "+clientIP+" Specified The Wrong Auth Method (Req: Basic; Has: Token) For Path "+ctx.path());
                    throw new UnauthorizedResponse(); // wrong auth method
                }
                // get client id & token
                long clientId;
                try{ clientId = Long.parseLong(ctx.header("userid")); }
                catch (Exception e){
                    logger.warn("Client At "+clientIP+" Specified Bad ClientID For Path "+ctx.path());
                    throw new BadRequestResponse();
                }
                String token = ctx.header("token");
                // get client & verify token
                client = clientManager.getClient(clientId);
                if(client == null || !client.getClientAuth().verifyToken(token)){
                    logger.warn("Client At "+clientIP+" Failed Auth For Path "+ctx.path());
                    throw new UnauthorizedResponse();
                }
            }else if(ctx.basicAuthCredentialsExist()){
                if(securitySettings.getRequiredAuthType() == SecuritySettings.AuthType.Token){
                    logger.warn("Client At "+clientIP+" Specified The Wrong Auth Method (Req: Token; Has: Basic) For Path "+ctx.path());
                    throw new UnauthorizedResponse(); // wrong auth method
                }
                // get clientId & pass
                long clientId;
                try{ clientId = Long.parseLong(ctx.basicAuthCredentials().getUsername()); }
                catch (Exception e){
                    logger.warn("Client At "+clientIP+" Specified Bad ClientID For Path "+ctx.path());
                    throw new BadRequestResponse();
                }
                String password = ctx.basicAuthCredentials().getPassword();
                // get client & verify passwd
                client = clientManager.getClient(clientId);
                if(client == null || !client.getClientAuth().verifyPassword(password)){
                    logger.warn("Client At "+clientIP+" Failed Auth For Path "+ctx.path());
                    throw new UnauthorizedResponse();
                }
            }else {
                // no auth specified
                if(securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional){
                    logger.warn("Client At "+clientIP+" Specified No Auth Which Is Required For The Requested Path "+ctx.path());
                    throw new ForbiddenResponse();
                }
            }
            // check client type
            if(securitySettings.getRequiredClientType() != SecuritySettings.ClientType.Any && securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional && client != null){
                if(client.getClientType() != securitySettings.getRequiredClientType()){
                    logger.warn("Client At "+clientIP+" Logged In Successful But Does Not Match The Required Client Type (Req: "+securitySettings.getRequiredClientType()+"; Has: "+client.getClientType()+")");
                    throw new ForbiddenResponse();
                }
            }
            return client;
        }catch (HttpResponseException e){
            if(!rateLimiterMap.containsKey(clientIP)){
                RateLimiter rateLimiter = new RateLimiter(TimeUnit.MINUTES, 5);
                rateLimiter.setMaxUsages(35);
                rateLimiterMap.put(clientIP, rateLimiter);
            }
            if(!rateLimiterMap.get(clientIP).takeNice()){
                logger.warn("Client At "+clientIP+" Hit The RateLimit");
                throw new HttpResponseException(429, "Too Many Requests", new HashMap<>());
            }
            throw e;
        }
    }

    public Client authorizeWsConnection(SecuritySettings securitySettings, WsContext ctx){
        // check if running behind proxy and we should use the sent ip
        String clientIP = ctx.header("X-Real-IP");
        if(clientIP == null || clientIP.isBlank()){
            clientIP = ctx.host(); // use the default one
        }
        try{
            // check ip block
            if(blockedIPs.contains(clientIP)){
                throw new ForbiddenResponse();
            }
            // check auth if specified
            Client client = null;
            if(ctx.header("userid") != null && ctx.header("token") != null){
                if(securitySettings.getRequiredAuthType() == SecuritySettings.AuthType.Basic){
                    logger.warn("Client At "+clientIP+" Specified The Wrong Auth Method (Req: Basic; Has: Token) For Path "+ctx.matchedPath());
                    throw new UnauthorizedResponse(); // wrong auth method
                }
                // get client id & token
                long clientId;
                try{ clientId = Long.parseLong(ctx.header("userid")); }
                catch (Exception e){
                    logger.warn("Client At "+clientIP+" Specified Bad ClientID For Path "+ctx.matchedPath());
                    throw new BadRequestResponse();
                }
                String token = ctx.header("token");
                // get client & verify token
                client = clientManager.getClient(clientId);
                if(client == null || !client.getClientAuth().verifyToken(token)){
                    logger.warn("Client At "+clientIP+" Failed Auth For Path "+ctx.matchedPath());
                    throw new UnauthorizedResponse();
                }
            }else {
                // no auth specified
                if(securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional){
                    logger.warn("Client At "+clientIP+" Specified No Auth Which Is Required For The Requested Path "+ctx.matchedPath());
                    throw new ForbiddenResponse();
                }
            }
            // check client type
            if(securitySettings.getRequiredClientType() != SecuritySettings.ClientType.Any && securitySettings.getRequiredAuthType() != SecuritySettings.AuthType.Optional && client != null){
                if(client.getClientType() != securitySettings.getRequiredClientType()){
                    logger.warn("Client At "+clientIP+" Logged In Successful But Does Not Match The Required Client Type (Req: "+securitySettings.getRequiredClientType()+"; Has: "+client.getClientType()+")");
                    throw new ForbiddenResponse();
                }
            }
            return client;
        }catch (HttpResponseException e){
            if(!rateLimiterMap.containsKey(clientIP)){
                RateLimiter rateLimiter = new RateLimiter(TimeUnit.MINUTES, 5);
                rateLimiter.setMaxUsages(35);
                rateLimiterMap.put(clientIP, rateLimiter);
            }
            if(!rateLimiterMap.get(clientIP).takeNice()){
                logger.warn("Client At "+clientIP+" Hit The RateLimit");
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
            JSONObject jsonObject = new JSONObject().put("blockedIPs", blockedIPs);
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
