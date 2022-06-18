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
import java.util.List;
import javax.swing.Action;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.openide.nodes.AbstractNode;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyzerRootNode extends AbstractNode {

    private final List<TreeModelListener> listeners = new ArrayList<>();
    private final SonarLintAnalyserIssueSeverityChildren children;
    private int flatChildCount = 0;

    public SonarLintAnalyzerRootNode() {
        super(new SonarLintAnalyserIssueSeverityChildren());
        children = (SonarLintAnalyserIssueSeverityChildren)getChildren();
        setDisplayName("Analyze waiting start");
        setIconBaseWithExtension("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint.png");
    }

    public void starting() {
        updateStartingTitle();
    }

    public void ending() {
        int count = children.getIssuesCount();
        if (count > 1) {
            setDisplayName("Analyze done, " + count + " issues found");
        } else {
            setDisplayName("Analyze done, " + count + " issue found");
        }
    }
    
    private void updateStartingTitle() {
        if (flatChildCount > 1) {
            setDisplayName("Analyze running, " + flatChildCount + " issues found");
        } else {
            setDisplayName("Analyze running, " + flatChildCount + " issue found");
        }
    }

    public void handle(Issue issue, String ruleName) {
        if (issue instanceof DefaultClientIssue) {
            flatChildCount++;
            children.addIssue(issue, ruleName);
            updateStartingTitle();
            for (TreeModelListener listener : listeners) {
                listener.treeNodesInserted(new TreeModelEvent(this, (TreePath)null));
            }
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
}