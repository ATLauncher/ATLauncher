package io.github.asyncronous.toast.thread;

import io.github.asyncronous.toast.Toaster;
import io.github.asyncronous.toast.ui.ToastWindow;

import javax.swing.*;
import java.awt.*;

/**
 * The main Toaster animation class, controls the entire window - location, whether or not to display, etc...
 */
public final class ToastAnimator extends SwingWorker<Void, Void> {
    private final ToastWindow window;

    public ToastAnimator(ToastWindow window) {
        this.window = window;
    }

    @Override
    protected Void doInBackground()
    throws Exception {
        boolean fromBottom = true;
        Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        int startY;
        int stopY;

        if(screenRect.y > 0){
            fromBottom = false;
        }

        Toaster.MAX_TOASTER_IN_SCREEN = (screenRect.height / this.window.getHeight());
        int posX = screenRect.width - this.window.getWidth() - 1;
        this.window.setLocation(posX, screenRect.height);
        this.window.setVisible(true);

        if(fromBottom){
            startY = screenRect.height;
            stopY = startY - this.window.getHeight() - 1;
            if(Toaster.CURRENT_TOASTER_NUMBER > 0){
                stopY = stopY - (Toaster.MAX_TOASTERS % Toaster.MAX_TOASTER_IN_SCREEN * this.window.getHeight());
            } else{
                Toaster.MAX_TOASTERS = 0;
            }
        } else{
            startY = screenRect.y - this.window.getHeight();
            stopY = screenRect.y;

            if(Toaster.CURRENT_TOASTER_NUMBER > 0){
                stopY = stopY + (Toaster.MAX_TOASTERS % Toaster.MAX_TOASTER_IN_SCREEN * this.window.getHeight());
            } else{
                Toaster.MAX_TOASTERS = 0;
            }
        }

        Toaster.CURRENT_TOASTER_NUMBER++;
        Toaster.MAX_TOASTERS++;
        this.moveVert(posX, startY, stopY);
        Thread.sleep((Integer) UIManager.get("Toaster.time"));
        this.moveVert(posX, stopY, startY);
        Toaster.CURRENT_TOASTER_NUMBER--;
        Thread.sleep(250);
        this.window.setVisible(false);
        this.window.dispose();

        return null;
    }

    private void moveVert(int posx, int fromY, int toY)
    throws Exception{
        this.window.setLocation(posx, fromY);
        if(toY < fromY){
            for(int i = fromY; i > toY; i -= 20){
                this.window.setLocation(posx, i);
                Thread.sleep(20);
            }
        } else{
            for(int i = fromY; i < toY; i += 20){
                this.window.setLocation(posx, i);
                Thread.sleep(20);
            }
        }
        this.window.setLocation(posx, toY);
    }
}