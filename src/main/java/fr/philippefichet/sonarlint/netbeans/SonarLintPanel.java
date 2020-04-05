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
package fr.philippefichet.sonarlint.netbeans;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.connected.LoadedAnalyzer;

public final class SonarLintPanel extends javax.swing.JPanel {

    private final SonarLintOptionsPanelController controller;

    private final Map<RuleKey, Boolean> ruleKeyChanged = new HashMap<>();
    private DefaultTableModel analyzerDefaultTableModel = new DefaultTableModel();

    private DefaultTableModel rulesDefaultTableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column < 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }
    };

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

            rulesDefaultTableModel.addColumn("");
            rulesDefaultTableModel.addColumn("Language");
            rulesDefaultTableModel.addColumn("Key");
            rulesDefaultTableModel.addColumn("Details");
            Collection<RuleDetails> allRuleDetails = engine.getAllRuleDetails();
            allRuleDetails.stream().sorted((r1, r2) -> {
                return r1.getKey().compareTo(r2.getKey());
            }).map(ruleDetail -> new Object[]{
                !engine.isExcluded(ruleDetail),
                ruleDetail.getLanguageKey(),
                ruleDetail.getKey(),
                ruleDetail.getName()}
            ).collect(Collectors.toList()).forEach(rulesDefaultTableModel::addRow);

            rulesDefaultTableModel.addTableModelListener((e) -> {
                controller.changed();
                int column = e.getColumn();

                if (column == 0) {
                    int firstRow = e.getFirstRow();
                    RuleKey ruleKey = RuleKey.parse(
                            rulesDefaultTableModel.getValueAt(firstRow, 2).toString()
                    );
                    Object valueAt = rulesDefaultTableModel.getValueAt(firstRow, column);
                    ruleKeyChanged.put(ruleKey, (Boolean) valueAt);
                }
            });

            categoriesList.addListSelectionListener((e) -> {
                if ("Rules".equals(categoriesList.getSelectedValue())) {
                    initRulesPanel();
                }
                if ("Analyzers".equals(categoriesList.getSelectedValue())) {
                    initAnalyzersPanel();
                }
                optionPanel.revalidate();
                optionPanel.repaint();
            });

            // Rule panel by default
            initRulesPanel();
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

    private void initRulesPanel() {
        optionPanel.removeAll();
        JTable rulesTable = new JTable(rulesDefaultTableModel);
        rulesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        rulesTable.getColumnModel().getColumn(1).setMaxWidth(250);
        optionPanel.add(rulesTable.getTableHeader(), BorderLayout.NORTH);
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
        optionPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout(10, 0));

        categoriesPanel.setLayout(new javax.swing.BoxLayout(categoriesPanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(categoriesLabel, org.openide.util.NbBundle.getMessage(SonarLintPanel.class, "SonarLintPanel.categoriesLabel.text")); // NOI18N
        categoriesPanel.add(categoriesLabel);

        categoriesList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Rules", "Analyzers" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        categoriesScrollPanel.setViewportView(categoriesList);

        categoriesPanel.add(categoriesScrollPanel);

        add(categoriesPanel, java.awt.BorderLayout.WEST);

        optionPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        optionPanel.setLayout(new java.awt.BorderLayout());
        add(optionPanel, java.awt.BorderLayout.CENTER);
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
        sonarLintEngine.includeRuleKyes(ruleKeysEnable);
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel categoriesLabel;
    private javax.swing.JList<String> categoriesList;
    private javax.swing.JPanel categoriesPanel;
    private javax.swing.JScrollPane categoriesScrollPanel;
    private javax.swing.JPanel optionPanel;
    // End of variables declaration//GEN-END:variables
}
