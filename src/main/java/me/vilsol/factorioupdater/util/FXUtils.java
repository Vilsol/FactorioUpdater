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
package me.vilsol.factorioupdater.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class FXUtils {
    
    private static final HashMap<String, Image> imageCache = new HashMap<>();
    
    public static void round(ImageView image, int topLeft, int topRight, int bottomLeft, int bottomRight){
        double x = image.getX();
        double y = image.getY();
        
        if(topLeft > 0){
            image.setX(0);
            image.setY(0);
            
            Rectangle clip = new Rectangle(image.getFitWidth() + topLeft, image.getFitHeight() + topLeft);
            clip.setArcWidth(topLeft);
            clip.setArcHeight(topLeft);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(topRight > 0){
            image.setX(topRight);
            image.setY(0);
            
            Rectangle clip = new Rectangle(image.getFitWidth() + topRight, image.getFitHeight() + topRight);
            clip.setArcWidth(topRight);
            clip.setArcHeight(topRight);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(bottomLeft > 0){
            image.setX(0);
            image.setY(bottomLeft);
            
            Rectangle clip = new Rectangle(image.getFitWidth() + bottomLeft, image.getFitHeight() + bottomLeft);
            clip.setArcWidth(bottomLeft);
            clip.setArcHeight(bottomLeft);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(bottomRight > 0){
            image.setX(bottomRight);
            image.setY(bottomRight);
            
            Rectangle clip = new Rectangle(image.getFitWidth() + bottomRight, image.getFitHeight() + bottomRight);
            clip.setArcWidth(bottomRight);
            clip.setArcHeight(bottomRight);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        image.setX(x);
        image.setY(y);
    }
    
    public static void inset(ImageView image, int top, int right, int bottom, int left){
        double x = image.getX();
        double y = image.getY();
    
        Rectangle clip = new Rectangle(image.getFitWidth(), image.getFitHeight());
        
        if(top > 0){
            image.setX(0);
            image.setY(-top);
            
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(right > 0){
            image.setX(right);
            image.setY(0);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(bottom > 0){
            image.setX(0);
            image.setY(bottom);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        if(left > 0){
            image.setX(-left);
            image.setY(0);
            image.setClip(clip);
            
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            
            image.setImage(image.snapshot(parameters, null));
            image.setClip(null);
        }
        
        image.setX(x);
        image.setY(y);
    }
    
    public static CompletableFuture<Image> getImage(String url){
        CompletableFuture<Image> future = new CompletableFuture<>();
        
        Image image = imageCache.computeIfAbsent(url, u -> new Image(u, true));
        image.progressProperty().addListener((observable, oldValue, newValue) -> {
            if((Double) newValue == 1.0){
                future.complete(new ImageView(image).snapshot(null, null));
            }
        });
        
        return future;
    }
    
}
