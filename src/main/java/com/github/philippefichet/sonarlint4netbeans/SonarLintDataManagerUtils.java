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

import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Common behavior related of all implementation of SonarLintDataManager
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintDataManagerUtils {

    private SonarLintDataManagerUtils() {
    }

    public static Project getProjectForAnalyse(SonarLintDataManager dataManager, FileObject fileObject)
    {
        Project projectForAnalyse = dataManager.getProject(fileObject).orElse(SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        if (dataManager.getPreferencesScope(projectForAnalyse) == SonarLintProjectPreferenceScope.GLOBAL) {
            return SonarLintEngine.GLOBAL_SETTINGS_PROJECT;
        }
        return projectForAnalyse;
    }

    public static Project getProjectForAnalyse(SonarLintDataManager dataManager, Project project)
    {
        if (dataManager.getPreferencesScope(project) == SonarLintProjectPreferenceScope.GLOBAL) {
            return SonarLintEngine.GLOBAL_SETTINGS_PROJECT;
        }
        return project;
    }
}
