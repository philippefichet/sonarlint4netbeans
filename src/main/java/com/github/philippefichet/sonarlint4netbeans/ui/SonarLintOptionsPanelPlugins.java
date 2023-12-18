/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
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

import com.github.philippefichet.sonarlint4netbeans.SonarLintPropertiesTableModel;
import com.github.philippefichet.sonarlint4netbeans.ui.listener.SonarLintOptionsPanelPluginsListener;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@SuppressWarnings({
    "java:S1450" // "Private fields used as local variables in methods" disabled because managed by netbeans
})
public class SonarLintOptionsPanelPlugins extends javax.swing.JPanel {

    private final SonarLintPropertiesTableModel sonarLintPropertiesTableModel = new SonarLintPropertiesTableModel("Plugin key", "Plugin path");
    private final String baseInformation;
    private final Map<String, String> pluginStatus = Collections.synchronizedMap(new HashMap<>());
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    /**
     * Creates new form SonarLintOptionsPanelPlugins
     * @param basePlugin List of plugins provided as standard
     * @param additionnalPlugins List of plugins added by the user
     * @param listener listener to modify the list of plugins
     */
    public SonarLintOptionsPanelPlugins(Set<String> basePlugin, Map<String, String> additionnalPlugins, SonarLintOptionsPanelPluginsListener listener) {
        StringJoiner basePluginJoiner = new StringJoiner(", ");
        basePlugin.forEach(basePluginJoiner::add);
        baseInformation = "Followed plugin key are provided by sonarlint4netbeans : " + basePluginJoiner.toString() + ".<br/>"
            + "You can overload their use (more updated version, ...)";
        initComponents();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        Document createDefaultDocument = htmlEditorKit.createDefaultDocument();
        informationTextPane.setDocument(createDefaultDocument);
        sonarLintPropertiesTableModel.setProperties(additionnalPlugins);
        sonarLintPropertiesTableModel.addTableModelListener(
            e -> {
                if (e.getType() == TableModelEvent.INSERT 
                    || e.getType() == TableModelEvent.UPDATE) {
                    for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
                        detectAndGetErrors(
                            sonarLintPropertiesTableModel.getPropertyKey(i),
                            sonarLintPropertiesTableModel.getPropertyValue(i)
                        );
                    }
                }
                listener.changed(sonarLintPropertiesTableModel.toPropertiesMap());
                updateInformationTextPane();
            }
        );
        sonarLintPropertiesTableModel.forEach(this::detectAndGetErrors);
        updateInformationTextPane();
    }

    private void updateInformationTextPane()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
            .append(baseInformation)
            .append("<br/>");
        sonarLintPropertiesTableModel.forEach(
            (String key, String url) ->
            sb.append("Plugin \"")
                    .append(key)
                    .append("\" check status : ")
                .append(pluginStatus.get(key))
                .append("<br/>")
        );
        sb.append("</html>");
        informationTextPane.setText(sb.toString());
    }

    private void detectAndGetErrors(String key, String url)
    {
        pluginStatus.put(key, "running ...");
        EXECUTOR_SERVICE.submit(() -> {
            try {
                Paths.get(url).toRealPath();
                pluginStatus.put(key, "OK");
            } catch (NoSuchFileException ex) {
                pluginStatus.put(key, "Path \"" + url + "\" not exists");
            } catch (InvalidPathException ex) {
                pluginStatus.put(key, "Mal formed path : " + url);
            } catch (IOException ex) {
                pluginStatus.put(key, "Error with path : " + url + " : " + ex.getMessage());
            }
            SwingUtilities.invokeLater(this::updateInformationTextPane);
        });
        updateInformationTextPane();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({
        "unchecked",
        "java:S1604",
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        pluginKeyPanel = new javax.swing.JPanel();
        pluginKeyTextField = new javax.swing.JTextField();
        pluginURLPanel = new javax.swing.JPanel();
        pluginURLTextField = new javax.swing.JTextField();
        additionnalPluginsJScrollPane = new javax.swing.JScrollPane();
        additionnalPluginsJTable = new javax.swing.JTable();
        informationsPanel = new javax.swing.JPanel();
        informationScrollPane = new javax.swing.JScrollPane();
        informationTextPane = new javax.swing.JEditorPane();

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.removeButton.text")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.removeButton.toolTipText")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        pluginKeyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.pluginKeyPanel.border.title"))); // NOI18N

        pluginKeyTextField.setText(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.pluginKeyTextField.text")); // NOI18N

        javax.swing.GroupLayout pluginKeyPanelLayout = new javax.swing.GroupLayout(pluginKeyPanel);
        pluginKeyPanel.setLayout(pluginKeyPanelLayout);
        pluginKeyPanelLayout.setHorizontalGroup(
            pluginKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginKeyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginKeyTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                .addContainerGap())
        );
        pluginKeyPanelLayout.setVerticalGroup(
            pluginKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginKeyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pluginURLPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.pluginURLPanel.border.title"))); // NOI18N

        pluginURLTextField.setText(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.pluginURLTextField.text")); // NOI18N

        javax.swing.GroupLayout pluginURLPanelLayout = new javax.swing.GroupLayout(pluginURLPanel);
        pluginURLPanel.setLayout(pluginURLPanelLayout);
        pluginURLPanelLayout.setHorizontalGroup(
            pluginURLPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginURLPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginURLTextField)
                .addContainerGap())
        );
        pluginURLPanelLayout.setVerticalGroup(
            pluginURLPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginURLPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        additionnalPluginsJTable.setModel(sonarLintPropertiesTableModel);
        additionnalPluginsJScrollPane.setViewportView(additionnalPluginsJTable);
        if (additionnalPluginsJTable.getColumnModel().getColumnCount() > 0) {
            additionnalPluginsJTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.additionnalPluginsJTable.columnModel.title0")); // NOI18N
            additionnalPluginsJTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.additionnalPluginsJTable.columnModel.title1")); // NOI18N
        }

        informationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SonarLintOptionsPanelPlugins.class, "SonarLintOptionsPanelPlugins.informationsPanel.border.title"))); // NOI18N

        informationTextPane.setEditable(false);
        informationTextPane.setContentType("text/html"); // NOI18N
        informationScrollPane.setViewportView(informationTextPane);

        javax.swing.GroupLayout informationsPanelLayout = new javax.swing.GroupLayout(informationsPanel);
        informationsPanel.setLayout(informationsPanelLayout);
        informationsPanelLayout.setHorizontalGroup(
            informationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(informationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(informationScrollPane)
                .addContainerGap())
        );
        informationsPanelLayout.setVerticalGroup(
            informationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(informationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(informationScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(informationsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(additionnalPluginsJScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pluginKeyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pluginURLPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addButton)
                            .addComponent(removeButton))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(informationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pluginKeyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pluginURLPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionnalPluginsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings({
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        sonarLintPropertiesTableModel.addOrUpdateProperty(pluginKeyTextField.getText(), pluginURLTextField.getText());
    }//GEN-LAST:event_addButtonActionPerformed

    @SuppressWarnings({
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] selectedRows = additionnalPluginsJTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            pluginStatus.remove(sonarLintPropertiesTableModel.getPropertyKey(selectedRows[i]));
            sonarLintPropertiesTableModel.removeRow(selectedRows[i]);
        }
        additionnalPluginsJTable.revalidate();
        additionnalPluginsJTable.repaint();
    }//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane additionnalPluginsJScrollPane;
    private javax.swing.JTable additionnalPluginsJTable;
    private javax.swing.JScrollPane informationScrollPane;
    private javax.swing.JEditorPane informationTextPane;
    private javax.swing.JPanel informationsPanel;
    private javax.swing.JPanel pluginKeyPanel;
    private javax.swing.JTextField pluginKeyTextField;
    private javax.swing.JPanel pluginURLPanel;
    private javax.swing.JTextField pluginURLTextField;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
