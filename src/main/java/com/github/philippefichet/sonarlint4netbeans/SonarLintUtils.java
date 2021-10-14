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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.apache.commons.text.StringEscapeUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;
import org.sonarsource.sonarlint.core.container.model.DefaultAnalysisResult;

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
            return ruleDetail -> ruleDetail.getLanguage().getLanguageKey().equals(languageKey);
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

        SonarLintOptions sonarlintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        boolean useTestRules = sonarlintOptions == null || sonarlintOptions.applyDifferentRulesOnTestFiles();

        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        Collection<StandaloneRuleDetails> allRuleDetails = sonarLintEngine.getAllRuleDetails();
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
        boolean applyTestRules = useTestRules && SonarLintUtils.isTest(fileObject);
       files.add(new FSClientInputFile(
            contentToAnalyze == null ? new String(Files.readAllBytes(path)) : contentToAnalyze,
            path.toAbsolutePath(),
            path.toFile().getName(),
            applyTestRules,
            FileEncodingQuery.getEncoding(fileObject))
        );

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(files)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .addRuleParameters(sonarLintEngine.getRuleParameters())
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
            + "<h1><a href=\"" + SonarLintUtils.toURL(ruleDetails) + "\">" + StringEscapeUtils.escapeHtml4(ruleDetails.getName()) + "</a></h1>"
            + ruleDetails.getHtmlDescription()
            + "</div>";
    }

    /**
     * 
     * @param files
     * @param listener
     * @param clientInputFileInputStreamEvent
     * @return
     * @throws IOException 
     */
    public static AnalysisResults analyze(
        List<File> files,
        IssueListener listener,
        ClientInputFileListener clientInputFileInputStreamEvent,
        SonarLintAnalyzerCancelableTask sonarLintAnalyzerCancelableTask
    ) throws IOException {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return new DefaultAnalysisResult();
        }

        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        Collection<StandaloneRuleDetails> allRuleDetails = sonarLintEngine.getAllRuleDetails();
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
        
        List<FSClientInputFile> clientInputFiles = new ArrayList<>();
        for (File file : files) {
            // Map file to implementation of ClientInputFile
            Path path = file.toPath();
            try {
                FileObject fileObject = FileUtil.toFileObject(file);
                // ignore null FileObject (e.g. .nbattr) as getEncoding throws IllegalArgumentException
                if (fileObject == null)
                {
                    continue;
                }
                Charset encoding = FileEncodingQuery.getEncoding(fileObject);
                clientInputFiles.add(new FSClientInputFile(
                    new String(Files.readAllBytes(path)),
                    path.toAbsolutePath(),
                    path.toFile().getName(),
                    SonarLintUtils.isTest(fileObject),
                    encoding
                ));
            } catch (IOException ex) {
                LOG.warning("Error during getEncoding from \"" + file.getAbsolutePath() + "\": " + ex.getMessage());
            }
        }

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(clientInputFiles)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .addRuleParameters(sonarLintEngine.getRuleParameters())
            .build();

        // Add listener only after configuration to prevent ClientInputFile.uri() call during configuration phase
        clientInputFiles.forEach(file -> file.addListener(clientInputFileInputStreamEvent));
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            listener,
            null,
            new ProgressMonitor() {
                @Override
                public boolean isCanceled() {
                    return sonarLintAnalyzerCancelableTask != null && sonarLintAnalyzerCancelableTask.isCanceled();
                }
            }
        );
        return analyze;
    }

    
    public static List<File> toFiles(Node[] nodes) {
        List<File> files = new ArrayList<>();
        for (Node node : nodes) {
            DataObject dataObjectOfNode = node.getLookup().lookup(DataObject.class);
            if (dataObjectOfNode != null) {
                File file = FileUtil.toFile(dataObjectOfNode.getPrimaryFile());
                try {
                    Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return dir.endsWith("generated-sources") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                        }
                        
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            files.add(file.toFile());
                            return FileVisitResult.CONTINUE;
                        }
                        
                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return files;
    }
    
    /**
     * Cut start URI too long
     * @param uri
     * @param maximalLength 
     * @return 
     */
    public static final String toTruncateURI(URI uri, int maximalLength)
    {
        if (uri == null) {
            return null;
        }
        String uriPath = uri.toString();
        if (uriPath.length() <= maximalLength) {
            return uriPath;
        }
        StringBuilder sb = new StringBuilder();
        String[] split = uriPath.split("/");
        for (int i = split.length - 1 ; i >= 1 ; i--) {
            sb.insert(0, split[i]);
            if ((sb.length() + 3) > maximalLength) {
                sb.insert(0, ".../");
                break;
            } else {
                sb.insert(0, "/");
            }
        }
        return sb.toString();
    }

    /**
     * Search a rule parameter
     * @param ruleDetail rule to search
     * @param parameterName parameter name to search
     * @return rule parameter if exist
     */
    public static Optional<StandaloneRuleParam> searchRuleParameter(StandaloneRuleDetails ruleDetail, String parameterName) {
        for (StandaloneRuleParam paramDetail : ruleDetail.paramDetails()) {
            if (paramDetail.name().equals(parameterName)) {
                return Optional.of(paramDetail);
            }
        }
        return Optional.empty();
    }

    public static Optional<Version> detectNodeJSVersion(String nodeJSPath)
    {
        ProcessBuilder processBuilder = new ProcessBuilder(nodeJSPath, "--version");
        try {
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            int executionStatus = start.waitFor();
            if (executionStatus == 0) {
                // NodeJS version is like vxx.xx.xx, no need read more and no need loop
                byte[] buffer = new byte[32];
                inputStream.read(buffer);
                return Optional.of(Version.create(new String(buffer).substring(1)));
            } else {
                LOG.warning("Cannot detect NodeJS version");
            }
        } catch (IOException ex) {
            LOG.warning("Cannot detect NodeJS version: " + ex.getMessage());
        } catch (InterruptedException ex) {
            LOG.warning("Cannot detect NodeJS version: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }
}
