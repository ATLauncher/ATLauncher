package com.atlauncher.adapter;

import com.atlauncher.Gsons;
import com.atlauncher.utils.Hashing;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;

public class HashCodeAdapterTest {
    @Test
    public void test()
    throws Exception{
        HashCodeTest test = Gsons.DEFAULT.fromJson(new InputStreamReader(System.class.getResourceAsStream("/hashcode.json")), HashCodeTest.class);
        Assert.assertEquals(test.hash, Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5"));
        System.out.println(test.hash.toString());
    }

    private static final class HashCodeTest{
        public final Hashing.HashCode hash;

        private HashCodeTest(Hashing.HashCode hash) {this.hash = hash;}
    }
}