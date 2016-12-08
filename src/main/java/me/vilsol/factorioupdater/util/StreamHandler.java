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

import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Nick Robson
 */
public class StreamHandler {

    public static final LinkedList<WrappedString> lastLines = new LinkedList<>();

    private static PrintToDeque outputStream, errorStream;
    public static PrintStream out, err;

    public static void init() {
        initOutput();
        initError();
    }

    public static void initOutput() {
        if (outputStream != null)
            return;
        outputStream = new PrintToDeque(lastLines, System.out, false);
        System.setOut(out = new PrintStream(outputStream, true));
    }

    public static void initError() {
        if (errorStream != null)
            return;
        errorStream = new PrintToDeque(lastLines, System.err, true);
        System.setErr(err = new PrintStream(errorStream, true));
    }

    public static PrintToDeque getOutputStream() {
        return outputStream;
    }

    public static PrintToDeque getErrorStream() {
        return errorStream;
    }

    @Getter
    public static class PrintToDeque extends OutputStream {

        private final boolean isErr;
        private final OutputStream parent;
        private final Deque<WrappedString> deque;
        private final List<Consumer<WrappedString>> onLineListeners = new LinkedList<>();

        private String currentLine = "";

        public PrintToDeque(Deque<WrappedString> deque, OutputStream parent, boolean isErr) {
            this.deque = deque;
            this.parent = parent;
            this.isErr = isErr;
        }

        private synchronized void queue(String s) {
            currentLine += s;
            String[] spl = currentLine.split("\\r?\\n");
            if (spl.length > 1) {
                for (int i = 1; i < spl.length; i++) {
                    WrappedString x = new WrappedString(spl[i - 1], isErr);
                    deque.add(x);
                    for (Consumer<WrappedString> onLine : onLineListeners)
                        onLine.accept(x);
                }
            }
            if (s.endsWith("\n")) {
                WrappedString x = new WrappedString(spl[spl.length - 1], isErr);
                deque.add(x);
                for (Consumer<WrappedString> onLine : onLineListeners)
                    onLine.accept(x);
                currentLine = "";
            } else {
                currentLine = spl[spl.length - 1];
            }

            while (deque.size() > 100)
                deque.removeFirst();
        }

        @Override
        public void write(int b) throws IOException {
            parent.write(b);
            queue(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] bs, int off, int len) throws IOException {
            parent.write(bs, off, len);
            queue(new String(bs, off, len));
        }

        public Runnable onLine(Consumer<WrappedString> consumer) {
            onLineListeners.add(consumer);
            return () -> onLineListeners.remove(consumer);
        }

    }

    public static class WrappedString {

        public final String string;
        public final boolean isError;

        public WrappedString(String string, boolean isError) {
            this.string = string;
            this.isError = isError;
        }

    }

}
