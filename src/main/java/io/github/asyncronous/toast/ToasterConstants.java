package io.github.asyncronous.toast;

public interface ToasterConstants {
    /**
     * The icon for an Information Toast Notification
     */
    String INFO_ICON = "Toaster.infoIcon";

    /**
     * The icon for an Error Toast Notification
     */
    String ERROR_ICON = "Toaster.errorIcon";

    /**
     * The icon for a Question Toast Notification
     */
    String QUESTION_ICON = "Toaster.questionIcon";

    /**
     * The icon for a Warning Toast Notification
     */
    String WARNING_ICON = "Toaster.warningIcon";

    /**
     * The font for the message in the notification
     */
    String FONT = "Toaster.font";

    /**
     * The color of the message of the notification
     */
    String MSG_COLOR = "Toaster.msgColor";

    /**
     * The color of the border of the notification
     */
    String BORDER_COLOR = "Toaster.borderColor";

    /**
     * The color of the background of the notification
     */
    String BG_COLOR = "Toaster.bgColor";

    /**
     * The delay of time in milliseconds before the notification collapses
     */
    String TIME = "Toaster.time";

    /**
     * Set if the notifications are opaque or not
     *
     * @note Will override the opacity, causing it to be redundant
     */
    String OPAQUE = "Toaster.opaque";

    /**
     * Changes the opacity of the notifications
     *
     * @note Overridden if the notifications are set to be opaque
     */
    String OPACITY = "Toaster.opacity";
}
