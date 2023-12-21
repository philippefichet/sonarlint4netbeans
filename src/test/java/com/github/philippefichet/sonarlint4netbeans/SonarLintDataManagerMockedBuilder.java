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
package com.github.philippefichet.sonarlint4netbeans;

import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintDataManagerMockedBuilder {
    private final SonarLintDataManager sonarLintDataManagerMocked = Mockito.mock(SonarLintDataManager.class);

    public SonarLintDataManagerMockedBuilder() {
        Mockito.when(sonarLintDataManagerMocked.getEncoding(ArgumentMatchers.any()))
            .thenReturn(Optional.of(StandardCharsets.UTF_8));
        Mockito.when(sonarLintDataManagerMocked.getGlobalSettingsPreferences())
            .thenReturn(new SonarLintPreferencesForTesting());
        Mockito.when(sonarLintDataManagerMocked.isTest(ArgumentMatchers.any()))
            .thenReturn(Boolean.FALSE);
        Mockito.when(sonarLintDataManagerMocked.getInstalledFile(ArgumentMatchers.any()))
            .thenAnswer(
                (InvocationOnMock iom) ->
                new File("." + File.separator + "src" + File.separator + "main" + File.separator + (String)iom.getArgument(0))
            );
    }

    public SonarLintDataManagerMockedBuilder createPreferences(Project project)
    {
        Mockito.when(sonarLintDataManagerMocked.getPreferences(project))
            .thenReturn(new SonarLintPreferencesForTesting());
        Mockito.when(sonarLintDataManagerMocked.getRemoteConfigurationPreferences(project))
            .thenReturn(new SonarLintPreferencesForTesting());
        return this;
    }

    public SonarLintDataManagerMockedBuilder createPreferences(Project project, SonarLintProjectPreferenceScope scope)
    {
        createPreferences(project);
        preferencesScope(project, scope);
        return this;
    }

    public SonarLintDataManagerMockedBuilder addFileToProject(Project project, File file)
    {
        Mockito.when(sonarLintDataManagerMocked.getProject(file))
            .thenReturn(Optional.of(project));
        Mockito.when(sonarLintDataManagerMocked.getProject(FileUtil.toFileObject(file)))
            .thenReturn(Optional.of(project));
        return this;
    }

    public SonarLintDataManagerMockedBuilder preferencesScope(Project project, SonarLintProjectPreferenceScope scope)
    {
        Mockito.when(sonarLintDataManagerMocked.getPreferencesScope(project))
            .thenReturn(scope);
        return this;
    }

    public SonarLintDataManager build()
    {
        return sonarLintDataManagerMocked;
    }
}
