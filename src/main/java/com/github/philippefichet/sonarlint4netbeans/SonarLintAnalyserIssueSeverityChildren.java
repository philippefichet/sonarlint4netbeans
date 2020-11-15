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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.sonarsource.sonarlint.core.analyzer.issue.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueSeverityChildren extends Children.Keys<String> {

    private static final String SEVERITY_BLOCKER = "BLOCKER";
    private static final String SEVERITY_CRITICAL = "CRITICAL";
    private static final String SEVERITY_INFO = "INFO";
    private static final String SEVERITY_MAJOR = "MAJOR";
    private static final String SEVERITY_MINOR = "MINOR";
    private final java.util.Map<String, SonarLintAnalyserIssueSeverityNode> nodeInstancies = new HashMap<>();
    private int issueCount = 0;

    public void addIssue(Issue issue) {
        if (issue instanceof DefaultClientIssue) {
            issueCount++;
            nodeInstancies.computeIfAbsent(issue.getSeverity(), SonarLintAnalyserIssueSeverityNode::new)
                .addIssue((DefaultClientIssue)issue);
            setKeys(orderKeysBySeverity(nodeInstancies.keySet()));
        }
    }
    
    private static List<String> orderKeysBySeverity(Set<String> keySet)
    {
        List<String> copyKeySet = new ArrayList<>(keySet);
        List<String> keys = new ArrayList<>(keySet.size());
        if (copyKeySet.contains(SEVERITY_BLOCKER)) {
            keys.add(SEVERITY_BLOCKER);
            copyKeySet.remove(SEVERITY_BLOCKER);
        }
        if (copyKeySet.contains(SEVERITY_CRITICAL)) {
            keys.add(SEVERITY_CRITICAL);
            copyKeySet.remove(SEVERITY_CRITICAL);
        }
        if (copyKeySet.contains(SEVERITY_MAJOR)) {
            keys.add(SEVERITY_MAJOR);
            copyKeySet.remove(SEVERITY_MAJOR);
        }
        if (copyKeySet.contains(SEVERITY_INFO)) {
            keys.add(SEVERITY_INFO);
            copyKeySet.remove(SEVERITY_INFO);
        }
        if (copyKeySet.contains(SEVERITY_MINOR)) {
            keys.add(SEVERITY_MINOR);
            copyKeySet.remove(SEVERITY_MINOR);
        }
        keys.addAll(copyKeySet);
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
