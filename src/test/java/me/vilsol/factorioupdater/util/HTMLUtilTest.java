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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Nick Robson
 */
public class HTMLUtilTest {

    @Test
    public void testEscape() {
        String input = "<p>\"What's going on & çome on?\"</p>";
        String expected = "&lt;p&gt;&quot;What&apos;s going on &amp; &#231;ome on?&quot;&lt;/p&gt;";
        assertEquals(expected, HTMLUtil.escapeHTML(input));
    }

    @Test
    public void testWrappedString() {
        StreamHandler.WrappedString ws = new StreamHandler.WrappedString("hello", false);
        assertEquals("<div>hello</div>", HTMLUtil.toHtml(ws));

        ws = new StreamHandler.WrappedString("world", true);
        assertEquals("<div style=\"color: red;\">world</div>", HTMLUtil.toHtml(ws));
    }

}
