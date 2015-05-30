package com.atlauncher.utils;

import com.atlauncher.OrderedRunner;
import com.atlauncher.anno.ExecutionOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(OrderedRunner.class)
public class CompressionUtilsTest {
    private Path desktop;

    @Before
    public void setup(){
        this.desktop = Paths.get(System.getProperty("user.home"), "Desktop");
    }

    @Test
    @ExecutionOrder(2)
    public void testUnzip()
    throws Exception {
        Path zip = this.desktop.resolve("Test.zip");
        Path output = this.desktop.resolve("Test2");
        FileUtils.createDirectory(output);
        CompressionUtils.unzip(zip, output);
    }

    @Test
    @ExecutionOrder(1)
    public void testZip()
    throws Exception {
        Path dir = this.desktop.resolve("Test");
        Path output = this.desktop.resolve("Test.zip");
        CompressionUtils.zip(output, dir);
    }
}