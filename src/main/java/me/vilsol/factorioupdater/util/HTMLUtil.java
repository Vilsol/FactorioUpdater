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

/**
 * @author Nick Robson
 */
public class HTMLUtil {

    private HTMLUtil() {}

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127) {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else if (c == '"') {
                out.append("&quot;");
            } else if (c == '\'') {
                out.append("&apos;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String toHtml(StreamHandler.WrappedString string) {
        String text = escapeHTML(string.string);
        if (string.isError) {
            text = "<div style=\"color: red;\">" + text + "</div>";
        } else {
            text = "<div>" + text + "</div>";
        }
        return text;
    }

}
