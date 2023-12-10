/*
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
package com.github.philippefichet.sonarlint4netbeans.remote.project;

import com.github.philippefichet.sonarlint4netbeans.remote.SonarLintRemoteEngine;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteProjectConfiguration;
import com.github.philippefichet.sonarlint4netbeans.remote.synchronization.TaskWrapper;
import java.util.Optional;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@ProjectServiceProvider(
    service=ProjectOpenedHook.class,
    projectType = {
        "org-netbeans-modules-ant-freeform",
        "org-netbeans-modules-apisupport-project",
        "org-netbeans-modules-j2ee-clientproject",
        "org-netbeans-modules-j2ee-earproject",
        "org-netbeans-modules-j2ee-ejbjarproject",
        "org-netbeans-modules-java-j2seproject",
        "org-netbeans-modules-java-j2semodule",
        "org-netbeans-modules-maven",
        "org-netbeans-modules-gradle",
        "org-netbeans-modules-cnd-makeproject",
        "org-netbeans-modules-cpplite-project-CPPLiteProject",
        "org-netbeans-modules-php-project",
        "org-netbeans-modules-web-project",
        "org-netbeans-modules-web-clientproject",
    }
)
public class SonarLintSyncTaskProjectOpenedHook extends ProjectOpenedHook {
    private Project project;

    public SonarLintSyncTaskProjectOpenedHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        SonarLintRemoteProjectConfiguration projectConfiguration = SonarLintRemoteProjectConfiguration.fromProject(project);
        if (projectConfiguration.getConnectionId() != null
            && projectConfiguration.getOrganization() != null
            && projectConfiguration.getProjectKey() != null) {

            SonarLintRemoteEngine sonarLintRemoteEngine = Lookup.getDefault().lookup(SonarLintRemoteEngine.class);
            sonarLintRemoteEngine.launchResyncTask(projectConfiguration);
        }
    }

    @Override
    protected void projectClosed() {
        SonarLintRemoteProjectConfiguration projectConfiguration = SonarLintRemoteProjectConfiguration.fromProject(project);
        SonarLintRemoteEngine sonarLintRemoteEngine = Lookup.getDefault().lookup(SonarLintRemoteEngine.class);
        Optional<TaskWrapper> launchedResyncTask = sonarLintRemoteEngine.getLaunchedResyncTask(projectConfiguration);
        sonarLintRemoteEngine.launchResyncTask(projectConfiguration);
        if (launchedResyncTask.isPresent() && launchedResyncTask.get().getTask().isFinished() == false) {
            launchedResyncTask.get().getSonarLintRemoteSynchronizationTask().cancel();
        }
    }
}
