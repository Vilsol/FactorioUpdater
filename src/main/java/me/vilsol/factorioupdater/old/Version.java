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
package me.vilsol.factorioupdater.old;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Version implements Comparable<Version> {

    private int major = -1;
    private int minor = -1;
    private int patch = -1;
    private String downloadUrl;
    private List<String> dependencies;
    private String sign;
    private boolean optional;
    private int size;
    private String fileName;

    public Version(JSONObject json){
        this(json.getString("version"));

        downloadUrl = json.getString("download_url");
        dependencies = new ArrayList<>();

        /*
        if(json.has("info_json") && json.getJSONObject("info_json").has("dependencies")) {
            json.getJSONObject("info_json").getJSONArray("dependencies").forEach(dependency -> {
                if (!((String) dependency).startsWith("base")) {
                    dependencies.add(((String) dependency));
                }
            });
        }
        */

        size = json.getInt("file_size");
        fileName = json.getString("file_name");
    }

    public Version() {
        this(-1, -1, -1);
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String s) {
        String[] split = s.split("\\.");

        if(split.length == 3){
            patch = split[2].equals("*") ? -1 : Integer.parseInt(split[2]);
        }

        if(split.length >= 2){
            minor = split[1].equals("*") ? -1 : Integer.parseInt(split[1]);
        }

        if(split.length >= 1){
            major = split[0].equals("*") ? -1 : Integer.parseInt(split[0]);
        }
    }

    public boolean newerOrEqualTo(Version version){
        if(this.equals(version)){
            return true;
        }

        if(this.major > version.major){
            return true;
        }else if(this.major == version.major){
            if(this.minor > version.minor){
                return true;
            }else if(this.minor == version.minor){
                return this.patch > version.patch;
            }
        }

        return false;
    }

    public boolean matches(Version version){
        if(version.major == -1){
            return true;
        }else if(version.major == this.major){
            if(version.minor == -1){
                return true;
            }else if(version.minor == this.minor){
                return version.patch == -1 || version.patch == this.patch;
            }
        }

        return false;
    }

    @Override
    public int compareTo(Version version) {
        if(this.major > version.major){
            return -1;
        }else if(this.major < version.major){
            return 1;
        }

        if(this.minor > version.minor){
            return -1;
        }else if(this.minor < version.minor){
            return 1;
        }

        if(this.patch > version.patch){
            return -1;
        }else if(this.patch < version.patch){
            return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version version = (Version) o;

        if (major != version.major) return false;
        if (minor != version.minor) return false;
        return patch == version.patch;

    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String toFileString() {
        return (sign == null ? "" : sign + " ") + major + "." + minor + "." + patch;
    }

    public String toPrettyString() {
        if(major == -1){
            return "*";
        }else if(minor == -1){
            return major + ".*";
        }else if(patch == -1){
            return major + "." + minor + ".*";
        }

        return major + "." + minor + "." + patch;
    }

    @Override
    public String toString() {
        return "Version{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }

    public Version sign(String sign){
        this.sign = sign;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public Version optional(boolean optional){
        this.optional = optional;
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    public Version size(int size){
        this.size = size;
        return this;
    }

    public int getSize() {
        return size;
    }

    public String getFileName() {
        return fileName;
    }
}
