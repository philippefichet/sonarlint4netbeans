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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.sonarsource.sonarlint.core.analyzer.issue.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyserIssueChildren extends Children.Keys<Issue> {

    private final java.util.Map<Issue, SonarLintAnalyserIssueNode> nodeInstancies = new HashMap<>();
    private final SonarLintAnalyserIssueComparator issueComparator = new SonarLintAnalyserIssueComparator();

    public SonarLintAnalyserIssueChildren() {
        
    }

    public void addIssue(DefaultClientIssue issue) {
        nodeInstancies.put(issue, new SonarLintAnalyserIssueNode(issue));
        List<Issue> keySet = new ArrayList<>(nodeInstancies.keySet());
        Collections.sort(keySet, issueComparator);
        setKeys(nodeInstancies.keySet());
    }

    @Override
    protected Node[] createNodes(Issue issue) {
        return new Node[] {
            nodeInstancies.get(issue)
        };
    }
}
