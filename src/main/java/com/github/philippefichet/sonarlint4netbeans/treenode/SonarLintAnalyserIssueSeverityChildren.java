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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueSeverityChildren extends Children.Keys<String> {

    private final java.util.Map<String, SonarLintAnalyserIssueSeverityNode> nodeInstancies = new HashMap<>();
    private int issueCount = 0;

    public void addIssue(Issue issue, String ruleName) {
        if (issue instanceof DefaultClientIssue) {
            issueCount++;
            nodeInstancies.computeIfAbsent(issue.getSeverity().name(), SonarLintAnalyserIssueSeverityNode::new)
                .addIssue((DefaultClientIssue)issue, ruleName);
            setKeys(orderKeysBySeverity(nodeInstancies.keySet()));
        }
    }
    
    private static List<String> orderKeysBySeverity(Set<String> keySet)
    {
        List<String> copyKeySet = new ArrayList<>(keySet);
        List<String> keys = new ArrayList<>(keySet.size());
        if (copyKeySet.contains(IssueSeverity.BLOCKER.name())) {
            keys.add(IssueSeverity.BLOCKER.name());
            copyKeySet.remove(IssueSeverity.BLOCKER.name());
        }
        if (copyKeySet.contains(IssueSeverity.CRITICAL.name())) {
            keys.add(IssueSeverity.CRITICAL.name());
            copyKeySet.remove(IssueSeverity.CRITICAL.name());
        }
        if (copyKeySet.contains(IssueSeverity.MAJOR.name())) {
            keys.add(IssueSeverity.MAJOR.name());
            copyKeySet.remove(IssueSeverity.MAJOR.name());
        }
        if (copyKeySet.contains(IssueSeverity.INFO.name())) {
            keys.add(IssueSeverity.INFO.name());
            copyKeySet.remove(IssueSeverity.INFO.name());
        }
        if (copyKeySet.contains(IssueSeverity.MINOR.name())) {
            keys.add(IssueSeverity.MINOR.name());
            copyKeySet.remove(IssueSeverity.MINOR.name());
        }
        for (String issueSeverity : copyKeySet) {
            keys.add(issueSeverity);
        }
        return keys;
    }

    @Override
    protected Node[] createNodes(String key) {
        return new Node[] {
            nodeInstancies.get(key)
        };
    }
    
    public int getIssuesCount()
    {
        return issueCount;
    }
}
