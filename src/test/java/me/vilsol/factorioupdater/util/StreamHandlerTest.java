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

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nick Robson
 */
public class StreamHandlerTest {

    @Test
    public void testOutputStreamHandler() {
        StreamHandler.lastLines.clear();
        PrintStream ps = System.out;
        assertNull(StreamHandler.getOutputStream());
        StreamHandler.initOutput();
        assertNotNull(StreamHandler.getOutputStream());
        assertFalse(StreamHandler.getOutputStream().isErr());
        assertEquals(ps, StreamHandler.getOutputStream().getParent());
        assertEquals(StreamHandler.lastLines, StreamHandler.getErrorStream().getDeque());

        for (int i = 0; i < 50; i++) {
            assertEquals(i, StreamHandler.lastLines.size());
            System.out.println(i);
            assertEquals(i + 1, StreamHandler.lastLines.size());
            StreamHandler.WrappedString ws = StreamHandler.lastLines.get(i);
            assertEquals(String.valueOf(i), ws.string);
            assertFalse(ws.isError);
        }

        try {
            StreamHandler.getOutputStream().write('a');
            StreamHandler.getOutputStream().write('\r');
            StreamHandler.getOutputStream().write('\n');
            assertEquals("a", StreamHandler.lastLines.get(50).string);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AtomicBoolean b = new AtomicBoolean();
        StreamHandler.getOutputStream().onLine(ws -> b.set(true));
        assertFalse(b.get());

        System.out.println("Hey\r\nthere\nmate!");

        assertTrue(b.get());

        assertEquals("Hey", StreamHandler.lastLines.get(51).string);
        assertEquals("there", StreamHandler.lastLines.get(52).string);
        assertEquals("mate!", StreamHandler.lastLines.get(53).string);

        for (int i = 50; i < 54; i++){
            assertFalse(StreamHandler.lastLines.get(i).isError);
        }
    }

    @Test
    public void testErrorStreamHandler(){
        StreamHandler.lastLines.clear();
        PrintStream ps = System.err;
        assertNull(StreamHandler.getErrorStream());
        StreamHandler.initError();
        assertNotNull(StreamHandler.getErrorStream());
        assertTrue(StreamHandler.getErrorStream().isErr());
        assertEquals(ps, StreamHandler.getErrorStream().getParent());
        assertEquals(StreamHandler.lastLines, StreamHandler.getErrorStream().getDeque());

        for(int i = 0; i < 50; i++){
            assertEquals(i, StreamHandler.lastLines.size());
            System.err.println(i);
            assertEquals(i + 1, StreamHandler.lastLines.size());
            StreamHandler.WrappedString ws = StreamHandler.lastLines.get(i);
            assertEquals(String.valueOf(i), ws.string);
            assertTrue(ws.isError);
        }
    }

    @Test
    public void testStreamHandlers() {
        StreamHandler.lastLines.clear();
        StreamHandler.PrintToDeque out = StreamHandler.getOutputStream();
        StreamHandler.PrintToDeque err = StreamHandler.getErrorStream();
        StreamHandler.init();
        assertNotNull(StreamHandler.getOutputStream());
        assertNotNull(StreamHandler.getErrorStream());
        if (out != null)
            assertEquals(out, StreamHandler.getOutputStream());
        if (err != null)
            assertEquals(err, StreamHandler.getErrorStream());

        for (int i = 0; i < 50; i++) {
            assertEquals(i*2, StreamHandler.lastLines.size());
            System.out.println(i);
            System.err.println(i);
            assertEquals(i*2 + 2, StreamHandler.lastLines.size());
            StreamHandler.WrappedString ws = StreamHandler.lastLines.get(i*2);
            assertEquals(String.valueOf(i), ws.string);
            assertFalse(ws.isError);
            ws = StreamHandler.lastLines.get(i*2 + 1);
            assertEquals(String.valueOf(i), ws.string);
            assertTrue(ws.isError);
        }

        assertEquals(100, StreamHandler.lastLines.size());
        System.out.println("hello!");
        assertEquals(100, StreamHandler.lastLines.size());
        StreamHandler.WrappedString ws = StreamHandler.lastLines.get(99);
        assertEquals("hello!", ws.string);
        assertFalse(ws.isError);
    }

    @Test
    public void testLineListener() {
        StreamHandler.lastLines.clear();
        StreamHandler.PrintToDeque out = StreamHandler.getOutputStream();
        StreamHandler.PrintToDeque err = StreamHandler.getErrorStream();
        StreamHandler.init();
        assertNotNull(StreamHandler.getOutputStream());
        assertNotNull(StreamHandler.getErrorStream());
        if (out != null)
            assertEquals(out, StreamHandler.getOutputStream());
        if (err != null)
            assertEquals(err, StreamHandler.getErrorStream());

        List<StreamHandler.WrappedString> list = new LinkedList<>();
        Consumer<StreamHandler.WrappedString> testListener = list::add;

        Runnable r = StreamHandler.getOutputStream().onLine(testListener);
        assertNotNull(r);

        assertEquals(0, list.size());
        System.out.println("test");
        assertEquals(1, list.size());
        StreamHandler.WrappedString ws = list.get(0);
        assertEquals("test", ws.string);
        assertFalse(ws.isError);

        System.err.println("test2");
        assertEquals(1, list.size());

        Runnable r2 = StreamHandler.getErrorStream().onLine(testListener);
        assertNotNull(r2);

        System.err.println("test2");
        assertEquals(2, list.size());
        ws = list.get(1);
        assertEquals("test2", ws.string);
        assertTrue(ws.isError);

        r.run();
        System.out.println("test3");
        assertEquals(2, list.size());

        r2.run();
        System.err.println("test4");
        assertEquals(2, list.size());
    }

}
