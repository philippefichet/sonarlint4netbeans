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
package com.github.philippefichet.sonarlint4netbeans.ui.listener;

import org.sonarsource.sonarlint.core.commons.RuleKey;

/**
 * Interface to notify when a rule is enable or disable in SonarLintRuleListPanel
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@FunctionalInterface
public interface SonarLintRuleListPanelListener {
    /**
     * Called when a rule is enabled or disabled
     * @param ruleKey rule key
     * @param enabled true for enable rule, false otherwise
     */
    public void ruleChanged(RuleKey ruleKey, boolean enabled);
}
