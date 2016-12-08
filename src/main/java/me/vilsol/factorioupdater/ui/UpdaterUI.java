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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lombok.Getter;
import me.vilsol.factorioupdater.Resource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Nick Robson
 */
public class UpdaterUI extends Application {

    private static final int width = 600, height = 400;
    private static final Paint paint = Color.WHITE;
    
    private static boolean skipLogin;
    
    private static Stage primaryStage;

    public static void launch(String[] args) {
        skipLogin = args.length >= 1 && args[0].equalsIgnoreCase("--skip-login");
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        UpdaterUI.primaryStage = primaryStage;
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);

        Transitioner transitioner = new Transitioner(primaryStage);

        //MenuBar menubar = MenuBarUI.create();
        Pane loginPane = LoginUI.create(transitioner, skipLogin);

        //root.add(menubar, 0, 0);
        root.add(loginPane, 0, 0);

        primaryStage.setTitle(Resource.APP_NAME);
        transitioner.transition(root);

        primaryStage.show();
    
        primaryStage.setMinWidth(595);
        primaryStage.setMinHeight(500);
    
        primaryStage.setWidth(575);
        primaryStage.setHeight(500);
        
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        
        primaryStage.toFront();
    }
    
    public static Stage getPrimaryStage(){
        return primaryStage;
    }
    
    public static class Transitioner {

        @Getter
        private final Stage stage;
        private final Stack<Scene> scenes = new Stack<>();
        private final Map<Integer, Set<Runnable>> onDestroy = new ConcurrentHashMap<>();

        private Scene currentScene;

        private ReentrantLock lock = new ReentrantLock();

        public Transitioner(Stage stage) {
            this.stage = stage;
        }

        public void onDestroy(Runnable runnable) {
            Objects.requireNonNull(runnable, "runnable");
            int depth = scenes.size();
            Set<Runnable> set = onDestroy.getOrDefault(depth, new HashSet<>());
            set.add(runnable);
            onDestroy.put(depth, set);
        }

        public void transition(Parent newRoot) {
            transition(newRoot, false);
        }

        public void transition(Parent newRoot, boolean async) {
            if (async)
                Platform.runLater(() -> doTransition(newRoot));
            else
                doTransition(newRoot);
        }

        private void doTransition(Parent newRoot) {
            lock.lock();
            if (currentScene != null)
                scenes.push(currentScene);
            currentScene = new Scene(newRoot, width, height, paint);
            stage.setScene(currentScene);
            lock.unlock();
        }

        public Scene current() {
            return currentScene;
        }

        public void back(int n, boolean async) {
            if (async)
                Platform.runLater(() -> doBack(n));
            else
                doBack(n);
        }

        private void doBack(int n) {
            lock.lock();
            for (int i = 0; i < n; i++) {
                if (!scenes.isEmpty()) {
                    int depth = scenes.size();
                    Set<Runnable> set = onDestroy.remove(depth);
                    if (set != null) {
                        for (Runnable r : set) {
                            try {
                                r.run();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    currentScene = scenes.pop();
                }
            }
            if (n > 0)
                stage.setScene(currentScene);
            lock.unlock();
        }

        public void back(int n) {
            back(n, false);
        }

        public void back() {
            back(1);
        }

        public void back(boolean async) {
            back(1, async);
        }

    }
}
