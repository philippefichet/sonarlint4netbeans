/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2022 Philippe FICHET.
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

import com.github.philippefichet.sonarlint4netbeans.SonarLintAnalyzersTableModel;
import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import java.util.Collection;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@SuppressWarnings({
    "java:S1450" // "Private fields used as local variables in methods" disabled because managed by netbeans
})
public class SonarLintOptionsPanelAnalyzers extends javax.swing.JPanel {
    private SonarLintAnalyzersTableModel analyzerDefaultTableModel = new SonarLintAnalyzersTableModel();

    /**
     * Creates new form SonarLintOptionsPanelAnalyzers
     * @param sonarLintEngine instance of SonarLintEngine used to retrieve information from plugin
     */
    public SonarLintOptionsPanelAnalyzers(SonarLintEngine sonarLintEngine) {
        sonarLintEngine.whenInitialized(engine -> {
            Collection<PluginDetails> loadedAnalyzers = engine.getPluginDetails();
            loadedAnalyzers.forEach(analyzerDefaultTableModel::addPluginDetails);
        });
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        analyzersTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        analyzersTable.setModel(analyzerDefaultTableModel);
        jScrollPane1.setViewportView(analyzersTable);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable analyzersTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
