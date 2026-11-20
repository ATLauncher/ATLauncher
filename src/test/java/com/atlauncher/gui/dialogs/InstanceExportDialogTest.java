/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2026 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini2Dx.gettext.GetText;
import org.mockito.MockedStatic;

import com.atlauncher.App;
import com.atlauncher.Launcher;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.InstanceLauncher;
import com.atlauncher.data.Settings;
import com.atlauncher.themes.ATLauncherLaf;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;

import io.github.asyncronous.toast.Toaster;

@Tag("ui")
class InstanceExportDialogTest {
    private static final int TIMEOUT_SECONDS = 10;
    private static final String MOD_FILE = "local-mod.jar";

    @TempDir
    Path testDir;

    @Test
    void closeButtonDismissesModrinthPermissionsAndCompletesExport() throws Exception {
        exportAndDismissWarning(InstanceExportFormat.MODRINTH,
            warning -> requireComponent(warning, JButton.class, button -> GetText.tr("Close").equals(button.getText()))
                .doClick(0));
    }

    @Test
    void titleBarCloseDismissesPermissionsAndCompletesCombinedExport() throws Exception {
        exportAndDismissWarning(InstanceExportFormat.CURSEFORGE_AND_MODRINTH,
            warning -> warning.dispatchEvent(new WindowEvent(warning, WindowEvent.WINDOW_CLOSING)));
    }

    private void exportAndDismissWarning(InstanceExportFormat format, Consumer<ModrinthExportOverridesDialog> dismiss)
        throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required for modal dialog tests");
        Path instanceDir = Files.createDirectory(testDir.resolve("instance"));
        Files.createFile(Files.createDirectory(instanceDir.resolve("mods")).resolve(MOD_FILE));

        Instance instance = mock(Instance.class);
        when(instance.getRoot()).thenReturn(instanceDir);
        instance.launcher = new InstanceLauncher();
        instance.launcher.name = "Local export";
        instance.launcher.version = "1.0";
        instance.launcher.lastExportAuthor = "Test author";
        instance.launcher.lastExportSaveTo = testDir.toString();
        Path mrpack = testDir.resolve("Local export 1.0.mrpack");
        String exportedMod = Paths.get("overrides", "mods", MOD_FILE).toString();
        when(instance.export("Local export", "1.0", "Test author", format, testDir.toString(),
            Collections.singletonList("mods"))).thenAnswer(invocation -> {
                assertFalse(SwingUtilities.isEventDispatchThread(), "Export must run in the background");
                return new Pair<>(mrpack, exportedMod + "\n");
            });

        Launcher originalLauncher = App.launcher;
        Settings originalSettings = App.settings;
        ATLauncherLaf originalTheme = App.THEME;
        Toaster originalToaster = App.TOASTER;
        Toaster toaster = mock(Toaster.class);
        JFrame owner = onEdt(() -> new JFrame("Instance export regression test"));
        CompletableFuture<ModrinthExportOverridesDialog> opened = new CompletableFuture<>();
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED
                && event.getSource() instanceof ModrinthExportOverridesDialog) {
                opened.complete((ModrinthExportOverridesDialog) event.getSource());
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            InstanceExportDialog exportDialog = onEdt(() -> {
                App.launcher = new Launcher();
                App.launcher.setParentFrame(owner);
                App.settings = new Settings();
                App.settings.defaultExportFormat = format;
                App.THEME = new ATLauncherLaf();
                App.TOASTER = toaster;
                owner.setSize(640, 480);
                owner.setVisible(true);
                return new InstanceExportDialog(instance);
            });
            FutureTask<Void> show = scheduleOnEdt(() -> {
                exportDialog.setVisible(true);
                return null;
            });
            FutureTask<Void> click = scheduleOnEdt(() -> {
                try (MockedStatic<OS> ignored = mockStatic(OS.class)) {
                    requireComponent(exportDialog, JButton.class,
                        button -> GetText.tr("Export").equals(button.getText())).doClick(0);
                }
                return null;
            });
            ModrinthExportOverridesDialog warning = opened.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            boolean progressWasClosed = onEdt(() -> {
                boolean progressVisible = Arrays.stream(exportDialog.getOwnedWindows())
                    .anyMatch(window -> window instanceof ProgressDialog && window.isShowing());
                String displayedMod = requireComponent(warning, JTextArea.class, area -> true).getText();
                dismiss.accept(warning);
                assertEquals(exportedMod, displayedMod.trim());
                return !progressVisible;
            });
            click.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            show.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            assertTrue(progressWasClosed, "Export progress should close before showing the permissions warning");
            onEdt(() -> {
                assertEquals(exportDialog, warning.getOwner());
                assertFalse(warning.isDisplayable());
                assertFalse(exportDialog.isDisplayable());
                assertTrue(owner.isShowing());
                assertFalse(Arrays.stream(owner.getOwnedWindows()).anyMatch(Window::isShowing));
                return null;
            });
            verify(toaster).pop(GetText.tr("Exported Instance Successfully"));
            verify(instance).save();
            assertEquals(instance.launcher.name, instance.launcher.lastExportName);
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            try {
                onEdt(() -> {
                    owner.dispose();
                    return null;
                });
            } finally {
                App.launcher = originalLauncher;
                App.settings = originalSettings;
                App.THEME = originalTheme;
                App.TOASTER = originalToaster;
            }
        }
    }

    private static <T extends Component> T requireComponent(Container root, Class<T> type, Predicate<T> match) {
        T component = findComponent(root, type, match);
        assertNotNull(component, "Expected " + type.getSimpleName() + " in " + root.getName());
        return component;
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type, Predicate<T> match) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component) && match.test(type.cast(component))) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                T found = findComponent((Container) component, type, match);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static <T> FutureTask<T> scheduleOnEdt(Callable<T> action) {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeLater(task);
        return task;
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        return scheduleOnEdt(action).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
