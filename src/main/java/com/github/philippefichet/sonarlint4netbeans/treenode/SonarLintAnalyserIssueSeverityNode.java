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

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.sonarsource.sonarlint.core.analyzer.issue.DefaultClientIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueSeverityNode extends AbstractNode {

    private final String severity;
    private final SonarLintAnalyserIssueSeverityRuleKeyChildren children;
    private int flatChildCount = 0;
    private final Sheet.Set severityPropertySet = new Sheet.Set();
    private final PropertySet[] propertySets = new PropertySet[] {
        severityPropertySet,
    };

    public SonarLintAnalyserIssueSeverityNode(String severity) {
        super(new SonarLintAnalyserIssueSeverityRuleKeyChildren());
        this.severity = severity.toLowerCase();
        children = (SonarLintAnalyserIssueSeverityRuleKeyChildren)getChildren();
        severityPropertySet.setName("severity");
        severityPropertySet.setDisplayName("Severity");
        severityPropertySet.setShortDescription("Severity");
        severityPropertySet.put(new SeverityProperty(severity));
        setIconBaseWithExtension("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-" + this.severity + ".png");
        updateDisplayName();
    }

    private void updateDisplayName() {
        if (flatChildCount > 1) {
            setDisplayName(this.severity + " (" + flatChildCount + " issues)");
        } else {
            setDisplayName(this.severity + " (" + flatChildCount + " issue)");
        }
    }

    public void addIssue(DefaultClientIssue issue) {
        children.addIssue(issue);
        flatChildCount++;
        updateDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
    
    @Override
    public PropertySet[] getPropertySets() {
        return propertySets;
    }
}
