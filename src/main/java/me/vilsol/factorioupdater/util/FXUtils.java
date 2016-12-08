package me.vilsol.factorioupdater.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

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
    
    public static Image getImage(String url){
        return new ImageView(imageCache.computeIfAbsent(url, Image::new)).snapshot(null, null);
    }
    
}
