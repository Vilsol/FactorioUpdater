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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.ModPack;
import me.vilsol.factorioupdater.util.FXUtils;
import org.json.JSONObject;

import java.io.File;

public class ModpacksNewFXUI {
    
    private static final FlowPane packPane = new FlowPane();

    public static Tab createModPacksTab(UpdaterUI.Transitioner transition) {
        VBox vbox = new VBox();
        vbox.setSpacing(25);
        vbox.setPadding(new Insets(25));
        vbox.getChildren().add(packPane);
        
        vbox.setBackground(new Background(new BackgroundFill(Color.web("#444444"), null, null)));
        
        packPane.setHgap(25);
        packPane.setVgap(25);
        
        JSONObject json = new JSONObject("{\"schema\":\"v1\",\"mods\":{\"foo_bar\":{\"version\":\"1.0.0\",\"enabled\":true}},\"name\":\"Test Pack\",\"version\":\"1.2.3\",\"factorio_version\":\"0.14.20\"}");
        ModPack test_pack = ModPack.fromJSON(json, new File(Resource.APP_MODPACK_DIR, "test_pack"));
    
        addPack(test_pack);
        addPack(test_pack);
        addPack(test_pack);
        addPack(test_pack);
        addPack(test_pack);
        addPack(test_pack);
        
        Tab tab = new Tab("FX Modpacks", vbox);
        tab.setClosable(false);
        return tab;
    }
    
    public static void addPack(ModPack pack){
        Platform.runLater(() -> packPane.getChildren().add(createPack(pack)));
    }
    
    public static Node createPack(ModPack pack){
        GridPane pane = new GridPane();
        pane.setVgap(0);
        pane.setHgap(0);
        pane.setVgap(-2);
        pane.setHgap(-2);
        pane.setMaxSize(250, 170);
    
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.2);
    
        BoxBlur bb = new BoxBlur();
        bb.setWidth(3);
        bb.setHeight(3);
        bb.setIterations(3);
    
        ImageView topLeft = new ImageView(FXUtils.getImage("https://mods-data.factorio.com/pub_data/media_files/D2vLWKD4FByu.thumb.png"));
        topLeft.setFitWidth(127);
        topLeft.setFitHeight(87);
        topLeft.setEffect(bb);
    
        ImageView topRight = new ImageView(FXUtils.getImage("https://mods-data.factorio.com/pub_data/media_files/GsfUZAF1WWN6.thumb.png"));
        topRight.setFitWidth(127);
        topRight.setFitHeight(87);
        topRight.setEffect(bb);
    
        ImageView bottomLeft = new ImageView(FXUtils.getImage("https://mods-data.factorio.com/pub_data/media_files/IePuQ7iioYxE.thumb.png"));
        bottomLeft.setFitWidth(127);
        bottomLeft.setFitHeight(87);
        bottomLeft.setEffect(bb);
    
        ImageView bottomRight = new ImageView(FXUtils.getImage("https://mods-data.factorio.com/pub_data/media_files/ymbteZEPDCzQ.thumb.png"));
        bottomRight.setFitWidth(127);
        bottomRight.setFitHeight(87);
        bottomRight.setEffect(bb);
    
        pane.add(topLeft, 0, 0);
        pane.add(topRight, 1, 0);
        pane.add(bottomLeft, 0, 1);
        pane.add(bottomRight, 1, 1);
        
        ImageView background = new ImageView(pane.snapshot(new SnapshotParameters(), null));
        background.setFitWidth(250);
        background.setFitHeight(170);
        FXUtils.inset(background, 7, 7, 7, 7);
        FXUtils.round(background, 20, 20, 20, 20);
        
        InnerShadow shade = new InnerShadow(3 ,Color.BLACK);
        shade.setInput(new ColorAdjust(0, 0, -0.1, 0));
        DropShadow dropShadow = new DropShadow(10, 3, 3, Color.BLACK);
        dropShadow.setInput(shade);
        background.setEffect(dropShadow);
    
    
        Text packName = new Text(pack.getName());
        packName.setFont(Font.font("Arial", 21));
        packName.setFill(Color.WHITE);
    
        Text packVersion = new Text(pack.getVersion().toString());
        packVersion.setFont(Font.font("Arial", 16));
        packVersion.setFill(Color.WHITE);
    
        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(packName);
        centerBox.getChildren().add(packVersion);
    
        HBox bottomButtons = new HBox(159);
        bottomButtons.setFillHeight(true);
        bottomButtons.setAlignment(Pos.BOTTOM_CENTER);
        bottomButtons.setEffect(new InnerShadow(5, 0, -2, Color.BLACK));
    
        Button edit = new Button("Edit");
        edit.setAlignment(Pos.BOTTOM_LEFT);
        edit.setTextFill(Color.WHITE);
        edit.setPadding(new Insets(6, 12, 6, 12));
        edit.setEffect(new InnerShadow(1, 0, 1, new Color(0.5098, 0.5098, 0.5098, 0.45)));
        edit.setStyle("-fx-border-radius: 0 10 0 10;" +
                "-fx-background-radius: 0 10 0 10;" +
                "-fx-border-width: 0px;" +
                "-fx-border-style: none;" +
                "-fx-background-color: #444444;" +
                "-fx-cursor: hand");
        
        Button play = new Button("Play");
        play.setAlignment(Pos.BOTTOM_RIGHT);
        play.setTextFill(Color.WHITE);
        play.setPadding(new Insets(6, 12, 6, 12));
        play.setEffect(new InnerShadow(1, 0, 1, new Color(0.5098, 0.5098, 0.5098, 0.45)));
        play.setStyle("-fx-border-radius: 10 0 10 0;" +
                "-fx-background-radius: 10 0 10 0;" +
                "-fx-border-width: 0px;" +
                "-fx-border-style: none;" +
                "-fx-background-color: #444444;" +
                "-fx-cursor: hand");
    
        bottomButtons.getChildren().add(edit);
        bottomButtons.getChildren().add(play);
    
        StackPane stackPane = new StackPane(background, centerBox, bottomButtons);
    
        return stackPane;
    }
    
}
