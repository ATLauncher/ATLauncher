package com.atlauncher.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import com.atlauncher.repository.base.IWorkerRepository;
import com.atlauncher.repository.impl.WorkerRepository;
import com.atlauncher.utils.Utils;

/**
 * @since 2024 / 06 / 10
 */
public class WorkerIndicator extends JLabel {
    private final IWorkerRepository workerRepository = WorkerRepository.get();
    private WorkerListFrame frame = null;

    public WorkerIndicator() {
        super(Utils.getIconImage("/assets/image/loading-bars-small.gif"));
        workerRepository.getAll().subscribe(jobs -> {
            setVisible(!jobs.isEmpty());
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (frame != null) {
                    frame.dispose();
                    frame = null;
                    System.gc();
                }

                frame = new WorkerListFrame();
                frame.setVisible(true);
            }
        });
    }
}
