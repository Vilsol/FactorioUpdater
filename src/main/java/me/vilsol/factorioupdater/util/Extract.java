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

import java.util.regex.Pattern;
import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Nick Robson
 */
public class Extract {

    public static boolean extract(File file, File dir, String ext) {
        switch (ext) {
            case "zip":
            case "tar.gz":
                return simpleExtract(file, dir);
            case "dmg":
                return dmgExtract(file, dir);
        }
        return false;
    }

    public static boolean simpleExtract(File file, File dir) {
        try {
            Archiver archiver = ArchiverFactory.createArchiver(file);
            ArchiveStream stream = archiver.stream(file);
            ArchiveEntry entry = stream.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                String[] pathParts = name.split(Pattern.quote(File.separator), 2);
                if (pathParts[0].startsWith("factorio")) {
                    System.out.format("%s -> %s\n", name, pathParts[1]);
                    name = pathParts[1];
                }
                File extractTo = new File(dir, name);
                extractTo.getParentFile().mkdirs();
                entry.extract(extractTo);
                entry = stream.getNextEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean dmgExtract(File file, File dir) {
        boolean extracted = false;
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(new String[]{ "hdiutil", "mount", file.toString() });
            Process proc = runtime.exec(new String[]{ "hdiutil", "info" });
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line, prevLine = null;
            while ((line = reader.readLine()) != null) {
                prevLine = line;
            }
            if (prevLine != null) {
                String mountLocation = prevLine.split("\\t", 3)[2];
                runtime.exec(new String[]{"cp", "-R", mountLocation + "/factorio.app", dir.toString() });
                extracted = true;
                runtime.exec(new String[]{"hdiutil", "unmount", mountLocation});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extracted;
    }

}
