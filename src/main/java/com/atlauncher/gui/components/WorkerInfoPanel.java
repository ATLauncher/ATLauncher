package com.atlauncher.gui.components;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.WorkerInfo;

/**
 * @since 2024 / 06 / 10
 */
public class WorkerInfoPanel extends JPanel {

    WorkerInfoPanel(WorkerInfo workerInfo, Runnable onStop) {
        setLayout(new BorderLayout());
        add(new JTextField(workerInfo.name), BorderLayout.PAGE_START);
        add(new JProgressBar(), BorderLayout.CENTER);

        JButton stopButton = new JButton(GetText.tr("Stop"));
        stopButton.addActionListener((e) -> onStop.run());
        add(stopButton, BorderLayout.LINE_END);
    }
}
