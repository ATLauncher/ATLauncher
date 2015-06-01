package com.atlauncher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Test;

import static org.junit.Assert.*;

public final class OptionsTest{
    @Test
    public void test(){
        OptionParser parser = new OptionParser();
        parser.accepts("launch").withRequiredArg().ofType(String.class);
        parser.accepts("updated").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug-level").withRequiredArg().ofType(Integer.class);
        parser.accepts("use-gzip").withRequiredArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration").withRequiredArg().ofType(Boolean.class);
        parser.accepts("force-offline-mode").withRequiredArg().ofType(Boolean.class);

        OptionSet options = parser.parse("--force-offline-mode=true", "--launch=ResonantRise", "--skip-tray-integration=true", "--debug-level=3");
        assertTrue(options.has("force-offline-mode"));
        assertEquals(true, options.valueOf("force-offline-mode"));
        assertEquals("ResonantRise", options.valueOf("launch"));
        assertEquals(3, options.valueOf("debug-level"));
    }
}