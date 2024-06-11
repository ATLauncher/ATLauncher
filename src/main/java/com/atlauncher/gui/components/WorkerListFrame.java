package com.atlauncher.gui.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.WorkerInfo;
import com.atlauncher.managers.WorkerManager;
import com.atlauncher.repository.base.IWorkerRepository;
import com.atlauncher.repository.impl.WorkerRepository;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @since 2024 / 06 / 10
 */
class WorkerListFrame extends JFrame {
    private final Disposable disposable;

    WorkerListFrame() {
        super(GetText.tr("Tasks"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 200));

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                close();
            }
        });


        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        IWorkerRepository workerRepository = WorkerRepository.get();
        disposable = workerRepository.getAll().observeOn(SwingSchedulers.edt()).subscribe(workers -> {
            panel.removeAll();
            System.gc();

            if (!workers.isEmpty()) {

                final GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.insets = UIConstants.FIELD_INSETS;
                gbc.fill = GridBagConstraints.BOTH;

                for (WorkerInfo worker : workers) {
                    panel.add(
                        new WorkerInfoPanel(
                            worker,
                            () -> WorkerManager.stop(worker.id)
                        ),
                        gbc
                    );

                    gbc.gridy++;
                }

                panel.validate();
                panel.repaint();
                pack();
            } else {
                close();
            }
        });

        JScrollPane scrollPane = new JScrollPane(
            panel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setPreferredSize(getMinimumSize());
        add(scrollPane);
        pack();
    }

    private void close() {

        dispose();
        disposable.dispose();
    }
}
