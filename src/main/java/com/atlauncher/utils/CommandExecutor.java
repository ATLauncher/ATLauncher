package com.atlauncher.utils;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.exceptions.CommandException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor {
    /**
     * Runs the specified {@code command} in the system command line.
     * Substitutes <br/>
     * "$INST_NAME" with the name of the specified {@code instance}, <br/>
     * "$INST_ID" with the name of the instance's root directory, <br/>
     * "$INST_DIR" with the absolute path the the instance directory
     * "$INST_JAVA" with the absolute path to the java binary used to run the instance, <br/>
     * "$INST_JAVA_ARGS" with the JVM arguments (e.g. "-Xmx3G", NOT the arguments used to launch the game, containing account information), <br/>
     * in the specified command, then runs it on the command line.
     *
     * @param instance The instance to run the command for
     * @param command  The command to run on the command line
     * @throws CommandException If the process exits with a non zero value or another error occurs when trying to run the command
     */
    public static void executeCommand(Instance instance, String command) {
        if (App.settings.preLaunchCommand == null)
            return;

        try {
            command = replaceArgumentTokensForCommand(getCommandArgumentTokensForInstance(instance), command);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            if (process.exitValue() != 0) {
                System.out.println(process.exitValue());

                String errorText = IOUtils.toString(process.getErrorStream());

                if (errorText.isEmpty())
                    errorText = IOUtils.toString(process.getInputStream());

                throw new CommandException(errorText);
            }
        } catch (IOException | InterruptedException e) {
            throw new CommandException(e);
        }
    }

    /**
     * Substitutes all tokens beginning with '$' (and in all capitals) with the matching key in {@code tokens} if it exists
     *
     * @param tokens  All the keys that will be replaced with their value in {@code command}
     * @param command The string to substitute into
     * @return The string with all tokens substituted
     */
    private static String replaceArgumentTokensForCommand(Map<String, String> tokens, String command) {
        final Pattern tokenPattern = Pattern.compile("\\$([A-Z_]+)");
        final StringBuffer result = new StringBuffer();
        Matcher match = tokenPattern.matcher(command);

        while (match.find()) {
            final String key = match.group(1);

            if (tokens.containsKey(key)) {
                match.appendReplacement(result, tokens.get(key).replace("\\", "\\\\"));
            }
        }

        match.appendTail(result);

        return result.toString();
    }

    private static Map<String, String> getCommandArgumentTokensForInstance(Instance instance) {
        final Map<String, String> result = new HashMap<>();
        result.put("INST_NAME", instance.getName());
        result.put("INST_ID", instance.getRootDirectory().getName());
        result.put("INST_DIR", instance.getRootDirectory().getAbsolutePath());
        //not a thing in atlauncher
        //result.put("INST_MC_DIR", "");
        result.put("INST_JAVA", instance.getMinecraftJar().getAbsolutePath());
        result.put("INST_JAVA_ARGS", getJavaParametersForInstance(instance));
        return result;
    }

    private static String getJavaParametersForInstance(Instance instance) {
        if (instance.getSettings().javaArguments == null)
            return App.settings.javaParameters;
        else
            return instance.getSettings().javaArguments;
    }
}
