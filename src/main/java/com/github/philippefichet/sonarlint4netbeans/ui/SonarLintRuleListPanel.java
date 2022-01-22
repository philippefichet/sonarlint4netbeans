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
package com.github.philippefichet.sonarlint4netbeans.ui;

import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.SonarLintOptions;
import com.github.philippefichet.sonarlint4netbeans.ui.listener.SonarLintRuleListPanelListener;
import com.github.philippefichet.sonarlint4netbeans.ui.listener.SonarLintRuleSettingsListener;
import com.github.philippefichet.sonarlint4netbeans.ui.renderer.SonarLintRuleKeyTableCellRenderer;
import com.github.philippefichet.sonarlint4netbeans.ui.renderer.SonarLintSettingsTableCellRenderer;
import com.github.philippefichet.sonarlint4netbeans.ui.renderer.SonarLintSeverityTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRuleListPanel extends javax.swing.JPanel {
    private final SonarLintRuleTableModel rulesDefaultTableModel = new SonarLintRuleTableModel();

    /**
     * Creates new form SonarLintRuleListPanel
     */
    public SonarLintRuleListPanel(
        SonarLintRuleListPanelListener sonarLintRuleListPanelListener,
        SonarLintRuleSettingsListener sonarLintRuleSettingsListener,
        SonarLintEngine sonarLintEngine,
        Project project
    ) {
        initComponents();
        sonarLintEngine.whenInitialized(engine -> {
            rulesDefaultTableModel.addTableModelListener(e -> {
                int column = e.getColumn();
                if (column == 0) {
                    int firstRow = e.getFirstRow();
                    RuleKey ruleKey = RuleKey.parse(
                        rulesDefaultTableModel.getRuleKeyValueAt(firstRow).toString()
                    );
                    Object valueAt = rulesDefaultTableModel.getValueAt(firstRow, column);
                    sonarLintRuleListPanelListener.ruleChanged(ruleKey, (Boolean) valueAt);
                }
            });
        });

        JPanel languageKeyContainer = new JPanel(new FlowLayout());
        JButton resetSelectedRule = new JButton("Restore to default");
        resetSelectedRule.setToolTipText("Selected only rule activated by default");
        JTextField rulesFilter = new JTextField();
        rulesFilter.setColumns(20);
        JComboBox<String> comboLanguageKey = new JComboBox<>();
        sonarLintEngine.getAllRuleDetails().stream()
            .map(r -> r.getLanguage().getLanguageKey())
            .distinct()
            .forEach(comboLanguageKey::addItem);
        rulesFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                rulesDefaultTableModel.setRules(sonarLintEngine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText(), project);
            }
        });
        comboLanguageKey.addActionListener(
            e ->
            rulesDefaultTableModel.setRules(sonarLintEngine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText(), project)
        );
        resetSelectedRule.addActionListener(
            e -> {
            sonarLintEngine.getAllRuleDetails().stream()
            .filter(rule -> rule.getLanguage().getLanguageKey().equals((String)comboLanguageKey.getSelectedItem()))
            .forEach(rule -> {
                RuleKey ruleKey = RuleKey.parse(rule.getKey());
                if (rule.isActiveByDefault()) {
                    sonarLintEngine.includeRuleKey(ruleKey, project);
                } else {
                    sonarLintEngine.excludeRuleKey(ruleKey, project);
                }
            });
            rulesDefaultTableModel.setRules(sonarLintEngine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText(), project);
        });
        languageKeyContainer.add(new JLabel("language key: "));
        languageKeyContainer.add(comboLanguageKey);
        languageKeyContainer.add(new JSeparator());
        languageKeyContainer.add(new JLabel("filter: "));
        languageKeyContainer.add(rulesFilter);
        languageKeyContainer.add(resetSelectedRule);
        
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        JTable rulesTable = new JTable(rulesDefaultTableModel);
        rulesTable.setRowHeight(rulesTable.getRowHeight() + 2);
        rulesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rulesTable.columnAtPoint(e.getPoint()) == SonarLintRuleTableModel.SETTINGS_COLUMN_INDEX) {
                    String ruleKey = (String)rulesTable.getValueAt(rulesTable.getSelectedRow(), SonarLintRuleTableModel.KEY_COLUMN_INDEX);
                    Optional<StandaloneRuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(ruleKey);
                    ruleDetails.ifPresent((StandaloneRuleDetails standaloneRule) -> {
                        if (!standaloneRule.paramDetails().isEmpty()) {
                            SonarLintOptions sonarlintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
                            SonarLintRuleSettings sonarLintRuleParameters = new SonarLintRuleSettings(
                                sonarlintOptions,
                                sonarLintEngine,
                                ruleKey,
                                sonarLintRuleSettingsListener,
                                project
                            );
                            sonarLintRuleParameters.setVisible(true);
                        }
                    });
                }
            }
        });
        TableColumnModel columnModel = rulesTable.getColumnModel();
        columnModel.getColumn(SonarLintRuleTableModel.ENABLE_COLUMN_INDEX).setWidth(50);
        columnModel.getColumn(SonarLintRuleTableModel.SETTINGS_COLUMN_INDEX).setWidth(50);
        columnModel.getColumn(SonarLintRuleTableModel.SETTINGS_COLUMN_INDEX).setCellRenderer(new SonarLintSettingsTableCellRenderer());
        columnModel.getColumn(SonarLintRuleTableModel.SEVERITY_COLUMN_INDEX).setWidth(100);
        columnModel.getColumn(SonarLintRuleTableModel.SEVERITY_COLUMN_INDEX).setCellRenderer(new SonarLintSeverityTableCellRenderer());
        columnModel.getColumn(SonarLintRuleTableModel.KEY_COLUMN_INDEX).setWidth(100);
        columnModel.getColumn(SonarLintRuleTableModel.KEY_COLUMN_INDEX).setCellRenderer(new SonarLintRuleKeyTableCellRenderer(sonarLintEngine, project));
        columnModel.getColumn(SonarLintRuleTableModel.SEVERITY_COLUMN_INDEX).setCellRenderer(new SonarLintSeverityTableCellRenderer());

        rulesDefaultTableModel.setRules(sonarLintEngine, (String)comboLanguageKey.getSelectedItem(), rulesFilter.getText(), project);
        northContainer.add(languageKeyContainer);
        northContainer.add(rulesTable.getTableHeader());
        add(northContainer, BorderLayout.NORTH);
        add(rulesTable, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
