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

import org.sonarsource.sonarlint.core.commons.Version;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public interface SonarLintOptionsPanelOptionsListener {
    /**
     * Called when nodejs configuration is changed
     * @param nodeJSPath path if nodeJSPath exist and version is known, null otherwise
     * @param nodeJSVersion version if known, null otherwise
     */
    public void nodeJSOptionsChanged(String nodeJSPath, Version nodeJSVersion);

    /**
     * Called when test rules configuration is changed
     * @param applyDifferentRulesOnTestFiles new value of test rules configuration
     */
    public void testRulesOptionsChanged(Boolean applyDifferentRulesOnTestFiles);
}
