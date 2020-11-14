/*
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

import java.io.File;
import java.net.URI;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.sonarsource.sonarlint.core.analyzer.issue.DefaultClientIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyzerOpenIssueInFileAction extends NodeAction {
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            final SonarLintAnalyserIssueNode issueNode = n.getLookup().lookup(
                    SonarLintAnalyserIssueNode.class);
            if (issueNode != null) {
                DefaultClientIssue issue = issueNode.getIssue();
                URI uri = issue.getInputFile().uri();
                FileObject toFileObject = FileUtil.toFileObject(new File(uri));
                if (toFileObject != null) {
                    try {
                        DataObject find = DataObject.find(toFileObject);
                        if (find != null) {
                            Integer startLine = issue.getStartLine();
                            Integer startLineOffset = issue.getStartLineOffset();
                            NbDocument.openDocument(
                                find,
                                startLine != null ? startLine - 1 : 0, 
                                startLineOffset != null ? startLineOffset : 0,
                                Line.ShowOpenType.OPEN,
                                Line.ShowVisibilityType.FOCUS
                            );
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length > 0;
    }

    @Override
    public String getName() {
        return "Go to issue";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
