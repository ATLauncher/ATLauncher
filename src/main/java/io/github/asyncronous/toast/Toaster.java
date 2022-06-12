package io.github.asyncronous.toast;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlauncher.App;

import io.github.asyncronous.toast.ui.ToastWindow;

/**
 * Static class to allow easier use of toaster notifications
 */
public final class Toaster {
    private static final Logger LOG = LogManager.getLogger(Toaster.class);

    private static Toaster instance;

    public static Toaster instance() {
        return (instance == null ? instance = new Toaster() : instance);
    }

    /**
     * Field to keep track of the current toasters
     */
    @Intrinsic
    public static volatile int CURRENT_TOASTER_NUMBER = 0;

    /**
     * Field to keep track of the max amount of toasters able to fit on the screen
     */
    @Intrinsic
    public static volatile int MAX_TOASTER_IN_SCREEN = 0;

    /**
     * Field to keep track of the max amount of toasters at all times
     */
    @Intrinsic
    public static volatile int MAX_TOASTERS = 0;

    private Toaster() {
        UIManager.put(ToasterConstants.INFO_ICON, createImage("info"));
        UIManager.put(ToasterConstants.ERROR_ICON, createImage("error"));
        UIManager.put(ToasterConstants.QUESTION_ICON, createImage("question"));
        UIManager.put(ToasterConstants.WARNING_ICON, createImage("warning"));
        UIManager.put(ToasterConstants.FONT, App.THEME.getBoldFont().deriveFont(20.0F));
        UIManager.put("Toaster.contBounds", GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
    }

    /**
     * Will generate a question Toaster Notification with the chosen settings
     *
     * @param msg The text of the message you want to display
     * @example Toaster.popQuestion("This is a question?");
     */
    public void popQuestion(String msg) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(new ImageIcon((Image) UIManager.get(ToasterConstants.QUESTION_ICON)));
        window.pop();
    }

    /**
     * Will generate a standard info Toaster Notification with the chosen settings
     *
     * @param msg The text of the message you want to display
     * @example Toaster.pop("This is some information");
     */
    public void pop(String msg) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(new ImageIcon((Image) UIManager.get(ToasterConstants.INFO_ICON)));
        window.pop();
    }

    /**
     * Will generate a warning Toaster Notification with the chosen settings
     *
     * @param msg The text of the message you want to display
     * @example Toaster.popWarning("This is a warning");
     */
    public void popWarning(String msg) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(new ImageIcon((Image) UIManager.get(ToasterConstants.WARNING_ICON)));
        window.pop();
    }

    /**
     * Will generate an error Toaster Notification with the chosen settings
     *
     * @param msg The text of the message you want to display
     * @example Toaster.popError("This is an error");
     */
    public void popError(String msg) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(new ImageIcon((Image) UIManager.get(ToasterConstants.ERROR_ICON)));
        window.pop();
    }

    /**
     * Will generate a Toaster Notification with a custom Icon & the chosen settings
     *
     * @param msg The text of the message you want to display
     * @param ico The icon you would like to display
     * @example ImageIcon image = new
     *          ImageIcon(ImageIO.read(getClass().getResourceAsStream
     *          ("/assets/toaster/icons/error.png" ))); Toaster.pop("This is an
     *          error", image);
     */
    public void pop(String msg, Icon ico) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(ico);
        window.pop();
    }

    private Image createImage(String name) {
        InputStream stream = Toaster.class.getResourceAsStream("/assets/toast/icons/" + name + ".png");

        if (stream == null) {
            throw new NullPointerException("Stream == null");
        }

        try {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            LOG.error("Failed to load Toaster image", ex);
            return null;
        }
    }
}
