/*
 * FROM: https://github.com/aterai/java-swing-tips/blob/main/examples/WheelOverNestedScrollPane/src/java/example/MainPanel.java#L111-L143
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 TERAI Atsuhiro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.atlauncher.gui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

public class WheelScrollLayerUI extends LayerUI<JScrollPane> {
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
        Component c = e.getComponent();
        int dir = e.getWheelRotation();
        JScrollPane main = l.getView();
        if (c instanceof JScrollPane && !c.equals(main)) {
            JScrollPane child = (JScrollPane) c;
            BoundedRangeModel m = child.getVerticalScrollBar().getModel();
            int extent = m.getExtent();
            int minimum = m.getMinimum();
            int maximum = m.getMaximum();
            int value = m.getValue();
            boolean b1 = dir > 0 && value + extent >= maximum;
            boolean b2 = dir < 0 && value <= minimum;
            if (b1 || b2) {
                main.dispatchEvent(SwingUtilities.convertMouseEvent(c, e, main));
            }
        }
    }
}

