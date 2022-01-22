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
package com.github.philippefichet.sonarlint4netbeans.treenode;

import com.github.philippefichet.sonarlint4netbeans.SonarLintAnalyzerOpenIssueInFileAction;
import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import javax.swing.Action;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.sonarsource.sonarlint.core.analyzer.issue.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;

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
    private DataObject dataObject = null;
    private final SonarLintDataManager sonarLintDataManager;

    public SonarLintAnalyserIssueNode(DefaultClientIssue issue) {
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
        ruleNamePropertySet.setName("ruleName");
        ruleNamePropertySet.setDisplayName("Rule name");
        ruleNamePropertySet.setShortDescription("Rule name");
        ruleNamePropertySet.put(new RuleNameProperty(issue.getRuleName()));
        locationPropertySet.setName("location");
        locationPropertySet.setDisplayName("Location");
        locationPropertySet.setShortDescription("Location");
        locationPropertySet.put(new LocationProperty(issue));
        typePropertySet.setName("type");
        typePropertySet.setDisplayName("Type");
        typePropertySet.setShortDescription("Type");
        typePropertySet.put(new TypeProperty(issue));
        severityPropertySet.setName("severity");
        severityPropertySet.setDisplayName("Severity");
        severityPropertySet.setShortDescription("Severity");
        severityPropertySet.put(new SeverityProperty(issue.getSeverity()));
    }

    public static final class LocationProperty extends PropertySupport.ReadOnly<String> {
        private final String value;
        public LocationProperty(DefaultClientIssue issue) {
            super("location", String.class, "Location", "Location");
            value = "start at line " + issue.getStartLine() + " and column " + issue.getStartLineOffset() + " to end at line " + issue.getEndLine() + " and column" + issue.getEndLineOffset();
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }
    }

    public static final class RuleNameProperty extends PropertySupport.ReadOnly<String>
    {
        private final String ruleName;
        
        public RuleNameProperty(String ruleName) {
            super("ruleName", String.class, "ruleName", "ruleName");
            this.ruleName = ruleName;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return ruleName;
        }
    }

    public static final class TypeProperty extends PropertySupport.ReadOnly<String> {
        private final String value;
        public TypeProperty(DefaultClientIssue issue) {
            super("type", String.class, "Type", "Type");
            value = issue.getType();
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }
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