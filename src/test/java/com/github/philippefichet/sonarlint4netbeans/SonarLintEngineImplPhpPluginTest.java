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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
@Tag("php")
public class SonarLintEngineImplPhpPluginTest {

    public static Arguments[] parametersForAnalyze() throws IOException
    {
        return new Arguments[] {
            Arguments.of(
                SonarLintEngineTestConfiguration.builder()
                .description("sonarlin-example.php with rule php:S101 but without php:S1105 to check php plugin")
                .includeRules("php:S101")
                .excludeRules("php:S1105")
                .addClientInputFile(new File("./src/test/resources/sonarlin-example.php"))
                .build(),
                Arrays.asList(
                    new DefaultIssueTestImpl.Builder()
                    .severity("MINOR")
                    .type("CODE_SMELL")
                    .ruleKey("php:S101")
                    .ruleName("Class names should comply with a naming convention")
                    .startLine(2)
                    .startLineOffset(10)
                    .endLine(2)
                    .endLineOffset(18)
                    .build()
                )
            ),
        };
    }

    @ParameterizedTest(name = "[{index}}] analyze({0})")
    @MethodSource("parametersForAnalyze")
    public void analyze(SonarLintEngineTestConfiguration testConfiguration, List<Issue> expectedIssue) throws MalformedURLException, BackingStoreException, IOException
    {
        SonarLintEngineImplTestUtils.analyzeTesting(testConfiguration, expectedIssue);
    }
}
