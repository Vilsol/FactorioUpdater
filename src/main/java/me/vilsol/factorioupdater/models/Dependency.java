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
import lombok.RequiredArgsConstructor;
import me.vilsol.factorioupdater.util.Mappable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@RequiredArgsConstructor
public class Dependency implements Comparable<Dependency>, Mappable {
    
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("^(\\??)\\s?([a-zA-Z0-9-_\\s]+?)\\s?((?:>|<|!)?=?)\\s?([\\d.]*)$");

    private final String name;
    private final boolean optional;
    private final Version version;
    private final String comparison;
    
    public Dependency(String s){
        Matcher matcher = DEPENDENCY_PATTERN.matcher(s);

        if(!matcher.find()){
            throw new RuntimeException("Incorrect Dependency Format");
        }

        try{
            optional = matcher.group(1).equals("?");
            name = matcher.group(2);
            comparison = matcher.group(3).isEmpty() ? "=" : matcher.group(3);
            version = new Version(matcher.group(4));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int compareTo(Dependency o){
        return version.compareTo(o.getVersion());
    }

    @Override
    public String toString() {
        return "Dependency(" + toFieldString() + ")";
    }

    public String toFieldString() {
        return String.format("%s%s %s %s", optional ? "? " : "", name, comparison, version);
    }
    
    @Override
    public Map<String, Object> map(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("optional", optional);
        map.put("version", version);
        map.put("comparison", comparison);
        return map;
    }
}
