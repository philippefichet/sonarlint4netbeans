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

import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.SonarLintOptions;
import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ListSelectionEvent;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.commons.Version;

public final class SonarLintPanel extends javax.swing.JPanel {

    private final SonarLintPanelChangedListener changedListener;
    private final Project project;

    private final Map<RuleKey, Boolean> ruleKeyChanged = new HashMap<>();
    private final Map<String, String> extraProperties = new HashMap<>();
    private final Map<String, String> additionnalPlugins = new HashMap<>();
    private String nodeJSPathToSave;
    private Version nodeJSVersionToSave;
    private Boolean applyDifferentRulesOnTestFiles = null;
    private final AtomicBoolean loadingPanel = new AtomicBoolean(true);

    private final SonarLintRuleTableModel rulesDefaultTableModel = new SonarLintRuleTableModel();

    public SonarLintPanel(SonarLintPanelChangedListener changedListener) {
        this(changedListener, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
    }

    public SonarLintPanel(SonarLintPanelChangedListener changedListener, Project project) {
        this.changedListener = changedListener;
        this.project = project;
        initComponents();
        showLoadingPanel();

        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        additionnalPlugins.putAll(sonarLintEngine.getAdditionnalPlugins());
        extraProperties.putAll(sonarLintEngine.getAllExtraProperties(project));
        rulesDefaultTableModel.addTableModelListener(e -> {
            this.changedListener.changed();
            int column = e.getColumn();
            if (column == 0) {
                int firstRow = e.getFirstRow();
                RuleKey ruleKey = RuleKey.parse(
                    rulesDefaultTableModel.getRuleKeyValueAt(firstRow).toString()
                );
                Object valueAt = rulesDefaultTableModel.getValueAt(firstRow, column);
                ruleKeyChanged.put(ruleKey, (Boolean) valueAt);
            }
        });
        categoriesList.addListSelectionListener((ListSelectionEvent e) -> {
            if (!loadingPanel.get()) {
                showPanelFromCategory(sonarLintEngine, categoriesList.getSelectedValue());
            }
        });
        sonarLintEngine.whenInitialized(this::whenInitialized);
    }

    private void whenInitialized(SonarLintEngine engine)
    {
        engine.whenRestarted(this::whenRestarted);
        loadingPanel.set(false);
        String category = categoriesList.getSelectedValue();
        if (category == null) {
            categoriesList.setSelectedIndex(0);
            category = categoriesList.getSelectedValue();
        }
        showPanelFromCategory(engine, category);
    }

    private void showPanelFromCategory(SonarLintEngine engine, String category)
    {
        if ("Rules".equals(category)) {
            initRulesPanel(engine);
        }
        if ("Analyzers".equals(category)) {
            initAnalyzersPanel(engine);
        }
        if ("Options".equals(category)) {
            initOptionsPanel(engine);
        }
        if ("Properties".equals(category)) {
            initPropertiesPanel(engine);
        }
        if ("Plugins".equals(category)) {
            initPluginsPanel(engine);
        }
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    private void whenRestarted(SonarLintEngine engine)
    {
        engine.whenInitialized(this::whenInitialized);
        loadingPanel.set(true);
        showLoadingPanel();
    }

    private void showLoadingPanel() {
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.PAGE_AXIS));
        JProgressBar waiting = new JProgressBar();
        waiting.setIndeterminate(true);
        JLabel loadingText = new JLabel("Loading ...");
        loadingPanel.add(loadingText);
        loadingPanel.add(waiting);
        optionPanel.removeAll();
        optionPanel.add(loadingPanel, BorderLayout.NORTH);
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    private void initAnalyzersPanel(SonarLintEngine sonarLintEngine) {
        optionPanel.removeAll();
        optionPanel.add(new SonarLintOptionsPanelAnalyzers(sonarLintEngine), BorderLayout.CENTER);
    }

    private void initOptionsPanel(SonarLintEngine engine) {
        optionPanel.removeAll();
        SonarLintOptionsPanelOptions container = new SonarLintOptionsPanelOptions(engine, new SonarLintOptionsPanelOptionsListener() {
            @Override
            public void nodeJSOptionsChanged(String nodeJSPath, Version nodeJSVersion) {
                nodeJSPathToSave = nodeJSPath;
                nodeJSVersionToSave = nodeJSVersion;
                changedListener.changed();
            }

            @Override
            public void testRulesOptionsChanged(Boolean apply) {
                applyDifferentRulesOnTestFiles = apply;
                changedListener.changed();
            }
        });
        optionPanel.add(container, BorderLayout.NORTH);
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    private void initRulesPanel(SonarLintEngine sonarLintEngine) {
        optionPanel.removeAll();
        SonarLintRuleListPanel sonarLintRuleListPanel = new SonarLintRuleListPanel(
            (RuleKey ruleKey, boolean enabled) -> {
                this.changedListener.changed();
                ruleKeyChanged.put(ruleKey, enabled);
            },
            (String ruleKeyChanged, String parameterName, String parameterValue) -> {
                SonarLintUtils.changeRuleParameterValue(sonarLintEngine, project, ruleKeyChanged, parameterName, parameterValue);
            },
            sonarLintEngine,
            project
        );
        optionPanel.add(sonarLintRuleListPanel, BorderLayout.CENTER);
    }

    private void initPropertiesPanel(SonarLintEngine sonarLintEngine) {
        optionPanel.removeAll();
        optionPanel.add(
            new SonarLintOptionsPanelProperties(
                sonarLintEngine.getAllExtraProperties(project),
                (Map<String, String> extraProperties) -> {
                    this.extraProperties.clear();
                    this.extraProperties.putAll(extraProperties);
                    this.changedListener.changed();
                }
            )
        );
    }

    private void initPluginsPanel(SonarLintEngine sonarLintEngine) {
        optionPanel.removeAll();
        optionPanel.add(
            new SonarLintOptionsPanelPlugins(
                sonarLintEngine.getBasePlugins(),
                sonarLintEngine.getAdditionnalPlugins(),
                (Map<String, String> additionnalPlugins) -> {
                    this.additionnalPlugins.clear();
                    this.additionnalPlugins.putAll(additionnalPlugins);
                    this.changedListener.changed();
                }
            )
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        categoriesPanel = new javax.swing.JPanel();
        categoriesScrollPanel = new javax.swing.JScrollPane();
        categoriesList = new javax.swing.JList<>();
        categoriesLabel = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        optionScrollPane = new javax.swing.JScrollPane();
        optionPanel = new javax.swing.JPanel();

        leftPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        categoriesPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        categoriesList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Options", "Rules", "Analyzers", "Properties", "Plugins" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        categoriesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoriesScrollPanel.setViewportView(categoriesList);

        org.openide.awt.Mnemonics.setLocalizedText(categoriesLabel, org.openide.util.NbBundle.getMessage(SonarLintPanel.class, "SonarLintPanel.categoriesLabel.text")); // NOI18N

        javax.swing.GroupLayout categoriesPanelLayout = new javax.swing.GroupLayout(categoriesPanel);
        categoriesPanel.setLayout(categoriesPanelLayout);
        categoriesPanelLayout.setHorizontalGroup(
            categoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(categoriesPanelLayout.createSequentialGroup()
                .addGroup(categoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoriesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(categoriesScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        categoriesPanelLayout.setVerticalGroup(
            categoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(categoriesPanelLayout.createSequentialGroup()
                .addComponent(categoriesLabel)
                .addGap(0, 0, 0)
                .addComponent(categoriesScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(categoriesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(categoriesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        rightPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        optionScrollPane.setViewportView(null);

        optionPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        optionPanel.setLayout(new java.awt.BorderLayout());
        optionScrollPane.setViewportView(optionPanel);

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(optionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(optionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
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
        SonarLintOptions sonarLintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        SonarLintUtils.saveEnabledOrDisabledRules(sonarLintEngine, project, ruleKeyChanged);
        if (project == SonarLintEngine.GLOBAL_SETTINGS_PROJECT && sonarLintOptions != null && applyDifferentRulesOnTestFiles != null) {
            sonarLintOptions.useDifferentRulesOnTestFiles(applyDifferentRulesOnTestFiles);
        }
        if (project == SonarLintEngine.GLOBAL_SETTINGS_PROJECT && nodeJSPathToSave != null && nodeJSVersionToSave != null) {
            sonarLintEngine.setNodeJSPathAndVersion(nodeJSPathToSave, nodeJSVersionToSave);
        }
        sonarLintEngine.setAllExtraProperties(extraProperties, project);
        sonarLintEngine.setAdditionnalPlugins(additionnalPlugins);
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
    javax.swing.JPanel leftPanel;
    javax.swing.JPanel optionPanel;
    javax.swing.JScrollPane optionScrollPane;
    javax.swing.JPanel rightPanel;
    // End of variables declaration//GEN-END:variables
}
