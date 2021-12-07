package com.atlauncher.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingUtils{
    private LoggingUtils(){}

    public static void redirectSystemOutLogs(){
        System.setOut(SystemOutInterceptor.asDebug(System.out));
        System.setErr(SystemOutInterceptor.asError(System.err));
    }

    private static final Logger MINECRAFT_LOG = LogManager.getLogger("Minecraft");

    public static void minecraft(String line){
        if (line.contains("[INFO] [STDERR]")){
            MINECRAFT_LOG.warn(line.substring(line.indexOf("[INFO] [STDERR]")));
        } else if (line.contains("[INFO]")){
            line = line.substring(line.indexOf("[INFO]"));
            if (line.contains("CONFLICT")) {
                MINECRAFT_LOG.error(line);
            } else if (line.contains("overwriting existing item")) {
                MINECRAFT_LOG.warn(line);
            } else {
                MINECRAFT_LOG.info(line);
            }
        } else if (line.contains("[WARNING]")) {
            line = line.substring(line.indexOf("[WARNING]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("WARNING:")) {
            line = line.substring(line.indexOf("WARNING:"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("INFO:")) {
            line = line.substring(line.indexOf("INFO:"));
            MINECRAFT_LOG.info(line);
        } else if (line.contains("Exception")) {
            line = line;
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[SEVERE]")) {
            line = line.substring(line.indexOf("[SEVERE]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[Sound Library Loader/ERROR]")) {
            line = line.substring(line.indexOf("[Sound Library Loader/ERROR]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[Sound Library Loader/WARN]")) {
            line = line.substring(line.indexOf("[Sound Library Loader/WARN]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("[Sound Library Loader/INFO]")) {
            line = line.substring(line.indexOf("[Sound Library Loader/INFO]"));
            MINECRAFT_LOG.info(line);
        } else if (line.contains("[MCO Availability Checker #1/ERROR]")) {
            line = line.substring(line.indexOf("[MCO Availability Checker #1/ERROR]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[MCO Availability Checker #1/WARN]")) {
            line = line.substring(line.indexOf("[MCO Availability Checker #1/WARN]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("[MCO Availability Checker #1/INFO]")) {
            line = line.substring(line.indexOf("[MCO Availability Checker #1/INFO]"));
            MINECRAFT_LOG.info(line);
        } else if (line.contains("[Client thread/ERROR]")) {
            line = line.substring(line.indexOf("[Client thread/ERROR]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[Client thread/WARN]")) {
            line = line.substring(line.indexOf("[Client thread/WARN]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("[Client thread/INFO]")) {
            line = line.substring(line.indexOf("[Client thread/INFO]"));
            MINECRAFT_LOG.info(line);
        } else if (line.contains("[Server thread/ERROR]")) {
            line = line.substring(line.indexOf("[Server thread/ERROR]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[Server thread/WARN]")) {
            line = line.substring(line.indexOf("[Server thread/WARN]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("[Server thread/INFO]")) {
            line = line.substring(line.indexOf("[Server thread/INFO]"));
            MINECRAFT_LOG.info(line);
        } else if (line.contains("[main/ERROR]")) {
            line = line.substring(line.indexOf("[main/ERROR]"));
            MINECRAFT_LOG.error(line);
        } else if (line.contains("[main/WARN]")) {
            line = line.substring(line.indexOf("[main/WARN]"));
            MINECRAFT_LOG.warn(line);
        } else if (line.contains("[main/INFO]")) {
            line = line.substring(line.indexOf("[main/INFO]"));
            MINECRAFT_LOG.info(line);
        } else {
            MINECRAFT_LOG.info(line);
        }
    }
}