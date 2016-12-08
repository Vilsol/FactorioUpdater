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
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Data
public class Version implements Comparable<Version> {
    
    private final int major, minor, patch;
    private final boolean anyMajor, anyMinor, anyPatch;
    
    public Version(String versionString){
        Objects.requireNonNull(versionString, "version string is null");
        versionString = versionString.trim();

        if (versionString.isEmpty() || versionString.equals("*")) {
            this.major = this.minor = this.patch = -1;
            this.anyMajor = this.anyMinor = this.anyPatch = true;
            return;
        }

        String[] split = versionString.split("\\.");
        if(split.length > 3){
            throw new IllegalArgumentException("Too many version fields. Must be in form XX.yy.zz, is '" + versionString + "'");
        }

        try{
            this.anyMajor = split.length < 1 || split[0].equals("*");
            this.anyMinor = split.length < 2 || split[1].equals("*");
            this.anyPatch = split.length < 3 || split[2].equals("*");
            
            this.major = anyMajor ? -1 : Integer.parseInt(split[0]);
            this.minor = anyMinor ? -1 : Integer.parseInt(split[1]);
            this.patch = anyPatch ? -1 : Integer.parseInt(split[2]);
            
            validateParts();
        }catch(NumberFormatException ex){
            throw new IllegalArgumentException("All fields must be numbers, is '" + versionString + "'");
        }
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.anyMajor = this.anyMinor = this.anyPatch = false;

        validateParts();
    }

    private void validateParts() {
        if(major < 0 && !anyMajor){
            throw new IllegalArgumentException("Major version is negative, is " + major);
        }else if(minor < 0 && !anyMinor){
            throw new IllegalArgumentException("Minor version is negative, is " + minor);
        }else if(patch < 0 && !anyPatch){
            throw new IllegalArgumentException("Patch version is negative, is " + patch);
        }else if(major == 0 && minor == 0 && patch == 0){
            throw new IllegalArgumentException("Major, minor, and patch versions are all zero.");
        }
    }
    
    @Override
    public int compareTo(Version o){
        if(major != o.major && !(anyMajor || o.anyMajor)){
            return Integer.compare(major, o.major);
        }else if(minor != o.minor && !(anyMinor || o.anyMinor)){
            return Integer.compare(minor, o.minor);
        }else if(patch != o.patch && !(anyPatch || o.anyPatch)){
            return Integer.compare(patch, o.patch);
        }
        return 0;
    }

    public boolean matchesAny() {
        return anyMajor && anyMinor && anyPatch;
    }

    public boolean matches(ComparisonOperator operator, Version version) {
        return operator.matches(this, version);
    }

    public boolean matches(String operator, Version version) {
        ComparisonOperator op = ComparisonOperator.fromString(operator);
        return op.matches(this, version);
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", anyMajor ? "*" : major, anyMinor ? "*" : minor, anyPatch ? "*" : patch);
    }

    public enum ComparisonOperator {

        EQ("=", c -> c == 0),
        NE("!=", c -> c != 0),
        LT("<", c -> c < 0),
        LE("<=", c -> c <= 0),
        GT(">", c -> c > 0),
        GE(">=", c -> c >= 0);

        @Getter
        private final String operator;
        private final Predicate<Integer> predicate;
        private static final Map<String, ComparisonOperator> operators = new HashMap<>();

        ComparisonOperator(String operator, Predicate<Integer> predicate) {
            this.operator = operator;
            this.predicate = predicate;
        }
        
        public String toString() {
            return operator;
        }

        public boolean matches(Version a, Version b) {
            int cmp = a.compareTo(b);
            return predicate.test(cmp);
        }

        public static ComparisonOperator fromString(String operator) {
            ComparisonOperator op = operators.get(operator);
            if (op != null)
                return op;
            throw new IllegalArgumentException("no comparison operator with operator '" + operator + "'");
        }

        static {
            for (ComparisonOperator op : values()) {
                operators.put(op.getOperator(), op);
            }
        }
        
    }

}
