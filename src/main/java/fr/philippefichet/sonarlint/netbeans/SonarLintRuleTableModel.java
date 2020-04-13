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
package fr.philippefichet.sonarlint.netbeans;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;


/**
 *
 * @author FICHET Philippe <philippe.fichet@laposte.net>
 */
public class SonarLintRuleTableModel extends DefaultTableModel {

    public SonarLintRuleTableModel() {
        init();
    }
    
    private void init() {
        addColumn("");
        addColumn("Language");
        addColumn("Severity");
        addColumn("Key");
        addColumn("Details");
    }

    public void setRules(SonarLintEngine engine, String languagekey, String ruleFilter) {
        while(getRowCount() > 0) {
            removeRow(0);
        }
        Collection<RuleDetails> allRuleDetails = engine.getAllRuleDetails();
        allRuleDetails.stream()
        .filter(
            SonarLintUtils.FilterBy.languageKey(languagekey)
            .and(SonarLintUtils.FilterBy.keyAndName(ruleFilter))
        )
        .sorted((r1, r2) -> 
            r1.getKey().compareTo(r2.getKey())
        ).map(ruleDetail -> new Object[] {
            !engine.isExcluded(ruleDetail),
            ruleDetail.getLanguageKey(),
            ruleDetail.getSeverity(),
            ruleDetail.getKey(),
            ruleDetail.getName()}
        ).collect(Collectors.toList()).forEach(this::addRow);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return column < 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }
}
