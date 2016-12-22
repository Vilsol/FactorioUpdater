package me.vilsol.factorioupdater.ui.templates;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import me.vilsol.factorioupdater.models.Version;
import me.vilsol.factorioupdater.ui.UI;
import me.vilsol.factorioupdater.util.FXUtils;
import me.vilsol.factorioupdater.util.FuturePool;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ModpackListing extends FXMLTemplate<StackPane> {
    
    @FXML private Text packName;
    @FXML private Text packVersion;
    @FXML private GridPane backgroundPane;
    
    public ModpackListing(String packName, Version packVersion, List<String> imageLinks){
        super("modpack-listing");
        
        this.packName.setText(packName);
        this.packVersion.setText(packVersion.toString());
    
        FuturePool<Image> pool = new FuturePool<>();
        imageLinks.forEach(url -> pool.addFuture(FXUtils.getImage(url)));
    
        ImageView loading = new ImageView(new Image("/images/loading-3.gif"));
        loading.setFitHeight(backgroundPane.getPrefWidth());
        loading.setFitHeight(backgroundPane.getPrefHeight());
        loading.setPreserveRatio(true);
        backgroundPane.setAlignment(Pos.CENTER);
        backgroundPane.add(loading, 0, 0);
        
        pool.onComplete(images -> {
    
            try{
                Thread.sleep(5000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
    
            Platform.runLater(() -> {
                AtomicInteger row = new AtomicInteger(0);
                AtomicInteger column = new AtomicInteger(0);
    
                BoxBlur bb = new BoxBlur();
                bb.setWidth(3);
                bb.setHeight(3);
                bb.setIterations(3);
    
                backgroundPane.getChildren().remove(loading);
    
                images.forEach(img -> {
                    ImageView image = new ImageView(img);
                    image.setFitWidth(127);
                    image.setFitHeight(87);
                    image.setEffect(bb);
    
                    backgroundPane.add(image, column.getAndIncrement(), row.get());
        
                    if(column.get() == 2){
                        column.set(0);
                        row.incrementAndGet();
                    }
                });
    
                getPane().getChildren().remove(backgroundPane);
                Scene scene = new Scene(backgroundPane);
                ImageView background = new ImageView(scene.snapshot(null));
                background.setFitWidth(backgroundPane.getPrefWidth());
                background.setFitHeight(backgroundPane.getPrefHeight());
                FXUtils.inset(background, 7, 7, 7, 7);
                FXUtils.round(background, 20, 20, 20, 20);
        
                InnerShadow shade = new InnerShadow(3 , Color.BLACK);
                shade.setInput(new ColorAdjust(0, 0, -0.1, 0));
                DropShadow dropShadow = new DropShadow(10, 3, 3, Color.BLACK);
                dropShadow.setInput(shade);
                background.setEffect(dropShadow);
                
                getPane().getChildren().add(0, background);
            });
            
            return null;
        });
        
        pool.start();
    }
    
    @FXML
    private void onEdit(ActionEvent event){
        UI.showAlert(Alert.AlertType.INFORMATION, null, "You clicked edit!");
    }
    
    @FXML
    private void onPlay(ActionEvent event){
        UI.showAlert(Alert.AlertType.INFORMATION, null, "You clicked play!");
    }
    
}