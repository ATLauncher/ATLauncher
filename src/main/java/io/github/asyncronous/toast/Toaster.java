package io.github.asyncronous.toast;

import io.github.asyncronous.toast.ui.ToastWindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * Static class to allow easier use of toaster notifications
 */
@SuppressWarnings("unused")
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
        UIManager.put("Toaster.infoIcon", createImage("info"));
        UIManager.put("Toaster.errorIcon", createImage("error"));
        UIManager.put("Toaster.questionIcon", createImage("question"));
        UIManager.put("Toaster.warningIcon", createImage("warning"));
        UIManager.put("Toaster.font", new Font("SansSerif", Font.BOLD, 12).deriveFont(24.0F));
        UIManager.put("Toaster.msgColor", Color.BLACK);
        UIManager.put("Toaster.borderColor", Color.BLACK);
        UIManager.put("Toaster.bgColor", Color.WHITE);
        UIManager.put("Toaster.time", 5000);
        UIManager.put("Toaster.opaque", false);
        UIManager.put("Toaster.opacity", 0.5F);
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
        window.setIcon(new ImageIcon((Image) UIManager.get("Toaster.questionIcon")));
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
        window.setIcon(new ImageIcon((Image) UIManager.get("Toaster.infoIcon")));
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
        window.setIcon(new ImageIcon((Image) UIManager.get("Toaster.warningIcon")));
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
        window.setIcon(new ImageIcon((Image) UIManager.get("Toaster.errorIcon")));
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