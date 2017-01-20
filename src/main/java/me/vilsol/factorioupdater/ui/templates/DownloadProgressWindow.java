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
package me.vilsol.factorioupdater.ui.templates;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import me.vilsol.factorioupdater.util.DownloadPool;
import me.vilsol.factorioupdater.util.Utils;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class DownloadProgressWindow extends FXMLTemplate<VBox> {
    
    @FXML private Text downloadAll;
    @FXML private Text downloadCurrent;
    @FXML private Text downloadSpeed;
    
    @FXML private ProgressBar progressCurrent;
    @FXML private Text currentText;
    
    @FXML private ProgressBar progressTotal;
    @FXML private Text totalText;
    
    private final Stage stage;
    private DownloadPool downloadPool;
    
    public DownloadProgressWindow(){
        super("download-progress-window");
    
        stage = new Stage();
        stage.setTitle("Downloading modpack...");
        stage.setScene(new Scene(getPane(), getPane().getPrefWidth(), getPane().getPrefHeight()));
        stage.setResizable(false);
        stage.show();
        
        downloadAll.setText("");
        downloadCurrent.setText("Initializing");
    
        progressCurrent.setProgress(0);
        currentText.setText("0%");
    
        progressTotal.setProgress(0);
        totalText.setText("0%");
    }
    
    public void setDownloadPool(DownloadPool downloadPool){
        this.downloadPool = downloadPool;
    
        AtomicInteger skipper = new AtomicInteger(0);
        final long start = System.currentTimeMillis();
        downloadPool.setObserver(progress -> {
            if(skipper.getAndIncrement() % 10 != 0){
                return;
            }
    
            Platform.runLater(() -> {
                try{
                    String totalSize = Utils.formatSize(progress.getTotalSize());
                    String totalDlSize = Utils.formatSize(progress.getTotalDlSize());
            
                    String currentSize = Utils.formatSize(progress.getCurrentSize());
                    String currentDlSize = Utils.formatSize(progress.getCurrentDlSize());
                    
                    long passed = System.currentTimeMillis() - start;
                    double average = (double) progress.getTotalDlSize() / (double) passed;
                    
                    int seconds = (int) Math.ceil(((progress.getTotalSize() - progress.getTotalDlSize()) / average) / 1000d);
                    String remaining = "";
    
                    if(seconds / 3600 > 0){
                        remaining += seconds / 3600 + "h ";
                        seconds = seconds % 3600;
                    }
    
                    if(seconds / 60 > 0){
                        remaining += seconds / 60 + "m ";
                        seconds = seconds % 60;
                    }
                    
                    if(seconds > 0){
                        remaining += seconds + "s ";
                    }
                    
                    remaining += "Remaining";
            
                    getDownloadAll().setText(String.format("Downloaded %d/%d (%sMB / %sMB)", progress.getTotalDlCount(), progress.getTotalCount(), totalDlSize, totalSize));
                    getDownloadCurrent().setText(String.format("Downloading %s (%sMB / %sMB)", progress.getCurrentName(), currentDlSize, currentSize));
                    getDownloadSpeed().setText(String.format("%.2f KB/s - %s", average, remaining));
            
                    double currentProgress = Math.min((double) progress.getCurrentDlSize() / (double) progress.getCurrentSize(), 100);
                    getProgressCurrent().setProgress(currentProgress);
                    getCurrentText().setText(String.format("%.2f%%", currentProgress * 100));
            
                    double totalProgress = Math.min((double) progress.getTotalDlSize() / (double) progress.getTotalSize(), 100);
                    getProgressTotal().setProgress(totalProgress);
                    getTotalText().setText(String.format("%.2f%%", totalProgress * 100));
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
        });
    }
    
}
