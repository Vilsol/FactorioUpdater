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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nick Robson
 */
public class VersionTest {

    final String[] versionStrings = { "1.1.2", "1.1.3", "1.3.0", "5.2.0", "10.1.10" };
    final Version[] versions = new Version[versionStrings.length];
    {
        for (int i = 0; i < versionStrings.length; i++) {
            versions[i] = new Version(versionStrings[i]);
        }
    }

    @Test
    public void testEquality() {
        for (int i = 0, j = versions.length; i < j; i++) {
            String[] parts = versionStrings[i].split("\\.");
            int[] v = new int[3];
            for (int k = 0; k < parts.length; k++) {
                v[k] = Integer.parseInt(parts[k]);
            }
            assertEquals(new Version(v[0], v[1], v[2]), versions[i]);
        }
    }

    @Test
    public void testComparability() {
        for (int i = 0, j = versions.length - 1; i < j; i++) {
            int cmp = versions[i].compareTo(versions[i + 1]);
            assertTrue("comparison failed should be < 0, was " + cmp, cmp < 0);
            cmp = versions[i + 1].compareTo(versions[i]);
            assertTrue("comparison failed should be > 0, was " + cmp, cmp > 0);
        }
        for (int i = 0, j = versions.length; i < j; i++) {
            int cmp = versions[i].compareTo(versions[i]);
            assertTrue("comparison failed should be 0, was " + cmp, cmp == 0);
        }
    }

    @Test
    public void testToString() {
        for (int i = 0; i < versionStrings.length; i++) {
            assertEquals(versionStrings[i], versions[i].toString());
        }
    }

    @Test
    public void testEquals() {
        Version a = new Version("1.1.1");
        assertEquals(a, a);
        assertEquals(0, a.compareTo(a));
    }

    @Test
    public void testFields() {
        Version a = new Version("*");
        assertEquals(-1, a.getMajor());
        assertEquals(-1, a.getMinor());
        assertEquals(-1, a.getPatch());
        assertTrue(a.isAnyMajor());
        assertTrue(a.isAnyMinor());
        assertTrue(a.isAnyPatch());

        Version b = new Version("1.1.1");
        assertEquals(1, b.getMajor());
        assertEquals(1, b.getMinor());
        assertEquals(1, b.getPatch());
        assertFalse(b.isAnyMajor());
        assertFalse(b.isAnyMinor());
        assertFalse(b.isAnyPatch());
    }

    @Test
    public void testStar() {
        Version a = new Version("1.1.0");
        Version b = new Version("1.1.*");
        assertEquals("comparison failed", 0, a.compareTo(b));

        a = new Version("1.1.0");
        b = new Version("*");
        assertEquals("comparison failed", 0, a.compareTo(b));

        a = new Version("1.1.0");
        b = new Version("2.*.*");
        assertNotEquals("comparison failed", 0, a.compareTo(b));
    }

    @Test
    public void testOperators() {
        for (Version.ComparisonOperator op : Version.ComparisonOperator.values()) {
            assertEquals(op, Version.ComparisonOperator.fromString(op.getOperator()));
            assertEquals(op.getOperator(), op.toString());
        }

        Version first = versions[0], last = versions[versions.length - 1];

        assertFalse(first.matches("=", last));
        assertTrue(first.matches("!=", last));
        assertTrue(first.matches("<", last));
        assertTrue(first.matches("<=", last));
        assertFalse(first.matches(">", last));
        assertFalse(first.matches(">=", last));

        assertFalse(first.matches(Version.ComparisonOperator.EQ, last));
        assertTrue(first.matches(Version.ComparisonOperator.NE, last));
        assertTrue(first.matches(Version.ComparisonOperator.LT, last));
        assertTrue(first.matches(Version.ComparisonOperator.LE, last));
        assertFalse(first.matches(Version.ComparisonOperator.GT, last));
        assertFalse(first.matches(Version.ComparisonOperator.GE, last));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSuchOperator() {
        Version.ComparisonOperator.fromString("~");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyFields() {
        new Version("1.1.1.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNumbers() {
        new Version("a.b.c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMajor() {
        new Version("-1.1.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMinor() {
        new Version("1.-1.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePatch() {
        new Version("1.1.-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllZero() {
        new Version("0.0.0");
    }

}
