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
package me.vilsol.factorioupdater.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(exclude = {"data"})
@ToString(exclude = {"creationDate", "updateDate", "data"})
public class SearchedMod {

    private final int id;
    private final String name;
    private final String owner;
    private final String title;
    private final String summary;
    private final Calendar creationDate;
    private final Calendar updateDate;
    private final ModRelease latestRelease;
    private final JSONObject data;
    private final String avatar;
    private final String homepage;

    public SearchedMod(JSONObject data){
        this.data = data;
        this.id = data.getInt("id");
        this.name = data.getString("name");
        this.owner = data.getString("owner");
        this.title = data.getString("title");
        this.summary = data.getString("summary");
        this.homepage = data.getString("homepage");

        try{
            this.creationDate = DatatypeConverter.parseDateTime(data.getString("created_at").replace(' ', 'T'));
        }catch(NumberFormatException e){
            throw new RuntimeException("Could not parse " + data.getString("created_at"), e);
        }

        try{
            this.updateDate = DatatypeConverter.parseDateTime(data.getString("updated_at").replace(' ', 'T'));
        }catch(NumberFormatException e){
            throw new RuntimeException("Could not parse " + data.getString("updated_at"), e);
        }

        this.latestRelease = new ModRelease(data.getJSONObject("latest_release"));

        String avatar = null;
        JSONObject mediaFile = data.optJSONObject("first_media_file");
        if(mediaFile != null){
            avatar = mediaFile.getJSONObject("urls").getString("original");
        }
        this.avatar = avatar;
    }

}
