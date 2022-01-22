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
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectManagerImplementation;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintEngineImplTest {

    private static final Project mockedProjectWithProjectScope = Mockito.mock(Project.class);
    private static final Project mockedProjectWithGlobalScope = Mockito.mock(Project.class);
    
    @RegisterExtension
    private final SonarLintLookupMockedExtension lookupExtension = SonarLintLookupMockedExtension.builder()
        .logCall()
        .mockLookupMethodWith(ProjectManagerImplementation.class, Mockito.mock(ProjectManagerImplementation.class))
        .mockLookupMethodWith(SonarLintOptions.class, Mockito.mock(SonarLintOptions.class))
        .mockLookupMethodWith(
            SonarLintDataManager.class,
            new SonarLintDataManagerMockedBuilder()
                .createPreferences(mockedProjectWithGlobalScope)
                .preferencesScope(mockedProjectWithGlobalScope, SonarLintProjectPreferenceScope.GLOBAL)
                .createPreferences(mockedProjectWithProjectScope)
                .preferencesScope(mockedProjectWithProjectScope, SonarLintProjectPreferenceScope.PROJECT)
                .build()
        ).build();
    
    @Test
    public void getRuleParameter() throws MalformedURLException, BackingStoreException
    {
        String ruleKey = "java:S107";
        String parameterName = "max";
        String parameterValueOnProjectScope = "5";
        String parameterValueOnGlobalScope = "4";
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.waitingInitialization();
        sonarLintEngine.getPreferences(SonarLintEngine.GLOBAL_SETTINGS_PROJECT).removeNode();
        sonarLintEngine.setRuleParameter(ruleKey, parameterName, parameterValueOnProjectScope, mockedProjectWithProjectScope);
        sonarLintEngine.setRuleParameter(ruleKey, parameterName, parameterValueOnGlobalScope, mockedProjectWithGlobalScope);
        Optional<String> ruleParameterOnProjectScope = sonarLintEngine.getRuleParameter(ruleKey, parameterName, mockedProjectWithProjectScope);
        Optional<String> ruleParameterOnGlobalScope = sonarLintEngine.getRuleParameter(ruleKey, parameterName, mockedProjectWithGlobalScope);
        Optional<String> ruleParameterOnGlobalSettings = sonarLintEngine.getRuleParameter(ruleKey, parameterName, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Assertions.assertThat(ruleParameterOnProjectScope)
            .describedAs("On project with project scope")
            .isPresent()
            .get()
            .isEqualTo(parameterValueOnProjectScope);
        Assertions.assertThat(ruleParameterOnGlobalScope)
            .describedAs("On project with global scope")
            .isPresent()
            .get().isEqualTo(parameterValueOnGlobalScope);
        Assertions.assertThat(ruleParameterOnGlobalSettings)
            .describedAs("On global settings")
            .isNotPresent();
    }
}
