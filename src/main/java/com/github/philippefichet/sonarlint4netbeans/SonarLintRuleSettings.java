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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.editor.NbEditorUI;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRuleSettings extends javax.swing.JDialog {

    /**
     * Creates new form SonarLintRuleParameters
     */
    public SonarLintRuleSettings(
        SonarLintOptions sonarLintOptions,
        SonarLintEngine sonarLintEngine,
        String ruleKey
    ) {
        super((Frame)null, true);
        initComponents();
        Optional<StandaloneRuleDetails> ruleDetailsOptional = sonarLintEngine.getRuleDetails(ruleKey);
        if (ruleDetailsOptional.isPresent()) {
            StandaloneRuleDetails ruleDetails = ruleDetailsOptional.get();
            initComponents(sonarLintOptions, sonarLintEngine, ruleDetails);
        } else {
            mainTitle.setText("Rule \"" + ruleKey + "\" is not found");
        }
        ruleName.setMaximumSize(ruleName.getPreferredSize());
        setSize(800, 600);
        this.setLocationRelativeTo(NbEditorUI.getParentFrame(this));
    }

    private void enableHyperlinkOnRuleDescription() {
        // Enable hyperlink on rule description
        if (Desktop.isDesktopSupported()) {
            ruleDescription.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (URISyntaxException | IOException ex) {
                            ErrorManager.getDefault().log("Unable to open browser on URL: " + e.getURL());
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    }

    private void initComponents(SonarLintOptions sonarLintOptions, SonarLintEngine sonarLintEngine, StandaloneRuleDetails ruleDetails) {
        enableHyperlinkOnRuleDescription();
        String ruleKey = ruleDetails.getKey();
        // Set rule description and dialog
        String customCss = SonarLintUtils.toRuleDetailsStyleSheet(sonarLintOptions);
        String html = SonarLintUtils.toHtmlDescription(ruleDetails);
        ruleDescription.setText(customCss + html);
        setTitle(ruleKey + ": " + ruleDetails.getName());
        DefaultTableModel tableModel = (DefaultTableModel)ruleParametersTable.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // parameter description in tooltip on name column
        ruleParametersTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel tableCellRendererComponent = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Optional<StandaloneRuleParam> ruleParameter = SonarLintUtils.searchRuleParameter(ruleDetails, (String)value);
                if (ruleParameter.isPresent()) {
                    tableCellRendererComponent.setToolTipText(ruleParameter.get().description());
                }
                return tableCellRendererComponent;
            }
        });
        for (StandaloneRuleParam param : ruleDetails.paramDetails()) {
            String defaultValue = param.defaultValue();
            Optional<String> ruleParameter = sonarLintEngine.getRuleParameter(ruleKey, param.key());
            tableModel.addRow(new Object[] {
                param.key(),
                ruleParameter.orElse(""),
                defaultValue,
                param.description()
            });
        }
        if (tableModel.getRowCount() > 0) {
            tableModel.addTableModelListener(tableModelEvent -> {
                String parameterName = (String)tableModel.getValueAt(tableModelEvent.getFirstRow(), 0);
                String parameterValue = (String)tableModel.getValueAt(tableModelEvent.getFirstRow(), 1);
                if (parameterValue != null) {
                    if (parameterValue.isEmpty()) {
                        sonarLintEngine.removeRuleParameter(ruleKey, parameterName);
                    } else {
                        sonarLintEngine.setRuleParameter(ruleKey, parameterName, parameterValue);
                    }
                }
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ruleName = new javax.swing.JPanel();
        mainTitle = new javax.swing.JLabel();
        ruleParametersTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        ruleDescription = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        ruleName.setMaximumSize(ruleName.getPreferredSize());
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout();
        flowLayout1.setAlignOnBaseline(true);
        ruleName.setLayout(flowLayout1);

        org.openide.awt.Mnemonics.setLocalizedText(mainTitle, org.openide.util.NbBundle.getMessage(SonarLintRuleSettings.class, "SonarLintRuleSettings.mainTitle.text")); // NOI18N
        ruleName.add(mainTitle);

        getContentPane().add(ruleName);

        ruleParametersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Name", "Value", "Default value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ruleParametersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        getContentPane().add(ruleParametersTable.getTableHeader());
        getContentPane().add(ruleParametersTable);
        if (ruleParametersTable.getColumnModel().getColumnCount() > 0) {
            ruleParametersTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SonarLintRuleSettings.class, "SonarLintRuleSettings.ruleParametersTable.columnModel.title0_1")); // NOI18N
            ruleParametersTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SonarLintRuleSettings.class, "SonarLintRuleSettings.ruleParametersTable.columnModel.title1_1")); // NOI18N
            ruleParametersTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(SonarLintRuleSettings.class, "SonarLintRuleParameters.parametersTable.columnModel.title3_1")); // NOI18N
        }

        ruleDescription.setEditable(false);
        ruleDescription.setContentType("text/html");
        jScrollPane1.setViewportView(ruleDescription);

        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel mainTitle;
    private javax.swing.JEditorPane ruleDescription;
    private javax.swing.JPanel ruleName;
    private javax.swing.JTable ruleParametersTable;
    // End of variables declaration//GEN-END:variables
}
