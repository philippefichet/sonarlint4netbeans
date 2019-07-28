/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        Path path = toFile.toPath();
        List<ClientInputFile> files = new ArrayList<>();
        files.add(new FSClientInputFile(
            contentToAnalyze == null ? new String(Files.readAllBytes(path)) : contentToAnalyze,
            path.toAbsolutePath(),
            path.toFile().getName(),
            SonarLintUtils.isTest(fileObject),
            FileEncodingQuery.getEncoding(fileObject))
        );

        AnalysisResults analyze = sonarLintEngine.analyze(
            new StandaloneAnalysisConfiguration(
                new File(sonarLintHome).toPath(),
                new File(sonarLintHome + File.separator + "work").toPath(),
                files,
                Collections.emptyMap(),
                excludedRules,
                includedRules
            ),
            issues::add,
            null,
            null
        );
        return issues;
    }

    /**
     * Check if file is in test directory from project
     *
     * @param fileObject
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
                    LOG.log(Level.FINE, "{} is test", fileObject.getName());
                    return true;
                }
            }
        }
        LOG.log(Level.FINE, "{} is not test", fileObject.getName());
        return false;
    }
}
