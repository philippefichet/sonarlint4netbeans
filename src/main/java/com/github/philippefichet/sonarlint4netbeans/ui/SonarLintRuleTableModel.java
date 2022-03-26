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
package com.github.philippefichet.sonarlint4netbeans.ui;

import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.SonarLintUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.Project;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;


/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintRuleTableModel extends DefaultTableModel {

    public static final int ENABLE_COLUMN_INDEX = 0;
    public static final int SETTINGS_COLUMN_INDEX = 1;
    public static final int KEY_COLUMN_INDEX = 2;
    public static final int SEVERITY_COLUMN_INDEX = 3;
    
    public SonarLintRuleTableModel() {
        init();
    }

    private void init() {
        addColumn("Enable");
        addColumn("Settings");
        addColumn("Key");
        addColumn("Severity");
        addColumn("Details");
    }

    public void setRules(SonarLintEngine engine, String languagekey, String ruleFilter, Project project) {
        while(getRowCount() > 0) {
            removeRow(0);
        }
        Collection<StandaloneRuleDetails> allRuleDetails = engine.getAllRuleDetails();
        allRuleDetails.stream()
        .filter(
            SonarLintUtils.FilterBy.languageKey(languagekey)
            .and(SonarLintUtils.FilterBy.keyAndName(ruleFilter))
        )
        .sorted((r1, r2) -> 
            r1.getKey().compareTo(r2.getKey())
        ).map(ruleDetail -> new Object[] {
            !engine.isExcluded(ruleDetail, project),
            hasParams(ruleDetail),
            ruleDetail.getKey(),
            ruleDetail.getSeverity(),
            ruleDetail.getName()}
        ).collect(Collectors.toList()).forEach(this::addRow);
    }

    private boolean hasParams(StandaloneRuleDetails ruleDetail) {
        return !ruleDetail.paramDetails().isEmpty();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == ENABLE_COLUMN_INDEX;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == ENABLE_COLUMN_INDEX || columnIndex == SETTINGS_COLUMN_INDEX) {
            return Boolean.class;
        }
        return String.class;
    }
    
    public Object getRuleKeyValueAt(int row) {
        return getValueAt(row, SonarLintRuleTableModel.KEY_COLUMN_INDEX);
    }
}
