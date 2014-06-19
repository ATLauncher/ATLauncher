package io.github.asyncronous.toast;

import io.github.asyncronous.toast.ui.ToastWindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * Static class to allow easier use of toaster notifications
 */
public final class Toaster {
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
        UIManager.put(ToasterConstants.FONT, new Font("SansSerif", Font.BOLD, 12).deriveFont(24.0F));
        UIManager.put(ToasterConstants.MSG_COLOR, Color.BLACK);
        UIManager.put(ToasterConstants.BORDER_COLOR, Color.BLACK);
        UIManager.put(ToasterConstants.BG_COLOR, Color.WHITE);
        UIManager.put(ToasterConstants.TIME, 5000);
        UIManager.put(ToasterConstants.OPAQUE, false);
        UIManager.put(ToasterConstants.OPACITY, 0.5F);
        UIManager.put("Toaster.contBounds", GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds());
    }

    /**
     * Will generate a question Toaster Notification with the chosen settings
     * 
     * @example Toaster.popQuestion("This is a question?");
     * @param msg
     *            The text of the message you want to display
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
     * @example Toaster.pop("This is some information");
     * @param msg
     *            The text of the message you want to display
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
     * @example Toaster.popWarning("This is a warning");
     * @param msg
     *            The text of the message you want to display
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
     * @example Toaster.popError("This is an error");
     * @param msg
     *            The text of the message you want to display
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
     * @example ImageIcon image = new
     *          ImageIcon(ImageIO.read(getClass().getResourceAsStream("/assets/toaster/icons/error.png"
     *          ))); Toaster.pop("This is an error", image);
     * @param msg
     *            The text of the message you want to display
     * @param ico
     *            The icon you would like to display
     */
    public void pop(String msg, Icon ico) {
        ToastWindow window = new ToastWindow();
        window.setText(msg);
        window.setIcon(ico);
        window.pop();
    }

    private Image createImage(String name) {
        try {
            InputStream stream = Toaster.class.getResourceAsStream("/assets/toast/icons/" + name
                    + ".png");

            if (stream == null) {
                throw new NullPointerException("Stream == null");
            }

            return ImageIO.read(stream);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }
}