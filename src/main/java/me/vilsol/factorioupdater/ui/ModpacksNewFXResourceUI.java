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
package me.vilsol.factorioupdater.ui;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import me.vilsol.factorioupdater.managers.ModpackManager;
import me.vilsol.factorioupdater.models.ModPack;
import me.vilsol.factorioupdater.ui.templates.ModpackListing;

import java.util.HashMap;

public class ModpacksNewFXResourceUI {
    
    private static HashMap<ModPack, Pane> listings = new HashMap<>();
    
    public static Tab createModPacksTab(UpdaterUI.Transitioner transition) {
        VBox vbox = new VBox();
        vbox.setSpacing(25);
        vbox.setPadding(new Insets(25));
        vbox.setBackground(new Background(new BackgroundFill(Color.web("#444444"), null, null)));
    
        Text loadingText = new Text("Loading Modpacks...");
        FlowPane packPane = new FlowPane();
        
        vbox.getChildren().add(loadingText);
        vbox.getChildren().add(packPane);
        
        packPane.setHgap(25);
        packPane.setVgap(25);
        
        ModpackManager.getInstance().getModpacks().forEach(pack -> {
            vbox.getChildren().remove(loadingText);
            ModpackListing modpackListing = new ModpackListing(pack);
            packPane.getChildren().add(modpackListing.getPane());
            listings.put(pack, modpackListing.getPane());
        });
    
        ModpackManager.getInstance().getModpacks().addListener((ListChangeListener<ModPack>) c -> Platform.runLater(() -> {
            while(c.next()){
                if(c.wasAdded()){
                    c.getAddedSubList().forEach(pack -> {
                        vbox.getChildren().remove(loadingText);
                        ModpackListing modpackListing = new ModpackListing(pack);
                        packPane.getChildren().add(modpackListing.getPane());
                        listings.put(pack, modpackListing.getPane());
                    });
                }else if(c.wasRemoved()){
                    c.getRemoved().forEach(pack -> {
                        if(listings.containsKey(pack)){
                            packPane.getChildren().remove(listings.get(pack));
                            listings.remove(pack);
                        }
                    });
                }
            }
        }));
        
        Tab tab = new Tab("FX Resource Modpacks", vbox);
        tab.setClosable(false);
        return tab;
    }
    
}
