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
import me.vilsol.factorioupdater.util.Mappable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"json"})
public class ModRelease implements Comparable<ModRelease>, Mappable {

    private final Version version;
    private final Version factorioVersion;
    private final Calendar releaseDate;
    private final String downloadURL;
    private final String fileName;
    private final long fileSize;
    private final List<Dependency> dependencies;
    private final JSONObject json;
    private final String modName;
    private final String modAuthor;
    private final String modTitle;
    private final String modDescription;

    public ModRelease(JSONObject data){
        this.json = data;
        this.version = new Version(data.getString("version"));
        this.factorioVersion = new Version(data.getString("factorio_version"));

        try{
            this.releaseDate = DatatypeConverter.parseDateTime(data.getString("released_at"));
        }catch(NumberFormatException e){
            throw new RuntimeException("Could not parse " + data.getString("released_at"), e);
        }

        this.downloadURL = data.getString("download_url");
        this.fileName = data.getString("file_name");
        this.fileSize = data.getLong("file_size");

        JSONObject infoJson = data.getJSONObject("info_json");
        this.modName = infoJson.getString("name");
        this.modAuthor = infoJson.getString("author");
        this.modTitle = infoJson.getString("name");
        this.modDescription = infoJson.getString("author");

        this.dependencies = new ArrayList<>();
    
        if(data.getJSONObject("info_json").has("dependencies")){
            Object dependencies = data.getJSONObject("info_json").get("dependencies");
            if (dependencies instanceof JSONArray) {
                JSONArray arr = (JSONArray) dependencies;
                for(int i = 0; i < arr.length(); i++){
                    this.dependencies.add(new Dependency(arr.getString(i)));
                }
            } else if (dependencies instanceof String) {
                this.dependencies.add(new Dependency((String) dependencies));
            } else {
                throw new IllegalStateException("dependencies field is not a string or array: is " + (dependencies != null ? dependencies.getClass().getSimpleName() + "@( " + dependencies + " )" : "null"));
            }
        }
    }
    
    @Override
    public int compareTo(ModRelease o){
        return o.getVersion().compareTo(version);
    }

    @Override
    public String toString() {
        return String.format("ModRelease(%s @ %s, -> %s)", modName, version, dependencies);
    }
    
    @Override
    public Map<String, Object> map(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("version", version);
        map.put("factorioVersion", factorioVersion);
        map.put("releaseDate", releaseDate);
        map.put("downloadURL", downloadURL);
        map.put("fileName", fileName);
        map.put("fileSize", fileSize);
        map.put("dependencies", dependencies.stream().map(Dependency::map).collect(Collectors.toList()));
        map.put("modName", modName);
        map.put("modAuthor", modAuthor);
        map.put("modTitle", modTitle);
        map.put("modDescription", modDescription);
        return map;
    }
    
}
