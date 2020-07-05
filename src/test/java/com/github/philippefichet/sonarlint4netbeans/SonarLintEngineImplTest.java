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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.RuleParam;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRuleParam;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintEngineImplTest {

    @Test
    public void ruleParameterNotChanged() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.getPreferences().removeNode();
        File sonarlintFileDemo = new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile();
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        String rileKeyString = "java:S115";
        Optional<RuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(rileKeyString);
        RuleKey ruleKey = RuleKey.parse(rileKeyString);
        List<RuleKey> excludedRules = new ArrayList<>();
        List<RuleKey> includedRules = new ArrayList<>();
        includedRules.add(ruleKey);
        excludedRules.add(RuleKey.parse("java:S1220"));
        excludedRules.add(RuleKey.parse("java:S1118"));
        
        Path sonarlintFileDemoPath = sonarlintFileDemo.toPath();
        List<ClientInputFile> files = new ArrayList<>();
        files.add(new FSClientInputFile(
            new String(Files.readAllBytes(sonarlintFileDemoPath)),
            sonarlintFileDemoPath.toAbsolutePath(),
            sonarlintFileDemo.getName(),
            false,
            StandardCharsets.UTF_8
        )
        );
        
        StandaloneRule rule = (StandaloneRule)ruleDetails.get();
        for (RuleParam param : rule.params()) {
            System.out.println("ruleDetails = " + param.key() + " / " + param.description());
            System.out.println("ruleDetails = " + param.key() + " / " + ((StandaloneRuleParam)param).defaultValue());
        }

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(files)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .build();

        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            issues::add,
            null,
            null
        );

        System.out.println("analyze = " + analyze);
        System.out.println("issues = " + issues);
        Assertions.assertFalse(issues.isEmpty());
    }

    @Test
    public void ruleParameterChanged() throws MalformedURLException, IOException, BackingStoreException {
        SonarLintEngineImpl sonarLintEngine = new SonarLintEngineImpl();
        sonarLintEngine.getPreferences().removeNode();
        File sonarlintFileDemo = new File("./src/test/resources/SonarLintFileDemo.java").getAbsoluteFile();
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        String rileKeyString = "java:S115";
        Optional<RuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(rileKeyString);
        RuleKey ruleKey = RuleKey.parse(rileKeyString);
        List<RuleKey> excludedRules = new ArrayList<>();
        List<RuleKey> includedRules = new ArrayList<>();
        includedRules.add(ruleKey);
        excludedRules.add(RuleKey.parse("java:S1220"));
        excludedRules.add(RuleKey.parse("java:S1118"));
        
        Path sonarlintFileDemoPath = sonarlintFileDemo.toPath();
        List<ClientInputFile> files = new ArrayList<>();
        files.add(new FSClientInputFile(
            new String(Files.readAllBytes(sonarlintFileDemoPath)),
            sonarlintFileDemoPath.toAbsolutePath(),
            sonarlintFileDemo.getName(),
            false,
            StandardCharsets.UTF_8
        )
        );
        
        StandaloneRule rule = (StandaloneRule)ruleDetails.get();
        for (RuleParam param : rule.params()) {
            System.out.println("ruleDetails = " + param.key() + " / " + param.description());
            System.out.println("ruleDetails = " + param.key() + " / " + ((StandaloneRuleParam)param).defaultValue());
        }

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(files)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .build();

        sonarLintEngine.setRuleParameter("java:S115", "format", "^.+$");
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            issues::add,
            null,
            null
        );

        System.out.println("analyze = " + analyze);
        System.out.println("issues = " + issues);
        Assertions.assertTrue(issues.isEmpty());
    }
}
