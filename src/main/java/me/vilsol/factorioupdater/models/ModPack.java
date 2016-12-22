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
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.managers.ModManager;
import me.vilsol.factorioupdater.util.Mappable;
import me.vilsol.factorioupdater.util.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nick Robson
 */
@Data
public class ModPack implements Mappable {

    private final String name;
    private final Version version;
    private final Version factorioVersion;
    private final Map<String, ModWithRelease> mods;

    private final File rootDirectory;
    private final File gameDirectory;
    private final File modsDirectory;
    
    private Tree<ModWithRelease> dependencies = new Tree<>((Tree<ModWithRelease>) null);
    private List<Dependency> missing = new ArrayList<>();

    public ModPack(String name, File directory, Version version, Version factorioVersion, Map<String, ModWithRelease> mods){
        this.name = name;
        this.version = version;
        this.factorioVersion = factorioVersion;
        this.mods = mods;

        this.rootDirectory = directory;
        this.gameDirectory = new File(directory, "factorio");
        this.modsDirectory = new File(gameDirectory, "mods");

        if(!this.modsDirectory.exists()){
            if(!this.modsDirectory.mkdirs()){
                throw new RuntimeException("Failed to create mods directory. Check your permissions for: " + modsDirectory);
            }
        }

        File packJson = new File(directory, "pack.json");

        if(!packJson.exists()){
            try{
                if(!packJson.createNewFile()){
                    throw new RuntimeException("Failed to create pack.json file. Check your permissions for: " + directory);
                }
            }catch(IOException e){
                throw new RuntimeException(e);
            }

            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("version", version.toString());
            data.put("factorio_version", factorioVersion.toString());
            data.put("mods", this.mods.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsJson())));

            try{
                Files.write(packJson.toPath(), data.toString(4).getBytes());
            }catch(IOException e){
                throw new RuntimeException("Failed to write to pack.json file. Check your permissions for: " + directory);
            }
        }
    }
    
    public List<Dependency> resolve(){
        dependencies = new Tree<>((Tree<ModWithRelease>) null);
        missing = new ArrayList<>();
    
        mods.forEach((name, mod) -> {
            ModManager.FetchTreeResult result = ModManager.getInstance().fetchTree(name, mod.getModRelease().getVersion(), "=");
            dependencies.addBranch(result.getResult());
            missing.addAll(result.getMissing());
        });
        
        return missing;
    }

    public String exportPack(){
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("version", version.toString());
        data.put("factorio_version", factorioVersion.toString());
        data.put("mods", this.mods.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsJson())));
        data.put("schema", "v1");

        byte[] json = data.toString().getBytes();
        byte[] deflate = Utils.deflate(json);
        byte[] base64 = Utils.base64Encode(deflate);
        
        return new String(base64);
    }

    public static ModPack importPack(String s, File installDirectory){
        byte[] decoded = Utils.base64Decode(s.getBytes());
        byte[] inflated = Utils.inflate(decoded);
        return fromJSON(new JSONObject(new String(inflated)), installDirectory);
    }
    
    public static ImportedModPack fromJSON(JSONObject json, File installDirectory){
        String schema = json.getString("schema");
    
        Set<String> invalidMods = new HashSet<>();
        Map<Mod, Version> invalidVersions = new HashMap<>();
        Map<String, ModWithRelease> mods = new HashMap<>();
        
        switch(schema){
            case "v1":
                JSONObject modsData = json.getJSONObject("mods");
                for (String name : modsData.keySet()) {
                    JSONObject modData = modsData.getJSONObject(name);
                    String versionString = modData.getString("version");
                    boolean enabled = modData.optBoolean("enabled", true);
                    
                    Mod mod = ModManager.getInstance().fetchMod(name);
                    if (mod == null) {
                        invalidMods.add(name);
                        continue;
                    }
                    
                    Version version = new Version(versionString);
                    ModRelease release = mod.matchRelease("=", version);
                    
                    if (release == null) {
                        invalidVersions.put(mod, version);
                    } else {
                        mods.put(mod.getName(), new ModWithRelease(mod, release, enabled));
                    }
                }
    
                String name = json.getString("name");
                Version version = new Version(json.getString("version"));
                Version factorioVersion = new Version(json.getString("factorio_version"));
                
                return new ImportedModPack(name, installDirectory, version, factorioVersion, mods, invalidMods, invalidVersions);
        }
        
        return null;
    }

    public void download(){
        if(!modsDirectory.exists()){
            if(!modsDirectory.mkdirs()){
                throw new RuntimeException("Failed to create mods directory. Check your permissions for: " + modsDirectory);
            }
        }

        Set<ModWithRelease> toDownload = mods.values().stream().flatMap(mod -> {
            ModRelease release = mod.getModRelease();
            ModManager.FetchTreeResult result = ModManager.getInstance().fetchTree(release.getModName(), release.getVersion(), "=");
            return result.getResult().flattenUnique().stream().map(Tree::getLeaf);
        }).filter(mod -> {
            File downloadTo = new File(modsDirectory, mod.getModRelease().getFileName());
            return !downloadTo.exists();
        }).collect(Collectors.toSet());

        if(toDownload.size() > 0){
            double fileSize = toDownload.stream().mapToLong(m -> m.getModRelease().getFileSize()).sum();
            System.out.println("Need to download " + toDownload.size() + " mods (" + (new DecimalFormat("#.##").format(fileSize / Resource.FILE_SIZE_TO_MEGABYTES)) + " MB)");
            System.out.println();
        } else {
            System.out.println("All set! No mods need to be downloaded!");
        }

        for(ModWithRelease mod : toDownload){
            System.out.println("Downloading " + mod.getModRelease().getModName() + " " + mod.getModRelease().getVersion() + " (" + (new DecimalFormat("#.##").format(mod.getModRelease().getFileSize() / Resource.FILE_SIZE_TO_MEGABYTES)) + " MB)");

            try {
                File downloadTo = new File(modsDirectory, mod.getModRelease().getFileName());
                Utils.download(Resource.URL_FACTORIO_MODS + mod.getModRelease().getDownloadURL(), downloadTo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(modsDirectory.getAbsolutePath());
    }
    
    @Override
    public Map<String, Object> map(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("version", version);
        map.put("factorioVersion", factorioVersion);
        map.put("mods", mods.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().map())));
        map.put("rootDirectory", rootDirectory);
        map.put("gameDirectory", gameDirectory);
        map.put("modsDirectory", modsDirectory);
        return map;
    }
    
}