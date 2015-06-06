package com.atlauncher;

import org.junit.Test;

public final class SpecialCharactersTest{
    @Test
    public void test(){
        for(int i = Character.MIN_CODE_POINT; i <= Character.MAX_CODE_POINT; i++){
            if(Character.isJavaIdentifierStart(i) && !Character.isAlphabetic(i)){
                System.out.println(i + " " + (char) i);
            }
        }
    }
}