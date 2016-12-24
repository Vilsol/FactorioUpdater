package me.vilsol.factorioupdater.ui.templates;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

@Getter
public class DownloadProgressWindow extends FXMLTemplate<BorderPane> {
    
    @FXML private Text downloadAll;
    @FXML private Text downloadCurrent;
    
    @FXML private ProgressBar progressCurrent;
    @FXML private Text currentText;
    
    @FXML private ProgressBar progressTotal;
    @FXML private Text totalText;
    
    private final Stage stage;
    
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
    
}
