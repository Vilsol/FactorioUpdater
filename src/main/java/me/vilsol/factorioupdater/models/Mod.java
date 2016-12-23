/*
 * FactorioUpdater - The best factorio mod manager
 * Copyright 2016 The FactorioUpdater Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.vilsol.factorioupdater.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.vilsol.factorioupdater.util.Mappable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"data"})
@ToString(exclude = {"creationDate", "updateDate", "data"})
public class Mod implements Mappable {

    private final int id;
    private final String name;
    private final String owner;
    private final String title;
    private final String summary;
    private final Calendar creationDate;
    private final Calendar updateDate;
    private final SortedSet<ModRelease> releases;
    private final JSONObject data;
    private final String avatar;
    private final String homepage;
    private final int downloadCount;
    
    public Mod(JSONObject data){
        this.data = data;
        this.id = data.getInt("id");
        this.name = data.getString("name");
        this.owner = data.getString("owner");
        this.title = data.getString("title");
        this.summary = data.getString("summary");
        this.homepage = data.getString("homepage");
        this.downloadCount = data.getInt("downloads_count");

        try{
            this.creationDate = DatatypeConverter.parseDateTime(data.getString("created_at").replace(' ', 'T'));
        }catch(NumberFormatException e){
            throw new RuntimeException("Could not parse " + data.getString("created_at"), e);
        }

        try{
            this.updateDate = DatatypeConverter.parseDateTime(data.getString("updated_at").replace(' ', 'T'));
        }catch(NumberFormatException e){
            throw new RuntimeException("Could not parse " + data.getString("updated_at"), e);
        }

        this.releases = new TreeSet<>();
        JSONArray releases = data.getJSONArray("releases");
        for(int i = 0; i < releases.length(); i++){
            this.releases.add(new ModRelease(releases.getJSONObject(i)));
        }

        String avatar = null;
        if(data.has("media_files")){
            JSONArray arr = data.getJSONArray("media_files");
            if (arr.length() > 0){
                avatar = arr.getJSONObject(0).getJSONObject("urls").getString("original");
            }
        }
        this.avatar = avatar;
    }
    
    public ModRelease matchRelease(String comparison, Version version){
        if(comparison.isEmpty()){
            return releases.first();
        }

        for(ModRelease release : releases){
            if(release.getVersion().matches(comparison, version)){
                return release;
            }
        }
        
        return null;
    }
    
    @Override
    public Map<String, Object> map(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("owner", owner);
        map.put("title", title);
        map.put("summary", summary);
        map.put("creationDate", creationDate);
        map.put("updateDate", updateDate);
        map.put("releases", releases.stream().map(ModRelease::map).collect(Collectors.toList()));
        map.put("avatar", avatar);
        map.put("homepage", homepage);
        return map;
    }
    
    public int getDownloadCount(){
        return downloadCount;
    }
}
