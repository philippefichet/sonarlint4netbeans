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

import com.github.philippefichet.sonarlint4netbeans.ui.SonarLintAnalyzerActionTopComponent;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ActionID(id = "com.github.philippefichet.sonarlint4netbeans.SonarLintAnalyzeAction", category = "Project")
@ActionRegistration(
    displayName = "Analyze with SonarLint",
    iconBase = "com/github/philippefichet/sonarlint4netbeans/resources/sonarlint.png",
    lazy = false
)
@ActionReferences({
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1310),
    @ActionReference(path = "Projects/org-netbeans-modules-gradle/Actions", position = 1310),
    @ActionReference(path = "Projects/package/Actions", position = 1310),
    @ActionReference(path = "UI/ToolActions/Files", position = 400),
})
public class SonarLintAnalyzerAction extends NodeAction {
    @Override
    protected void performAction(Node[] activatedNodes) {
        // PENDING
        SwingUtilities.invokeLater(() -> {
            TopComponent topComponent = WindowManager.getDefault().findTopComponent("SonarLintAnalyzerActionTopComponent");
            if (topComponent instanceof SonarLintAnalyzerActionTopComponent) {
                SonarLintAnalyzerActionTopComponent sonarRuleDetailsTopComponent = (SonarLintAnalyzerActionTopComponent)topComponent;
                sonarRuleDetailsTopComponent.open();
                sonarRuleDetailsTopComponent.requestActive();
                sonarRuleDetailsTopComponent.requestFocusInWindow();
                sonarRuleDetailsTopComponent.addDeepSonarLintAnalyze(activatedNodes);
            }
        });
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public String getName() {
        return "Analyze with SonarLint";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
