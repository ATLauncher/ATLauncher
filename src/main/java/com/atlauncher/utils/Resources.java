package com.atlauncher.utils;

import com.atlauncher.exceptions.ChunkyException;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class Resources{
    private static final Map<String, Object> resources = new HashMap<String, Object>();
    public static final String[] FONT_FAMILIES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    private Resources(){}

    public static boolean isSystemFont(String name){
        for(String str : FONT_FAMILIES){
            if(str.equalsIgnoreCase(name)){
                return true;
            }
        }

        return false;
    }

    public static StyleSheet makeStyleSheet(String name){
        try{
            if(resources.containsKey(name)){
                Object obj = resources.get(name);
                if(!(obj instanceof StyleSheet)){
                    throw new ChunkyException("Reference for " + name + " ended up with a bad value, suggested=" + StyleSheet.class.getName() + "; got=" + obj.getClass().getName());
                } else{
                    return (StyleSheet) obj;
                }
            } else{
                StyleSheet sheet = new StyleSheet();
                Reader reader = new InputStreamReader(System.class.getResourceAsStream("/assets/css/" + name + ".css"));
                sheet.loadRules(reader, null);
                reader.close();

                resources.put(name, sheet);
                return sheet;
            }
        } catch(Exception ex){
            throw new ChunkyException(ex);
        }
    }

    public static Font makeFont(String name){
        try{
            if(resources.containsKey(name)){
                Object obj = resources.get(name);
                if(!(obj instanceof Font)){
                    throw new ChunkyException("Reference for " + name + " ended up with a bad value, suggested=" + Font.class.getName() + "; got=" + obj.getClass().getName());
                } else{
                    return (Font) obj;
                }
            } else{
                if(isSystemFont(name)){
                    Font f = new Font(name, Font.PLAIN, 0);
                    resources.put(name, f);
                    return f;
                } else{
                    URL url = System.class.getResource("/assets/font/" + name + ".ttf");
                    if(url == null){
                        throw new NullPointerException("Cannot find font " + name);
                    } else{
                        Font f = Font.createFont(Font.TRUETYPE_FONT, url.openStream());
                        resources.put(name, f);
                        return f;
                    }
                }
            }
        } catch(Exception ex){
            throw new ChunkyException(ex);
        }
    }
}