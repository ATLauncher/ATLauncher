package io.github.asyncronous.toast;

public interface ToasterConstants {
    /**
     * The icon for an Information Toast Notification
     */
    public static final String INFO_ICON = "Toaster.infoIcon";

    /**
     * The icon for an Error Toast Notification
     */
    public static final String ERROR_ICON = "Toaster.errorIcon";

    /**
     * The icon for a Question Toast Notification
     */
    public static final String QUESTION_ICON = "Toaster.questionIcon";

    /**
     * The icon for a Warning Toast Notification
     */
    public static final String WARNING_ICON = "Toaster.warningIcon";

    /**
     * The font for the message in the notification
     */
    public static final String FONT = "Toaster.font";

    /**
     * The color of the message of the notification
     */
    public static final String MSG_COLOR = "Toaster.msgColor";

    /**
     * The color of the border of the notification
     */
    public static final String BORDER_COLOR = "Toaster.borderColor";

    /**
     * The color of the background of the notification
     */
    public static final String BG_COLOR = "Toaster.bgColor";

    /**
     * The delay of time in milliseconds before the notification collapses
     */
    public static final String TIME = "Toaster.time";

    /**
     * Set if the notifications are opaque or not
     *
     * @note Will override the opacity, causing it to be redundant
     */
    public static final String OPAQUE = "Toaster.opaque";

    /**
     * Changes the opacity of the notifications
     *
     * @note Overridden if the notifications are set to be opaque
     */
    public static final String OPACITY = "Toaster.opacity";
}
