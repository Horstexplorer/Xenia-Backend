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

package de.netbeacon.xenia.backend.utils.twitch;

import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TwitchWrapQOL {

    private static final ReentrantLock lock = new ReentrantLock();

    public static UserResponse getUserOf(String channelName, TwitchWrap twitchWrap){
        try{
            lock.lock();
            JSONObject response = twitchWrap.request("https://api.twitch.tv/helix/users?login="+channelName);
            JSONArray data = response.getJSONArray("data");
            if(data.isEmpty()){
                throw new RuntimeException("Data Is Empty");
            }
            return new UserResponse(data.getJSONObject(0));
        }finally {
            lock.unlock();
        }
    }

    public static List<UserResponse> getUserOf(Set<String> channelNames, TwitchWrap twitchWrap){
        try{
            lock.lock();
            List<UserResponse> results = new ArrayList<>();
            List<List<String>> rightSizedLists = ListUtils.partition(new ArrayList<>(channelNames), 100);
            for(var sublist : rightSizedLists){
                try{
                    var query = sublist.stream().map(user -> "login=" + user).collect(Collectors.joining("&"));
                    JSONObject response = twitchWrap.request("https://api.twitch.tv/helix/users?"+query);
                    JSONArray data = response.getJSONArray("data");
                    for(int i = 0; i < data.length(); i++){
                        try{
                            results.add(new UserResponse(data.getJSONObject(i)));
                        }catch (Exception ignore){}
                    }
                }catch (Exception ignore){}
            }
            return results;
        }finally {
            lock.unlock();
        }
    }

    public static StreamResponse getStreamOf(Long channelId, TwitchWrap twitchWrap){
        try{
            lock.lock();
            JSONObject response = twitchWrap.request("https://api.twitch.tv/helix/streams?user_id="+channelId);
            JSONArray data = response.getJSONArray("data");
            if(data.isEmpty()) return null;
            return new StreamResponse(data.getJSONObject(0));
        }finally {
            lock.unlock();
        }
    }

    public static List<StreamResponse> getStreamOf(Set<Long> channelIds, TwitchWrap twitchWrap){
        try{
            lock.lock();
            List<StreamResponse> results = new ArrayList<>();
            List<List<Long>> rightSizedLists = ListUtils.partition(new ArrayList<>(channelIds), 100);
            for(var sublist : rightSizedLists){
                try{
                    var query = sublist.stream().map(user -> "user_id=" + user).collect(Collectors.joining("&"));
                    JSONObject response = twitchWrap.request("https://api.twitch.tv/helix/streams?"+query);
                    JSONArray data = response.getJSONArray("data");
                    for(int i = 0; i < data.length(); i++){
                        try{
                            results.add(new StreamResponse(data.getJSONObject(i)));
                        }catch (Exception ignore){}
                    }
                }catch (Exception ignore){}
            }
            return results;
        }finally {
            lock.unlock();
        }
    }

    public static class UserResponse{

        private final long userID;
        private final String userName;
        private final String displayName;

        public UserResponse(JSONObject jsonObject){
            this.userID = jsonObject.has("id") ? Long.parseLong(jsonObject.getString("id")) : -1;
            this.userName = jsonObject.has("login") ? jsonObject.getString("login") : "-- No User --";
            this.displayName = jsonObject.has("display_name") ? jsonObject.getString("display_name") : "-- No Display --";
        }

        public long getUserID() {
            return userID;
        }

        public String getUserName() {
            return userName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class StreamResponse{

        private final long channelId;
        private final String channelName;
        private final String streamTitle;
        private final String game;
        private final boolean isOnline;

        public StreamResponse(JSONObject response){
            channelId = response.has("user_id") ? Long.parseLong(response.getString("user_id")) : -1;
            channelName = response.has("user_name") ? response.getString("user_name") : "-- No User --";
            isOnline = response.has("type") && response.getString("type").equalsIgnoreCase("live");
            streamTitle = response.has("title") ? response.getString("title") : "-- No Title --";
            game = response.has("game_name") ? response.getString("game_name") : "-- No Game --";
        }

        public long getChannelId() {
            return channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public String getStreamTitle() {
            return streamTitle;
        }

        public String getGame() {
            return game;
        }

        public boolean isLive() {
            return isOnline;
        }
    }
}
