/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2020 Philippe FICHET.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.github.philippefichet.sonarlint4netbeans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.connected.LoadedAnalyzer;

public final class SonarLintPanel extends javax.swing.JPanel {

    private final SonarLintOptionsPanelController controller;

    private final Map<RuleKey, Boolean> ruleKeyChanged = new HashMap<>();
    private DefaultTableModel analyzerDefaultTableModel = new DefaultTableModel();

    private SonarLintRuleTableModel rulesDefaultTableModel = new SonarLintRuleTableModel();

    public SonarLintPanel(SonarLintOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.PAGE_AXIS));
        JProgressBar waiting = new JProgressBar();
        waiting.setIndeterminate(true);
        JLabel loadingText = new JLabel("Loading ...");
        loadingPanel.add(loadingText);
        loadingPanel.add(waiting);
        optionPanel.add(loadingPanel, BorderLayout.NORTH);

        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        sonarLintEngine.whenInitialized(engine -> {
            analyzerDefaultTableModel.addColumn("Key");
            analyzerDefaultTableModel.addColumn("Name");
            analyzerDefaultTableModel.addColumn("Version");
            Collection<LoadedAnalyzer> loadedAnalyzers = engine.getLoadedAnalyzers();
            for (LoadedAnalyzer loadedAnalyzer : loadedAnalyzers) {
                analyzerDefaultTableModel.addRow(new Object[]{
                    loadedAnalyzer.key(),
                    loadedAnalyzer.name(),
                    loadedAnalyzer.version()
                });
            }

            rulesDefaultTableModel.addTableModelListener(e -> {
                controller.changed();
                int column = e.getColumn();

                if (column == 0) {
                    int firstRow = e.getFirstRow();
                    RuleKey ruleKey = RuleKey.parse(
                            rulesDefaultTableModel.getValueAt(firstRow, 3).toString()
                    );
                    Object valueAt = rulesDefaultTableModel.getValueAt(firstRow, column);
                    ruleKeyChanged.put(ruleKey, (Boolean) valueAt);
                }
            });

            categoriesList.addListSelectionListener((e) -> {
                if ("Rules".equals(categoriesList.getSelectedValue())) {
                    initRulesPanel(engine);
                }
                if ("Analyzers".equals(categoriesList.getSelectedValue())) {
                    initAnalyzersPanel();
                }
                optionPanel.revalidate();
                optionPanel.repaint();
            });

            // Rule panel by default
            initRulesPanel(engine);
            optionPanel.revalidate();
            optionPanel.repaint();
        });

    }

    private void initAnalyzersPanel() {
        optionPanel.removeAll();
        JTable analyzersTable = new JTable(analyzerDefaultTableModel);
        optionPanel.add(analyzersTable.getTableHeader(), BorderLayout.NORTH);
        optionPanel.add(analyzersTable, BorderLayout.CENTER);
    }

    private void initRulesPanel(SonarLintEngine engine) {
        optionPanel.removeAll();
        
        JPanel languageKeyContainer = new JPanel(new FlowLayout());
        JTextField rulesFilter = new JTextField();
        rulesFilter.setColumns(20);
        JComboBox<String> comboLanguageKey = new JComboBox<>();
        engine.getAllRuleDetails().stream()
            .map(r -> r.getLanguageKey())
            .distinct()
            .forEach(comboLanguageKey::addItem);
        rulesFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                rulesDefaultTableModel.setRules(engine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText());
            }
        });
        comboLanguageKey.addActionListener(
            e ->
            rulesDefaultTableModel.setRules(engine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText())
        );
        languageKeyContainer.add(new JLabel("language key: "));
        languageKeyContainer.add(comboLanguageKey);
        languageKeyContainer.add(new JSeparator());
        languageKeyContainer.add(new JLabel("filter: "));
        languageKeyContainer.add(rulesFilter);
        
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        JTable rulesTable = new JTable(rulesDefaultTableModel);
        rulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        rulesTable.getColumnModel().getColumn(1).setMaxWidth(250);
        rulesTable.getColumnModel().getColumn(2).setMaxWidth(200);
        rulesTable.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
                String severity = String.valueOf(value);
                Optional<ImageIcon> toImageIcon = SonarLintUtils.toImageIcon(severity);
                if (toImageIcon.isPresent()) {
                    defaultTableCellRenderer.setIcon(toImageIcon.get());
                }
                return defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        rulesDefaultTableModel.setRules(engine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText());
        northContainer.add(languageKeyContainer);
        northContainer.add(rulesTable.getTableHeader());
        optionPanel.add(northContainer, BorderLayout.NORTH);
        optionPanel.add(rulesTable, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoriesPanel = new javax.swing.JPanel();
        categoriesLabel = new javax.swing.JLabel();
        categoriesScrollPanel = new javax.swing.JScrollPane();
        categoriesList = new javax.swing.JList<>();
        optionScrollPane = new javax.swing.JScrollPane();
        optionPanel = new javax.swing.JPanel();

        setPreferredSize(getPreferredSize());
        setLayout(new java.awt.BorderLayout(10, 0));

        categoriesPanel.setLayout(new javax.swing.BoxLayout(categoriesPanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(categoriesLabel, org.openide.util.NbBundle.getMessage(SonarLintPanel.class, "SonarLintPanel.categoriesLabel.text")); // NOI18N
        categoriesPanel.add(categoriesLabel);

        categoriesList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Rules", "Analyzers" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        categoriesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoriesList.setSelectedIndex(0);
        categoriesScrollPanel.setViewportView(categoriesList);

        categoriesPanel.add(categoriesScrollPanel);

        add(categoriesPanel, java.awt.BorderLayout.WEST);

        optionScrollPane.setViewportView(null);

        optionPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        optionPanel.setLayout(new java.awt.BorderLayout());
        optionScrollPane.setViewportView(optionPanel);

        add(optionScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        // TODO read settings and initialize GUI
        // Example:
        // someCheckBox.setSelected(Preferences.userNodeForPackage(SonarLintPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(SonarLintPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    void store() {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        List<RuleKey> ruleKeysEnable = new ArrayList<>();
        List<RuleKey> ruleKeysDisable = new ArrayList<>();
        ruleKeyChanged.forEach((ruleKey, enable) -> {
            if (enable) {
                ruleKeysEnable.add(ruleKey);
            } else {
                ruleKeysDisable.add(ruleKey);
            }
        });

        sonarLintEngine.excludeRuleKeys(ruleKeysDisable);
        sonarLintEngine.includeRuleKeys(ruleKeysEnable);
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel categoriesLabel;
    javax.swing.JList<String> categoriesList;
    javax.swing.JPanel categoriesPanel;
    javax.swing.JScrollPane categoriesScrollPanel;
    javax.swing.JPanel optionPanel;
    javax.swing.JScrollPane optionScrollPane;
    // End of variables declaration//GEN-END:variables
}
