package com.atlauncher;

import javax.swing.SwingUtilities;

import com.atlauncher.gui.LauncherFrame;

public class App {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new LauncherFrame();
			}
		});
	}

}
