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
package me.vilsol.factorioupdater;

import java.awt.GraphicsEnvironment;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Nick Robson
 */
public class GameLauncherTest {

    public static void main(String[] args) {
        File credentials = new File(Resource.APP_HOME_DIR, "credentials.json");
        String username = null, password = null;
        if (credentials.exists()) {
            try {
                JSONObject o = new JSONObject(String.join("", Files.readAllLines(credentials.toPath())));
                username = o.optString("username");
                password = o.optString("password");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Path tempFile = Files.createTempDirectory("factorio-updater-download");
            boolean res = GameLauncher.fetchFactorio("0.14.21", username, password, tempFile.toFile(), null);
            if (!res)
                return;
            System.out.println("Files:");
            Files.walkFileTree(tempFile, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) {
                    System.out.println(file + File.separator);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            if (!GraphicsEnvironment.isHeadless()) {
                try {
                    Process proc = GameLauncher.launchFactorioClient(tempFile.toFile(), new String[0], null);
                    System.out.println("Factorio exited with code: " + proc.waitFor());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            deleteDirectory(tempFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

}
