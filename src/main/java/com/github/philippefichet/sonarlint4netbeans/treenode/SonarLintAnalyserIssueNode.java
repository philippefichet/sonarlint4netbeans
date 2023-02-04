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
package com.github.philippefichet.sonarlint4netbeans.treenode;

import com.github.philippefichet.sonarlint4netbeans.SonarLintAnalyzerOpenIssueInFileAction;
import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import java.awt.Image;
import java.io.File;
import java.util.Optional;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueNode extends AbstractNode {
    private final DefaultClientIssue issue;
    private final Sheet.Set ruleNamePropertySet = new Sheet.Set();
    private final Sheet.Set locationPropertySet = new Sheet.Set();
    private final Sheet.Set typePropertySet = new Sheet.Set();
    private final Sheet.Set severityPropertySet = new Sheet.Set();
    private final PropertySet[] propertySets = new PropertySet[] {
        locationPropertySet,
        typePropertySet,
        severityPropertySet,
        ruleNamePropertySet
    };
    private final File file;
    private final SonarLintDataManager sonarLintDataManager;

    public SonarLintAnalyserIssueNode(DefaultClientIssue issue, String ruleName) {
        super(Children.LEAF);
        this.issue = issue;
        sonarLintDataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
        ClientInputFile inputFile = issue.getInputFile();
        Integer startLine = issue.getStartLine();
        Integer startLineOffset = issue.getStartLineOffset();
        String prefixDisplayName = ": ";
        if (startLine != null && startLineOffset != null) {
            prefixDisplayName = startLine + ":" + startLineOffset + ": ";
        }
        if (inputFile != null) {
            setDisplayName(prefixDisplayName + inputFile.relativePath());
            file = Utilities.toFile(inputFile.uri());
        } else {
            setDisplayName(prefixDisplayName + "Unkown file");
            file = null;
        }
        ruleNamePropertySet.setName(RuleNameProperty.NAME);
        ruleNamePropertySet.setDisplayName(RuleNameProperty.DISPLAY_NAME);
        ruleNamePropertySet.setShortDescription(RuleNameProperty.DISPLAY_NAME);
        ruleNamePropertySet.put(new RuleNameProperty(ruleName));
        locationPropertySet.setName(LocationProperty.NAME);
        locationPropertySet.setDisplayName(LocationProperty.DISPLAY_NAME);
        locationPropertySet.setShortDescription(LocationProperty.DISPLAY_NAME);
        locationPropertySet.put(new LocationProperty(issue));
        typePropertySet.setName(TypeProperty.NAME);
        typePropertySet.setDisplayName(TypeProperty.DISPLAY_NAME);
        typePropertySet.setShortDescription(TypeProperty.DISPLAY_NAME);
        typePropertySet.put(new TypeProperty(issue));
        severityPropertySet.setName(SeverityProperty.NAME);
        severityPropertySet.setDisplayName(SeverityProperty.DISPLAY_NAME);
        severityPropertySet.setShortDescription(SeverityProperty.DISPLAY_NAME);
        severityPropertySet.put(new SeverityProperty(issue.getSeverity().name()));
    }

    @Override
    public PropertySet[] getPropertySets() {
        return propertySets;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(SonarLintAnalyzerOpenIssueInFileAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(SonarLintAnalyzerOpenIssueInFileAction.class);
    }

    public DefaultClientIssue getIssue() {
        return issue;
    }

    @Override
    public Image getIcon(int type) {
        Optional<Image> icon = sonarLintDataManager.getIcon(file, type);
        if (icon.isPresent()) {
            return icon.get();
        } else {
            return super.getIcon(type);
        }
    }
}