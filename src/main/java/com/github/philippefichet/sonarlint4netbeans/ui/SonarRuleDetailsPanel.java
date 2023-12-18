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

import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.SonarLintListMouseAdapter;
import com.github.philippefichet.sonarlint4netbeans.SonarLintOptions;
import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import com.github.philippefichet.sonarlint4netbeans.ui.renderer.SonarLintListCellRenderer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;
import javax.swing.DefaultListModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;

/**
 * Panel used to show rules and details
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@SuppressWarnings({
    "java:S1450" // "Private fields used as local variables in methods" disabled because managed by netbeans
})
public class SonarRuleDetailsPanel extends javax.swing.JPanel {
    private final SonarLintEngine sonarLintEngine;
    private final Project project;

    // TODO See later to manage the remote case and limit interaction

    /**
     * Creates new form SonarRuleDetailsPanel
     * @param sonarLintEngine SonarLintEngine instance dealing with the retrieval or modification of information
     * @param project Targeted project for information retrieval or modification
     */
    public SonarRuleDetailsPanel(SonarLintEngine sonarLintEngine, Project project) {
        this.sonarLintEngine = sonarLintEngine;
        this.project = project;
        initComponents();
        SonarLintOptions sonarLintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        try {
            sonarLintOptions.getSonarLintDetailsStyle().addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    // Reset previous stylesheet
                    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
                    Document createDefaultDocument = htmlEditorKit.createDefaultDocument();
                    sonarLintRuleDetailsEditor.setDocument(createDefaultDocument);
                    // Update editor
                    sonarLintAllRulesValueChanged(null);
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Project getProject() {
        return project;
    }
    
    public void setSonarRuleKeyFilter(String sonarRuleKeyFilter)
    {
        this.sonarRuleKeyFilter.setText(sonarRuleKeyFilter);
        ruleKeyFilter = this.sonarRuleKeyFilter.getText().toLowerCase();
        initListAllRuleDetails();
        sonarLintAllRules.setSelectedValue(sonarRuleKeyFilter, true);
        updateUI();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({
        "unchecked",
        "java:S1604", // "Anonymous inner classes containing only one method should become lambdas" disabled because managed by netbeans
        "java:S1161", // '"@Override" should be used on overriding and implementing methods' disabled because managed by netbeans
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sonarRuleKeyFilter = new javax.swing.JTextField();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        sonarLintRuleDetailsEditor = new javax.swing.JEditorPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        sonarLintRuleDetailsEditorHtmlSource = new javax.swing.JEditorPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        sonarLintAllRules = new javax.swing.JList<>();

        sonarRuleKeyFilter.setText(org.openide.util.NbBundle.getMessage(SonarRuleDetailsPanel.class, "SonarRuleDetailsPanel.sonarRuleKeyFilter.text")); // NOI18N
        sonarRuleKeyFilter.setToolTipText(org.openide.util.NbBundle.getMessage(SonarRuleDetailsPanel.class, "SonarRuleDetailsPanel.sonarRuleKeyFilter.toolTipText")); // NOI18N
        sonarRuleKeyFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sonarRuleKeyFilterKeyReleased(evt);
            }
        });

        sonarLintRuleDetailsEditor.setEditable(false);
        sonarLintRuleDetailsEditor.setContentType("text/html");
        jScrollPane1.setViewportView(sonarLintRuleDetailsEditor);
        if (Desktop.isDesktopSupported()) {
            sonarLintRuleDetailsEditor.addHyperlinkListener(new DesktopHyperlinkListener());
        }

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SonarRuleDetailsPanel.class, "SonarRuleDetailsPanel.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        sonarLintRuleDetailsEditorHtmlSource.setEditable(false);
        sonarLintRuleDetailsEditorHtmlSource.setContentType("text/plain");
        jScrollPane3.setViewportView(sonarLintRuleDetailsEditorHtmlSource);
        if (Desktop.isDesktopSupported()) {
            sonarLintRuleDetailsEditor.addHyperlinkListener(new HyperlinkListener() {
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

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SonarRuleDetailsPanel.class, "SonarRuleDetailsPanel.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);

        initListAllRuleDetailsRenderer();
        initListAllRuleDetails();
        sonarLintAllRules.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sonarLintAllRules.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                sonarLintAllRulesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(sonarLintAllRules);

        jSplitPane1.setLeftComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jSplitPane1)
                            .addContainerGap())
                        .addComponent(sonarRuleKeyFilter))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(sonarRuleKeyFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSplitPane1)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings({
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    private void sonarRuleKeyFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sonarRuleKeyFilterKeyReleased
        ruleKeyFilter = sonarRuleKeyFilter.getText().toLowerCase();
        initListAllRuleDetails();
    }//GEN-LAST:event_sonarRuleKeyFilterKeyReleased
    
    private void initListAllRuleDetails() {
        sonarLintEngine.whenInitialized((SonarLintEngine engine) -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            Collection<StandaloneRuleDetails> rules = engine.getAllRuleDetails();
            rules.stream().sorted((r1, r2) -> r1.getKey().compareTo(r2.getKey()))
            .filter(SonarLintUtils.FilterBy.keyAndName(ruleKeyFilter))
            .forEach(rule -> model.addElement(rule.getKey()));
            sonarLintAllRules.setModel(model);
            sonarLintAllRules.updateUI();
        });
    }

    private void initListAllRuleDetailsRenderer()
    {
        SonarLintOptions sonarLintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        sonarLintEngine.whenConfigurationChanged(engine -> sonarLintAllRules.repaint());
        sonarLintAllRules.setCellRenderer(new SonarLintListCellRenderer(sonarLintEngine, project));
        sonarLintAllRules.addMouseListener(new SonarLintListMouseAdapter(sonarLintAllRules, sonarLintOptions, sonarLintEngine, project));
    }

    @SuppressWarnings({
        "java:S1172" // "Unused method parameters should be removed" disabled because managed by netbeans
    })
    private void sonarLintAllRulesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_sonarLintAllRulesValueChanged
        String selectedValue = sonarLintAllRules.getSelectedValue();
        SonarLintOptions sonarLintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        sonarLintEngine.whenInitialized((SonarLintEngine engine) -> {
            Optional<StandaloneRuleDetails> optionalRuleDetails = engine.getRuleDetails(selectedValue);
            if (optionalRuleDetails.isPresent()) {
                StandaloneRuleDetails ruleDetails = optionalRuleDetails.get();
                String customCss = SonarLintUtils.toRuleDetailsStyleSheet(sonarLintOptions);
                String html = SonarLintUtils.toHtmlDescription(
                    ruleDetails,
                    SonarLintUtils.extractRuleParameters(sonarLintEngine, ruleDetails.getKey(), project)
                );
                sonarLintRuleDetailsEditor.setText(customCss + html);
                sonarLintRuleDetailsEditorHtmlSource.setText(html);
                sonarLintRuleDetailsEditor.getCaret().moveDot(0);
            }
        });
    }//GEN-LAST:event_sonarLintAllRulesValueChanged

    private String ruleKeyFilter = "";
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<String> sonarLintAllRules;
    private javax.swing.JEditorPane sonarLintRuleDetailsEditor;
    private javax.swing.JEditorPane sonarLintRuleDetailsEditorHtmlSource;
    private javax.swing.JTextField sonarRuleKeyFilter;
    // End of variables declaration//GEN-END:variables
}
