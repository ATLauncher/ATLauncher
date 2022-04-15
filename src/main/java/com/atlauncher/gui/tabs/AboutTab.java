package com.atlauncher.gui.tabs;

import com.atlauncher.evnt.listener.RelocalizationListener;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;

/**
 * 14 / 04 / 2022
 */
public class AboutTab extends JPanel implements Tab, RelocalizationListener {
    @Override
    public void onRelocalization() {
        // TODO Request Ryan explain this to me
    }

    @Override
    public String getTitle() {
        return GetText.tr("About");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "About";
    }
}
