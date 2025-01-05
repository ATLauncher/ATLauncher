package io.github.asyncronous.toast.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.UIManager;

import io.github.asyncronous.toast.ToasterConstants;
import io.github.asyncronous.toast.thread.ToastAnimator;

/**
 * Main Toaster Notification class
 */
@SuppressWarnings("serial")
public final class ToastWindow extends JWindow {
    private final JLabel ICON = new JLabel();
    private final JTextArea MESSAGE = new JTextArea();

    public ToastWindow() {
        this.MESSAGE.setFont((Font) UIManager.get(ToasterConstants.FONT));
        this.MESSAGE.setBackground((Color) UIManager.get(ToasterConstants.BG_COLOR));
        this.MESSAGE.setForeground((Color) UIManager.get(ToasterConstants.MSG_COLOR));
        this.MESSAGE.setLineWrap(true);
        this.MESSAGE.setEditable(false);
        this.MESSAGE.setWrapStyleWord(true);
        this.MESSAGE.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        if (!((Boolean) UIManager.get(ToasterConstants.OPAQUE)) && GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
            this.setOpacity((Float) UIManager.get(ToasterConstants.OPACITY));
        }

        JPanel CONTENT_PANEL = new JPanel(new BorderLayout());
        JPanel WRAPPER_PANEL = new JPanel(new BorderLayout());
        WRAPPER_PANEL.setBackground((Color) UIManager.get(ToasterConstants.BG_COLOR));
        WRAPPER_PANEL.add(this.ICON, BorderLayout.WEST);
        WRAPPER_PANEL.add(this.MESSAGE, BorderLayout.CENTER);
        WRAPPER_PANEL.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        CONTENT_PANEL.setBorder(BorderFactory.createLineBorder((Color) UIManager.get(ToasterConstants.BORDER_COLOR)));
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
