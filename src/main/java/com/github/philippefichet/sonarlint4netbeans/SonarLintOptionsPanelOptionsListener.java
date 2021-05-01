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

import org.sonarsource.sonarlint.core.client.api.common.Version;

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
     * Called when tes rules configuration is changed
     * @param applyDifferentRulesOnTestFiles 
     */
    public void testRulesOptionsChanged(Boolean applyDifferentRulesOnTestFiles);
}
