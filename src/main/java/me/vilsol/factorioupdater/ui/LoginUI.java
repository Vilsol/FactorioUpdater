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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import me.vilsol.factorioupdater.APIManager;
import me.vilsol.factorioupdater.Resource;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.function.BiConsumer;

/**
 * @author Nick Robson
 */
public class LoginUI {

    public static Pane create(UpdaterUI.Transitioner transition, boolean skipLogin) {
        if(skipLogin){
            Platform.runLater(() -> transition.transition(MainMenuUI.create(transition)));
            return new GridPane();
        }
        
        final TextField usernameField = new TextField();
        final PasswordField passwordField = new PasswordField();

        final Label usernameLabel = new Label("Username:");
        usernameLabel.setLabelFor(usernameField);

        final Label passwordLabel = new Label("Password:");
        passwordLabel.setLabelFor(passwordField);

        final Button loginButton = new Button();
        loginButton.setText("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        File credentials = new File(Resource.APP_HOME_DIR, "credentials.json");

        BiConsumer<String, String> doLogin = (username, password) -> {
            Thread thread = new Thread(() -> {
                if (username.isEmpty() || password.isEmpty()) {
                    return;
                }

                Platform.runLater(() -> {
                    loginButton.setDisable(true);
                    loginButton.setText("Logging in...");
                });

                try {
                    System.out.println("Logging in...");
    
                    APIManager.getInstance().login(username, password);
                        System.out.println("Successfully logged in as " + username + "!");
                        Platform.runLater(() -> transition.transition(MainMenuUI.create(transition)));
                        if (!credentials.exists()) {
                            JSONObject creds = new JSONObject();
                            creds.put("username", username);
                            creds.put("password", password);
                            Files.write(credentials.toPath(), creds.toString(4).getBytes(), StandardOpenOption.CREATE);
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                    UI.showAlert(Alert.AlertType.WARNING, null, "Login failed, please retry!");
                }

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                });
            }, "Factorio Login Thread");
            thread.setDaemon(true);
            thread.start();
        };

        if (credentials.exists()) {
            try {
                JSONObject o = new JSONObject(String.join("", Files.readAllLines(credentials.toPath())));
                String username = o.optString("username");
                String password = o.optString("password");
                if (username != null && password != null) {
                    usernameField.setText(username);
                    passwordField.setText(password);
                    doLogin.accept(username, password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Runnable onLogin = () -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            doLogin.accept(username, password);
        };

        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> onLogin.run());
        loginButton.setOnAction(event -> onLogin.run());

        VBox vbox = new VBox(20);
        vbox.setFillWidth(true);

        vbox.getChildren().add(new VBox(5, usernameLabel, usernameField));
        vbox.getChildren().add(new VBox(5, passwordLabel, passwordField));
        vbox.getChildren().add(loginButton);

        Text welcome = new Text("Welcome to " + Resource.APP_NAME);
        welcome.setFont(Font.font("Arial", 20));

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setHgap(20);
        layout.setVgap(20);

        layout.add(welcome, 0, 0);
        layout.add(vbox, 0, 1);

        return layout;
    }

}
