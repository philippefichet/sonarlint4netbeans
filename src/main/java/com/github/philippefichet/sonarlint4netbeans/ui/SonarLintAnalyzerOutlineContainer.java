/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
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
package com.github.philippefichet.sonarlint4netbeans.ui;

import com.github.philippefichet.sonarlint4netbeans.treenode.SonarLintAnalyzerRootNode;
import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyzerOutlineContainer extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
    private final Lookup lookup;
    private final ExplorerManager manager;
    private final SonarLintAnalyzerRootNode rootNode = new SonarLintAnalyzerRootNode();
    private static final String COLUMN_ID_LOCATION = "Location";
    private static final String COLUMN_ID_TYPE = "Type";
    private static final String COLUMN_ID_SEVERITY = "Severity";
    private static final String COLUMN_ID_RULE_NAME = "Rule name";

    public SonarLintAnalyzerOutlineContainer() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        this.manager = new ExplorerManager();
        ActionMap map = this.getActionMap ();
        // following line tells the top component which lookup should be associated with it
        lookup = ExplorerUtils.createLookup(manager, map);
        // https://bits.netbeans.org/12.0/javadoc/org-netbeans-spi-viewmodel/org/netbeans/spi/viewmodel/package-summary.html
        NodeTableModel nodeTableModel = new NodeTableModel();
        nodeTableModel.setNodes(new Node[] {
            rootNode
        });
        OutlineView outlineView = new OutlineView();
        outlineView.addPropertyColumn("location", COLUMN_ID_LOCATION);
        outlineView.addPropertyColumn("type", COLUMN_ID_TYPE);
        outlineView.addPropertyColumn("severity", COLUMN_ID_SEVERITY);
        outlineView.addPropertyColumn("ruleName", COLUMN_ID_RULE_NAME);
        ETableColumnModel columnModel  = ((ETableColumnModel)outlineView.getOutline().getColumnModel());
        columnModel.setColumnHidden(
            columnModel.getColumn(columnModel.getColumnIndex(COLUMN_ID_LOCATION)),
            true
        );
        columnModel.setColumnHidden(
            columnModel.getColumn(columnModel.getColumnIndex(COLUMN_ID_TYPE)),
            true
        );
        columnModel.setColumnHidden(
            columnModel.getColumn(columnModel.getColumnIndex(COLUMN_ID_SEVERITY)),
            true
        );
        columnModel.setColumnHidden(
            columnModel.getColumn(columnModel.getColumnIndex(COLUMN_ID_RULE_NAME)),
            true
        );
        this.add(outlineView, BorderLayout.CENTER);
        getExplorerManager().setRootContext(rootNode);
        outlineView.expandNode(rootNode);
    }

    public void starting() {
        rootNode.starting();
    }

    public void ending() {
        rootNode.ending();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public void handle(Issue issue, String ruleName) {
        rootNode.handle(issue, ruleName);
    }

    // It is good idea to switch all listeners on and off when the
    // component is shown or hidden. In the case of TopComponent use:
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
}
