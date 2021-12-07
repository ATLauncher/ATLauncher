package com.atlauncher.logging;

import com.atlauncher.App;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Timestamper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import javax.swing.UIManager;
import java.awt.Color;
import java.io.Serializable;
import java.util.Optional;

@Plugin(name= LauncherConsoleAppender.PLUGIN_NAME,
        category= LauncherConsoleAppender.PLUGIN_CATEGORY,
        elementType=Appender.ELEMENT_TYPE,
        printObject=true)
public final class LauncherConsoleAppender extends AbstractAppender{
    public static final String PLUGIN_NAME = "LauncherConsole";
    public static final String PLUGIN_CATEGORY = Core.CATEGORY_NAME;

    public LauncherConsoleAppender(String name,
                                   Filter filter,
                                   Layout<? extends Serializable> layout,
                                   boolean ignoreExceptions,
                                   Property[] properties){
        super(name, filter, layout, ignoreExceptions, properties);
    }

    private Optional<Console> getConsole(){
        if(App.console == null)
            return Optional.empty();
        return Optional.ofNullable(App.console.console);
    }

    @Override
    public void append(LogEvent e){
        this.getConsole().ifPresent((console)->{
            // write timestamp
            console.setColor(getColor(e)).setBold(true).write("[" + Timestamper.now() + "]");
            console.setColor(UIManager.getColor("EditorPane.foreground"))
                .setBold(false)
                .write(String.format("%s\n", e.getMessage().getFormattedMessage()));
        });

//TODO: restore functionality
//        if (App.settings != null && !LogManager.showDebug) {
//            body = body.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "**USERSDIR**");
//        }
    }

    private static Color getColor(final LogEvent e){
        if(e.getLevel().equals(Level.INFO)){
            return UIManager.getColor("Console.LogType.info");
        } else if(e.getLevel().equals(Level.WARN)){
            return UIManager.getColor("Console.LogType.warn");
        } else if(e.getLevel().equals(Level.ERROR)){
            return UIManager.getColor("Console.LogType.error");
        } else if(e.getLevel().equals(Level.DEBUG)){
            return UIManager.getColor("Console.LogType.debug");
        }
        return UIManager.getColor("Console.LogType.default");
    }

    @PluginFactory
    public static LauncherConsoleAppender createAppender(@PluginAttribute("name") String name){
        return new LauncherConsoleAppender(name, null, null, false, null);
    }
}