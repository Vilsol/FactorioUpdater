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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Nick Robson
 */
public class UI {

    public static void showAlert(Alert.AlertType type, String header, String message, Object... params) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setHeaderText(header);
            alert.setContentText(params.length == 0 ? message : String.format(message, params));
            
            DialogPane root = alert.getDialogPane();
            Stage dialogStage = new Stage(StageStyle.UTILITY);
    
            for (ButtonType buttonType : root.getButtonTypes()) {
                ButtonBase button = (ButtonBase) root.lookupButton(buttonType);
                button.setOnAction(evt -> {
                    root.setUserData(buttonType);
                    dialogStage.close();
                });
            }
            
            root.getScene().setRoot(new Group());
            Scene scene = new Scene(root);
    
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setAlwaysOnTop(true);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            if (type == Alert.AlertType.WARNING || type == Alert.AlertType.ERROR)
                System.err.println("[" + type.name() + "] " + message);
        });
    }

}
