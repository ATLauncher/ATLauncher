package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.utils.ComboItem;

public class ModUpdatesChooserCard extends JPanel {
    final private Instance instance;
    final private DisableableMod mod;

    public ModUpdatesChooserCard(Instance instance, DisableableMod mod) {
        super();

        this.instance = instance;
        this.mod = mod;

        setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder(mod.getNameFromFile(instance))));
        setPreferredSize(new Dimension(300, 250));
        setLayout(new BorderLayout());

        setupComponents();
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Current Version
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel currentVersionLabel = new JLabel(GetText.tr("Current Version") + ": ");
        currentVersionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        currentVersionLabel.setFont(
                currentVersionLabel.getFont().deriveFont(currentVersionLabel.getFont().getStyle() | Font.BOLD));
        mainPanel.add(currentVersionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel currentVersion = new JLabel(String.format("%s (%s)", mod.getVersionFromFile(instance), mod.file));
        currentVersion.setToolTipText(currentVersion.getText());
        currentVersion.setBorder(new EmptyBorder(0, 10, 0, 10));
        mainPanel.add(currentVersion, gbc);

        // Updated Version Selector
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel updatedVersionLabel = new JLabel(GetText.tr("Updated Version") + ": ");
        updatedVersionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        updatedVersionLabel.setFont(
                updatedVersionLabel.getFont().deriveFont(updatedVersionLabel.getFont().getStyle() | Font.BOLD));
        mainPanel.add(updatedVersionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel updatedVersionPanel = new JPanel();
        updatedVersionPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        updatedVersionPanel.setLayout(new BoxLayout(updatedVersionPanel, BoxLayout.X_AXIS));

        JComboBox<ComboItem<String>> updatedVersionComboBox = new JComboBox<>();
        updatedVersionComboBox.addItem(new ComboItem<String>("347HlKZS", "iris-mc1.19.4-1.6.1.jar"));
        updatedVersionComboBox.addItem(new ComboItem<String>("rKiitlRl", "iris-mc1.19.4-1.6.0.jar"));
        updatedVersionComboBox.addItem(new ComboItem<String>("mVGu4Ze2", "iris-mc1.19.4-1.5.2.jar"));
        updatedVersionPanel.add(updatedVersionComboBox);
        mainPanel.add(updatedVersionPanel, gbc);
        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.setLayout(new FlowLayout());

        JButton changelogButton = new JButton(GetText.tr("Changelog"));
        bottomPanel.add(changelogButton);

        JButton websiteButton = new JButton(GetText.tr("Website"));
        bottomPanel.add(websiteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(
                new ImageIcon("C:\\Users\\rldow\\Downloads\\dc558eece920db435f9823ce86de0c4cde89800b (1).png"));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(iconLabel, BorderLayout.NORTH);
    }
}
