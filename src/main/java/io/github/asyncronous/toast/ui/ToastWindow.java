package io.github.asyncronous.toast.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.UIManager;

import com.atlauncher.LogManager;

import io.github.asyncronous.toast.ToasterConstants;
import io.github.asyncronous.toast.thread.ToastAnimator;

/**
 * Main Toaster Notification class
 */
public final class ToastWindow extends JWindow {
    private final JLabel ICON = new JLabel();
    private final JTextArea MESSAGE = new JTextArea();

    public ToastWindow() {
        this.MESSAGE.setFont((Font) UIManager.get(ToasterConstants.FONT));
        this.MESSAGE.setBackground((Color) UIManager.get(ToasterConstants.BG_COLOR));
        this.MESSAGE.setForeground((Color) UIManager.get(ToasterConstants.MSG_COLOR));
        this.MESSAGE.setLineWrap(true);
        this.MESSAGE.setEditable(false);
        this.MESSAGE.setMargin(new Insets(2, 2, 2, 2));
        this.MESSAGE.setWrapStyleWord(true);

        if (!((Boolean)UIManager.get(ToasterConstants.OPAQUE))) {
            tryToSetTranslucency();
        }

        JPanel CONTENT_PANEL = new JPanel(new BorderLayout(1, 1));
        CONTENT_PANEL.setBackground((Color) UIManager.get(ToasterConstants.BORDER_COLOR));
        JPanel WRAPPER_PANEL = new JPanel(new BorderLayout(2, 2));
        WRAPPER_PANEL.setBackground((Color) UIManager.get(ToasterConstants.BG_COLOR));
        WRAPPER_PANEL.add(this.ICON, BorderLayout.WEST);
        WRAPPER_PANEL.add(this.MESSAGE, BorderLayout.CENTER);
        CONTENT_PANEL.setBorder(BorderFactory.createEtchedBorder());
        CONTENT_PANEL.add(WRAPPER_PANEL);

        this.setFocusable(false);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(250, 100));
        this.getContentPane().add(CONTENT_PANEL);
        this.pack();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }
    
    private void tryToSetTranslucency() {
        try {
            final Class<?> windowTranslucency = Class.forName("java.awt.GraphicsDevice$WindowTranslucency");
            final Object translucent = windowTranslucency.getField("TRANSLUCENT").get(null);
            final Method isWindowTranslucencySupported = GraphicsDevice.class.getMethod("isWindowTranslucencySupported", windowTranslucency);
            final Method setOpacity = this.getClass().getMethod("setOpacity", float.class);
            
            final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            
            if ((Boolean)isWindowTranslucencySupported.invoke(device, translucent)) {
                setOpacity.invoke(this, (Float)UIManager.get(ToasterConstants.OPACITY));
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Could not set window translucency, ignoring", e);
        }
    }

    /**
     * Sets the text of the message
     *
     * @param text The text you would like to pop
     */
    public void setText(String text) {
        this.MESSAGE.setText(text);
    }

    /**
     * Gets the text of the message
     *
     * @return The text of the message
     */
    public String getText() {
        return this.MESSAGE.getText();
    }

    /**
     * Sets the icon of the message
     *
     * @param icon The icon you would like to pop
     */
    public void setIcon(Icon icon) {
        this.ICON.setIcon(icon);
    }

    /**
     * Gets the icon of the message
     *
     * @return The icon of the message
     */
    public Icon getIcon() {
        return this.ICON.getIcon();
    }

    /**
     * Called to pop the message
     */
    public void pop() {
        new ToastAnimator(this).execute();
    }
}