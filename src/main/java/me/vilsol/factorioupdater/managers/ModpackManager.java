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
package me.vilsol.factorioupdater.managers;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.ImportedModPack;
import me.vilsol.factorioupdater.models.Mod;
import me.vilsol.factorioupdater.models.ModPack;
import me.vilsol.factorioupdater.models.Version;
import me.vilsol.factorioupdater.ui.UI;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ModpackManager {
    
    private final static ModpackManager ourInstance = new ModpackManager();
    
    private final ObservableList<ModPack> modpacks = new ObservableListWrapper<>(new CopyOnWriteArrayList<>());
    
    private ModpackManager(){
        loadPacks();
    }
    
    private void loadPacks(){
        File packsDirectory = new File(Resource.APP_HOME_DIR, "modpacks");
        if (!packsDirectory.exists() && !packsDirectory.mkdir()) {
            UI.showAlert(Alert.AlertType.ERROR, null, "Failed to create modpacks directory.");
        }
    
        Thread thread = new Thread(() -> {
            if (!packsDirectory.isDirectory()) {
                return;
            }
    
            for (File packDir : packsDirectory.listFiles(f -> f.isDirectory())) {
                try {
                    ModPack pack = loadModPack(packDir);
                    modpacks.add(pack);
                    
                    if(pack.getMissing().size() > 0){
                        System.out.println("Loaded modpack: " + packDir.getName() + " (" + pack.getMods().size() + " -> " + pack.getDependencies().size() + ", missing " + pack.getMissing().size() + ")");
                    }else{
                        System.out.println("Loaded modpack: " + packDir.getName() + " (" + pack.getMods().size() + " -> " + pack.getDependencies().size() + ")");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
        }, "Modpack Loader");
        
        thread.setDaemon(true);
        thread.start();
    }
    
    private static ModPack loadModPack(File packDir) {
        if(!packDir.isDirectory()){
            System.err.format("%s is not a directory, assuming not a modpack instance\n", packDir);
            return null;
        }
        
        if(packDir.listFiles() == null || packDir.listFiles().length == 0){
            System.err.format("No files in %s, assuming not a modpack instance\n", packDir);
            return null;
        }
        
        Map<String, File> files = Arrays.stream(packDir.listFiles()).collect(Collectors.toMap(File::getName, f -> f));
        
        File packJson = files.get("pack.json");
        if (packJson == null) {
            System.err.format("No pack.json in %s, assuming not a modpack instance\n", packDir);
            return null;
        }
        
        if (!packJson.isFile() || !packJson.canRead()) {
            UI.showAlert(Alert.AlertType.ERROR, null, "%s is not a file, or cannot be read!", packJson);
            return null;
        }
        
        JSONObject rootJson;
        try {
            List<String> lines = Files.readAllLines(packJson.toPath()).stream().map(String::trim).collect(Collectors.toList());
            rootJson = new JSONObject(String.join("", lines));
        } catch (IOException e) {
            UI.showAlert(Alert.AlertType.ERROR, null, "Failed to parse JSON in %s!", packJson);
            e.printStackTrace();
            return null;
        }
    
        ImportedModPack pack = ModPack.fromJSON(rootJson, packDir);
    
        Set<String> invalidMods = pack.getInvalidMods();
        Map<Mod, Version> invalidVersions = pack.getInvalidVersions();
        
        String reportString = "";
        if (!invalidMods.isEmpty()) {
            reportString += "There are no mods with these names:\n\t";
            reportString += String.join("\n\t", invalidMods);
        }
        
        if (!invalidVersions.isEmpty()) {
            if (!reportString.isEmpty()) {
                reportString += "\n\n";
            }
            
            reportString += "There are no releases matching these versions:\n\t";
            reportString += String.join("\n\t", invalidVersions.entrySet().stream().map(e -> e.getKey().getName() + " @ " + e.getValue().toString()).collect(Collectors.toList()));
        }
        
        if (!reportString.isEmpty()) {
            UI.showAlert(Alert.AlertType.WARNING, null, reportString);
        }
    
        pack.resolve();
        pack.download();
    
        return pack;
    }
    
    public static ModpackManager getInstance(){
        return ourInstance;
    }
    
    public ObservableList<ModPack> getModpacks(){
        return modpacks;
    }
    
}
