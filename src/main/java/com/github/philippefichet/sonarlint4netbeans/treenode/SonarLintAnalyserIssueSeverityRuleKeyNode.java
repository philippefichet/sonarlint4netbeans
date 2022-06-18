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

import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueSeverityRuleKeyNode extends AbstractNode {

    private final String ruleKey;
    private final String ruleName;
    private final String type;
    private final SonarLintAnalyserIssueChildren children;
    private final Sheet.Set ruleNamePropertySet = new Sheet.Set();
    private final Sheet.Set typePropertySet = new Sheet.Set();
    private final Sheet.Set severityPropertySet = new Sheet.Set();
    private final PropertySet[] propertySets = new PropertySet[] {
        typePropertySet,
        severityPropertySet,
        ruleNamePropertySet
    };
    private int flatChildCount = 0;
    
    public SonarLintAnalyserIssueSeverityRuleKeyNode(DefaultClientIssue issue, String ruleName) {
        super(new SonarLintAnalyserIssueChildren());
        children = (SonarLintAnalyserIssueChildren)getChildren();
        this.ruleKey = issue.getRuleKey();
        this.ruleName = ruleName;
        this.type = issue.getType();
        ruleNamePropertySet.setName("ruleName");
        ruleNamePropertySet.setDisplayName("Rule name");
        ruleNamePropertySet.setShortDescription("Rule name");
        ruleNamePropertySet.put(new SonarLintAnalyserIssueNode.RuleNameProperty(ruleName));
        typePropertySet.setName("type");
        typePropertySet.setDisplayName("Type");
        typePropertySet.setShortDescription("Type");
        typePropertySet.put(new SonarLintAnalyserIssueNode.TypeProperty(issue));
        severityPropertySet.setName("severity");
        severityPropertySet.setDisplayName("Severity");
        severityPropertySet.setShortDescription("Severity");
        severityPropertySet.put(new SeverityProperty(issue.getSeverity()));
        updateDisplayName();
        setIconBaseWithExtension(
            SonarLintUtils.getRuleTypePathIconInClasspath(type, false)
        );
    }

    private void updateDisplayName() {
        setDisplayName(ruleKey + " : " + ruleName + " (" + flatChildCount + ")");
    }

    public void addIssue(DefaultClientIssue issue, String ruleName) {
        children.addIssue(issue, ruleName);
        flatChildCount++;
        updateDisplayName();
    }

    @Override
    public PropertySet[] getPropertySets() {
        return propertySets;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
}
