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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;

/**
 *
 * @author FICHET Philippe
 */
public final class SonarLintUtils {

    private static final Logger LOG = Logger.getLogger(SonarLintUtils.class.getCanonicalName());

    private SonarLintUtils() {
    }

    public static class FilterBy {

        private FilterBy() {
        }

       /**
         * Create a predicat to filter rule detail by language key
         * @param languageKey language key to filter
         * @return Predicat to filter rule detail by language key
         */
        public static Predicate<RuleDetails> languageKey(String languageKey) {
            return ruleDetail -> ruleDetail.getLanguageKey().equals(languageKey);
        }

        /**
         * Create a predicat to filter rule detail by key or name
         * @param keyOrName key or name to filter
         * @return Predicat to filter rule detail by key or name
         */
        public static Predicate<RuleDetails> keyAndName(String keyOrName) {
            String ruleFilterLowerCase = keyOrName.toLowerCase();
            return ruleDetail -> keyOrName.isEmpty()
                || ruleDetail.getKey().toLowerCase().contains(ruleFilterLowerCase)
                || ruleDetail.getName().toLowerCase().contains(ruleFilterLowerCase);
        }
    }

    public static Optional<ImageIcon> toImageIcon(String severity)
    {
        URL resource = SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-" + severity.toLowerCase() + ".png");
        if (resource == null) {
            return Optional.empty();
        }
        return Optional.of(new ImageIcon(resource, severity));
    }

    public static String toURL(RuleDetails ruleDetails)
    {
        String[] keySplit = ruleDetails.getKey().split(":");
        return "https://rules.sonarsource.com/" + keySplit[0] + "/RSPEC-" + keySplit[1].substring(1);
    }

    public static List<Issue> analyze(FileObject fileObject, String contentToAnalyze) throws IOException {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return Collections.emptyList();
        }

        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        Collection<RuleDetails> allRuleDetails = sonarLintEngine.getAllRuleDetails();
        List<RuleKey> excludedRules = new ArrayList<>();
        List<RuleKey> includedRules = new ArrayList<>();
        for (RuleDetails allRuleDetail : allRuleDetails) {
            RuleKey ruleKey = RuleKey.parse(allRuleDetail.getKey());
            if (sonarLintEngine.isExcluded(allRuleDetail)) {
                excludedRules.add(ruleKey);
            } else {
                includedRules.add(ruleKey);
            }
        }

        File toFile = FileUtil.toFile(fileObject);
        if (toFile == null) {
            return Collections.emptyList();
        }
        Path path = toFile.toPath();
        List<ClientInputFile> files = new ArrayList<>();
        files.add(new FSClientInputFile(
            contentToAnalyze == null ? new String(Files.readAllBytes(path)) : contentToAnalyze,
            path.toAbsolutePath(),
            path.toFile().getName(),
            SonarLintUtils.isTest(fileObject),
            FileEncodingQuery.getEncoding(fileObject))
        );

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
        return issues;
    }

    /**
     * Check if file is in test directory from project
     *
     * @param fileObject file object to check if is test
     * @return true if file is in test directory from project
     */
    public static boolean isTest(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project != null) {
            File projectFile = FileUtil.toFile(project.getProjectDirectory());
            File file = FileUtil.toFile(fileObject);
            if (file.getAbsolutePath().startsWith(projectFile.getAbsolutePath())) {
                String relativeProjectPath = file.getAbsolutePath().replace(projectFile.getAbsolutePath(), "");
                if (relativeProjectPath.contains(File.separator + "test" + File.separator)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrive stylesheet for HTML rule detail description
     * @param sonarLintOptions
     * @return Stylesheet for HTML rule detail description
     */
    public static String toRuleDetailsStyleSheet(SonarLintOptions sonarLintOptions)
    {
        try {
            return "<style>\n" + sonarLintOptions.getSonarLintDetailsStyle().asText() + "\n</style>";
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

    /**
     * Retrieve HTML detail description of rule
     * @param ruleDetails Detail of rule
     * @return HTML detail description of rule
     */
    public static String toHtmlDescription(RuleDetails ruleDetails)
    {
        return "<div id=\"" + ruleDetails.getKey().replaceAll(":", "-") + "\">"
            + "<h1><a href=\"" + SonarLintUtils.toURL(ruleDetails) + "\">" + ruleDetails.getName() + "</a></h1>"
            + ruleDetails.getHtmlDescription()
            + "</div>";
    }
}
