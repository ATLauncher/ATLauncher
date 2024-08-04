package com.atlauncher.gui.components;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.WorkerInfo;
import com.atlauncher.utils.Utils;

/**
 * @since 2024 / 06 / 10
 */
public class WorkerInfoPanel extends JPanel {

    WorkerInfoPanel(WorkerInfo workerInfo, Runnable onStop) {
        setLayout(new BorderLayout());
        add(new JLabel(workerInfo.name, Utils.getIconImage(workerInfo.icon), JLabel.LEFT), BorderLayout.PAGE_START);
        add(new JProgressBar(), BorderLayout.CENTER);

        JButton stopButton = new JButton(GetText.tr("Stop"));
        stopButton.addActionListener((e) -> onStop.run());
        add(stopButton, BorderLayout.LINE_END);
    }
}
