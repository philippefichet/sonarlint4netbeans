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

import com.github.philippefichet.sonarlint4netbeans.issue.IssueUtils;
import com.github.philippefichet.sonarlint4netbeans.issue.IssueWrapperForServerIssue;
import com.github.philippefichet.sonarlint4netbeans.project.SonarLintProjectPreferenceScope;
import com.github.philippefichet.sonarlint4netbeans.remote.SonarLintRemoteEngine;
import com.github.philippefichet.sonarlint4netbeans.remote.configuration.SonarLintRemoteProjectConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.apache.commons.text.StringEscapeUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.nodejs.NodeCommand;
import org.sonarsource.nodejs.NodeCommandBuilderImpl;
import org.sonarsource.nodejs.NodeCommandException;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;
import org.sonarsource.sonarlint.core.commons.IssueSeverity;
import org.sonarsource.sonarlint.core.commons.RuleKey;
import org.sonarsource.sonarlint.core.commons.RuleType;
import org.sonarsource.sonarlint.core.commons.Version;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.serverconnection.issues.ServerIssue;

/**
 *
 * @author FICHET Philippe
 */
public final class SonarLintUtils {

    private static final Logger LOG = Logger.getLogger(SonarLintUtils.class.getCanonicalName());
    private static final String HTML_END_END_TD_BEGIN_TD = "\"/></td><td>";
    private static final String HTML_END_TD = "</td>";

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

    public static URL toReadableURL(String url) throws IOException
    {
        URL pluginUrl = new URL(url);
        try (InputStream pluginInputStream = pluginUrl.openStream())
        {
            int read = pluginInputStream.read();
            if (read == -1) {
                throw new IOException("Cannot read first byte from \"" + url + "\"");
            }
        }
        return pluginUrl;
    }

    /**
     * Change or remove (if empty) a rule parameter value
     * @param sonarLintEngine sonarlint engine used to change rule parameter value
     * @param project project concerned by change rule parameter value
     * @param ruleKeyChanged rule key to change parameter value
     * @param parameterName name of parameter
     * @param parameterValue new value of parameter, if null then remove from project configuration
     */
    public static void changeRuleParameterValue(SonarLintEngine sonarLintEngine, Project project, String ruleKeyChanged, String parameterName, String parameterValue) {
        if (parameterValue != null) {
            if (parameterValue.isEmpty()) {
                sonarLintEngine.removeRuleParameter(ruleKeyChanged, parameterName, project);
            } else {
                sonarLintEngine.setRuleParameter(ruleKeyChanged, parameterName, parameterValue, project);
            }
        }
    }

    /**
     * Enable or disable a rule
     * @param sonarLintEngine sonarlint engine used to change rule status
     * @param project project concerned by change rule status
     * @param ruleKeyChanged rule key to change status
     */
    public static void saveEnabledOrDisabledRules(SonarLintEngine sonarLintEngine, Project project, Map<RuleKey, Boolean> ruleKeyChanged)
    {
        List<RuleKey> ruleKeysEnable = new ArrayList<>();
        List<RuleKey> ruleKeysDisable = new ArrayList<>();
        ruleKeyChanged.forEach((ruleKey, enable) -> {
            if (enable) {
                ruleKeysEnable.add(ruleKey);
            } else {
                ruleKeysDisable.add(ruleKey);
            }
        });
        sonarLintEngine.excludeRuleKeys(ruleKeysDisable, project);
        sonarLintEngine.includeRuleKeys(ruleKeysEnable, project);
    }

    /**
     * Copied from org.openide.util.ImageUtilities#isDarkLaF()
     * @return true if current LAF is a dark LAF, false otherwise
     */
    public static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N 
    }

    /**
     * Retrieve classpath path (like com/.../../xx.png) of rule type icon
     * @param type issue type
     * @param useSuffixForDarkLAF use "_dark" suffix if current LAF is dark
     * @return URL of PNG image severity if exist
     */
    public static String getRuleTypePathIconInClasspath(RuleType type, boolean useSuffixForDarkLAF)
    {
        if (useSuffixForDarkLAF && isDarkLaF()) {
            return "com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-type-" + type.name().toLowerCase() + "-16px_dark.png";
        } else {
            return "com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-type-" + type.name().toLowerCase() + "-16px.png";
        }
    }

    /**
     * Retrieve URL of PNG image type if exist
     * @param type type of rule
     * @param useSuffixForDarkLAF to use "_dark" prefix in dark LAF
     * @return URL of PNG image type if exist
     */
    public static Optional<URL> ruleTypePathIconInClasspathToURL(RuleType type, boolean useSuffixForDarkLAF)
    {
        return Optional.ofNullable(
            SonarLintUtils.class.getClassLoader().getResource(getRuleTypePathIconInClasspath(type, useSuffixForDarkLAF))
        );
    }

    /**
     * Retrieve URL of PNG image tags
     * @param useSuffixForDarkLAF to use "_dark" prefix in dark LAF
     * @return URL of PNG image tags
     */
    public static URL tagsPathIconInClasspathToURL(boolean useSuffixForDarkLAF)
    {
        if (useSuffixForDarkLAF && isDarkLaF()) {
            return SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-tags-16px_dark.png");
        } else {
            return SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-tags-16px.png");
        }
    }

    /**
     * Retrieve URL of PNG image severity if exist
     * @param severity Severity from which the URL of the PNG image must be retrieved
     * @return URL of PNG image severity if exist
     */
    public static Optional<URL> ruleSeverityToURL(IssueSeverity severity)
    {
        return Optional.ofNullable(SonarLintUtils.class.getClassLoader().getResource("com/github/philippefichet/sonarlint4netbeans/resources/sonarlint-" + severity.name().toLowerCase() + ".png"));
    }

    /**
     * Retrieve ImageIcon of PNG image severity if exist
     * @param severity Severity of which the icon must be recovered
     * @return ImageIcon of PNG image severity if exist
     */
    public static Optional<ImageIcon> ruleSeverityToImageIcon(IssueSeverity severity)
    {
        return ruleSeverityToURL(severity).map(resource -> new ImageIcon(resource, severity.name().toLowerCase()));
    }

    public static String toURL(RuleDetails ruleDetails)
    {
        String[] keySplit = ruleDetails.getKey().split(":");
        return "https://rules.sonarsource.com/" + keySplit[0] + "/RSPEC-" + keySplit[1].substring(1);
    }

    public static List<Issue> analyze(FileObject fileObject, String contentToAnalyze) throws IOException {
        // TODO scope remote = changement de moteur
        FileObject configRoot = FileUtil.getConfigRoot();
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return Collections.emptyList();
        }
        
        File toFile = FileUtil.toFile(fileObject);
        if (toFile == null) {
            return Collections.emptyList();
        }
        SonarLintOptions sonarlintOptions = Lookup.getDefault().lookup(SonarLintOptions.class);
        SonarLintDataManager dataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
        boolean useTestRules = sonarlintOptions == null || sonarlintOptions.applyDifferentRulesOnTestFiles();
        Project project = dataManager.getProject(fileObject).orElse(SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        Path path = project == null
            ? toFile.getParentFile().toPath()
            : FileUtil.toFile(project.getProjectDirectory()).toPath();
        boolean applyTestRules = useTestRules && dataManager.isTest(toFile);
        String relativizeToFile = path.relativize(toFile.toPath()).toString();
        FSClientInputFile clientInputFile = new FSClientInputFile(
            contentToAnalyze == null ? new String(Files.readAllBytes(toFile.toPath())) : contentToAnalyze,
            toFile.toPath().toAbsolutePath(),
            relativizeToFile,
            applyTestRules,
            FileEncodingQuery.getEncoding(fileObject)
        );

        SonarLintProjectPreferenceScope preferencesScope = dataManager.getPreferencesScope(project);
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        if (preferencesScope == SonarLintProjectPreferenceScope.REMOTE) {
            return analyzeRemote(sonarLintEngine, project, clientInputFile, sonarLintHome);
        }

        Project projectForRules = SonarLintDataManagerUtils.getProjectForAnalyse(dataManager, fileObject);
        List<Issue> issues = new ArrayList<>();
        List<RuleKey> excludedRules = new ArrayList<>(sonarLintEngine.getExcludedRules(projectForRules));
        List<RuleKey> includedRules = new ArrayList<>(sonarLintEngine.getIncludedRules(projectForRules));

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(clientInputFile)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .addRuleParameters(sonarLintEngine.getRuleParameters(projectForRules))
            .putAllExtraProperties(getMergedExtraPropertiesAndReplaceVariables(sonarLintEngine, dataManager.getProject(fileObject).orElse(SonarLintEngine.GLOBAL_SETTINGS_PROJECT)))
            .build();

        LOG.info("Start analyze ...");
        AnalysisResults analyze = sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            issues::add,
            // TODO create Output
            (String formattedMessage, ClientLogOutput.Level level) ->
            LOG.info("[" + level.name() + "] " + formattedMessage),
            null
        );
        LOG.info("End analyze");
        LOG.fine(() -> "Analyze result for file \"" + fileObject.getPath() + "\" : " + analyze);
        return issues;
    }

    private static List<Issue> analyzeRemote(SonarLintEngine sonarLintEngine, Project project, FSClientInputFile clientInputFile, String sonarLintHome) {
        SonarLintRemoteEngine remoteEngine = Lookup.getDefault().lookup(SonarLintRemoteEngine.class);
        SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration = SonarLintRemoteProjectConfiguration.fromProject(project);
        ConnectedAnalysisConfiguration build = ConnectedAnalysisConfiguration.builder()
            .setProjectKey(sonarLintRemoteProjectConfiguration.getProjectKey())
            .addInputFiles(clientInputFile)
            .setBaseDir(new File(sonarLintHome).toPath())
            .build();
        List<Issue> listener = new ArrayList<>();
        AnalysisResults remoteAnalyze = remoteEngine.analyze(
            sonarLintRemoteProjectConfiguration,
            build,
            listener::add,
            (String formattedMessage, ClientLogOutput.Level level) -> {
                System.out.println(level + " | " + formattedMessage);
            },
            new ClientProgressMonitor() {
                @Override
                public boolean isCanceled() {
                    return false;
                }
                
                @Override
                public void setMessage(String msg) {
                    LOG.info("ClientProgressMonitor.setMessage(\"" + msg + "\")");
                }
                
                @Override
                public void setFraction(float fraction) {
                    LOG.info("ClientProgressMonitor.setFraction(" + fraction + ")");
                }
                
                @Override
                public void setIndeterminate(boolean indeterminate) {
                    LOG.info("ClientProgressMonitor.setIndeterminate(" + indeterminate + ")");
                }
            }
        );
        List<ServerIssue> serverIssues = remoteEngine.getServerIssues(sonarLintRemoteProjectConfiguration, clientInputFile.relativePath());
        serverIssues.stream()
            .map((ServerIssue s) -> 
                    new IssueWrapperForServerIssue(
                        clientInputFile,
                        s,
                        // TODO replace by a sonarcloud/sonarqube rules store
                        sonarLintEngine.getRuleDetails(s.getRuleKey())
                    )
            )
            // Remove server issue not found in local change
            .filter((IssueWrapperForServerIssue s) -> s.getStartLine() != -1)
            // Add only no local issue
            .filter((IssueWrapperForServerIssue s) -> !IssueUtils.containsSimilarIssue(listener, s))
            .forEach(listener::add);
        LOG.fine(() -> "Analyze (Remote) result for file \"" + clientInputFile.uri() + "\" : " + remoteAnalyze);
        return listener;
    }

    /**
     * Retrive stylesheet for HTML rule detail description
     * @param sonarLintOptions SonarLint global integration option used to retrieve the stylesheet to apply
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
     * Add "header" rule description (key, type, severity and tags) in StringBuilder
     * @param ruleDetails rule details
     * @param sb StringBuilder to add header rule description
     */
    public static void addHtmlDescriptionHeader(StandaloneRuleDetails ruleDetails, StringBuilder sb)
    {
        sb.append("<table class=\"rule-type-severity-and-tags-container\">\n");
        sb.append("<tr>\n");
        sb.append("<td>")
            .append(ruleDetails.getKey())
            .append(HTML_END_TD);

        SonarLintUtils.ruleSeverityToURL(ruleDetails.getDefaultSeverity())
            .ifPresent(
                severityURL -> 
                    sb.append("<td><img class=\"rule-type-severity-and-tags-container-severity\" src=\"")
                        .append(severityURL.toString())
                        .append(HTML_END_END_TD_BEGIN_TD)
                        .append(ruleDetails.getDefaultSeverity().name())
                        .append(HTML_END_TD)
            );

        SonarLintUtils.ruleTypePathIconInClasspathToURL(ruleDetails.getType(), true)
            .ifPresent(
                typeURL -> 
                    sb.append("<td><img class=\"rule-type-severity-and-tags-container-type\" src=\"")
                        .append(typeURL.toString())
                        .append(HTML_END_END_TD_BEGIN_TD)
                        .append(ruleDetails.getType())
                        .append(HTML_END_TD)
            );

        String[] tags = ruleDetails.getTags();
        if (tags != null && tags.length > 0) {
            StringJoiner tagsJoiner = new StringJoiner(", ");
            for (String tag : tags) {
                
                tagsJoiner.add("<a href=\"https://rules.sonarsource.com/java/tag/" + tag + "\"a>" + tag + "</a>");
            }
            sb.append("<td><img class=\"rule-type-severity-and-tags-container-tags\" src=\"")
                .append(tagsPathIconInClasspathToURL(true))
                .append(HTML_END_END_TD_BEGIN_TD)
                .append(tagsJoiner.toString())
                .append(HTML_END_TD);
        }
        sb.append("</tr>\n");
        sb.append("</table>\n");
    }

    /**
     * Retrieve HTML detail description of rule
     * @param ruleDetails Detail of rule
     * @param ruleParams rule parameter values
     * @return HTML detail description of rule
     */
    public static String toHtmlDescription(StandaloneRuleDetails ruleDetails, Map<StandaloneRuleParam, String> ruleParams)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"")
            .append(ruleDetails.getKey().replaceAll(":", "-"))
            .append("\">\n")
            .append("<h1><a href=\"")
            .append(SonarLintUtils.toURL(ruleDetails))
            .append("\">")
            .append(StringEscapeUtils.escapeHtml4(ruleDetails.getName()))
            .append("</a></h1>\n")
        ;
        addHtmlDescriptionHeader(ruleDetails, sb);
        sb.append(ruleDetails.getHtmlDescription())
            .append("\n</div>")
        ;
        if (!ruleParams.isEmpty()) {
            sb.append("<div class=\"rule-parameters-container\">\n");
            sb.append("<h2>Parameters</h2>\n");
            for (Map.Entry<StandaloneRuleParam, String> entry : ruleParams.entrySet()) {
                StandaloneRuleParam standaloneRuleParam = entry.getKey();
                sb.append("<div class=\"rule-parameters-param\">\n<strong class=\"rule-parameters-param-name\">")
                    .append(standaloneRuleParam.name())
                    .append("</strong>: <span class=\"rule-parameters-description\">")
                    .append(standaloneRuleParam.description())
                    .append("</span>. (<span>value</span>: <span class=\"rule-parameters-value\">")
                    .append(entry.getValue())
                    .append("</span>, <span>default</span>: <span class=\"rule-parameters-default\">")
                    .append(standaloneRuleParam.defaultValue())
                    .append("</span>)</div>\n");
            }
            sb.append("</div>\n");
        }
        return sb.toString();
    }

    /**
     * Extract all parameters of rule with customized or defaut value
     * @param sonarLintEngine instance of SonarLintEngine retrieving the parameters of the rule
     * @param ruleKey rule key ("java:S115", ...)
     * @param project project use to extract rule parameters
     * @return all parameters of rule with customized or defaut value
     */
    public static Map<StandaloneRuleParam, String> extractRuleParameters(SonarLintEngine sonarLintEngine, String ruleKey, Project project)
    {
        Optional<StandaloneRuleDetails> ruleDetailsOptional = sonarLintEngine.getRuleDetails(ruleKey);
        if (ruleDetailsOptional.isPresent())
        {
            StandaloneRuleDetails ruleDetails = ruleDetailsOptional.get();
            Collection<StandaloneRuleParam> paramDetails = ruleDetails.paramDetails();
            if(paramDetails.isEmpty()) {
                return new HashMap<>(0);
            }
            Map<StandaloneRuleParam, String> ruleParameters = new HashMap<>();
            for (StandaloneRuleParam paramDetail : paramDetails) {
                ruleParameters.put(
                    paramDetail,
                    sonarLintEngine.getRuleParameter(ruleKey, paramDetail.name(), project).orElseGet(paramDetail::defaultValue)
                );
            }
            return ruleParameters;
        }
        else
        {
            return Collections.emptyMap();
        }
    }

    public static Map<String, String> getMergedExtraPropertiesAndReplaceVariables(SonarLintEngine sonarLintEngine, Project project)
    {
        if (project != SonarLintEngine.GLOBAL_SETTINGS_PROJECT) {
            FileObject projectDirectoryFileObject = project.getProjectDirectory();
            File projectDirectory = projectDirectoryFileObject != null ? FileUtil.toFile(projectDirectoryFileObject) : null;
            if (projectDirectory != null) {
                Map<String, String> allExtraProperties = new HashMap<>(sonarLintEngine.getMergedExtraProperties(project));
                for (Map.Entry<String, String> entry : allExtraProperties.entrySet()) {
                    if (entry.getValue().contains("${projectDir}")) {
                        entry.setValue(
                            entry.getValue()
                            .replace("${projectDir}", projectDirectory.getPath())
                        );
                    }
                }
                return allExtraProperties;
            }
        }
        return sonarLintEngine.getMergedExtraProperties(project);
    }

    /**
     * 
     * @param files List of files to analyze
     * @param listener Issue listener
     * @param clientInputFileInputStreamEvent Listener for consumption of the URI of the analyzed files
     * @param sonarLintAnalyzerCancelableTask task checking if the analysis is cancelled
     * @return Summary of the analysis
     */
    public static AnalysisResults analyze(
        List<File> files,
        IssueListener listener,
        ClientInputFileListener clientInputFileInputStreamEvent,
        SonarLintAnalyzerCancelableTask sonarLintAnalyzerCancelableTask
    ) {
        // TODO pas besoin du moteur ici tant que la séparation en fichier n'est pas fait
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return new AnalysisResults();
        }

        List<File> fileGlobalSettings = new ArrayList<>();
        Map<Project, List<File>> fileByProject = new HashMap<>();
        SonarLintDataManager dataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);

        // Split files by project
        for (File file : files) {
            Project projectForFile = dataManager.getProject(file).orElse(SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
            if (projectForFile == SonarLintEngine.GLOBAL_SETTINGS_PROJECT) {
                fileGlobalSettings.add(file);
            } else {
                fileByProject.computeIfAbsent(projectForFile, (Project p) -> new ArrayList<>())
                    .add(file);
            }
        }

        // Separe analyze and merge results
        if (!fileGlobalSettings.isEmpty() && !fileByProject.isEmpty() || fileByProject.size() > 1) {
            AnalysisResultsMergerable analysisResults = new AnalysisResultsMergerable();
            if (!fileGlobalSettings.isEmpty()) {
                analysisResults.merge(analyze(fileGlobalSettings, listener, clientInputFileInputStreamEvent, sonarLintAnalyzerCancelableTask));
            }
            for (List<File> value : fileByProject.values()) {
                analysisResults.merge(analyze(value, listener, clientInputFileInputStreamEvent, sonarLintAnalyzerCancelableTask));
            }
            return analysisResults;
        }

        Project projectForRules = SonarLintEngine.GLOBAL_SETTINGS_PROJECT;
        Project project = SonarLintEngine.GLOBAL_SETTINGS_PROJECT;
        if (fileGlobalSettings.isEmpty() && fileByProject.size() == 1) {
            Map.Entry<Project, List<File>> filesByProject = fileByProject.entrySet().iterator().next();
            project = filesByProject.getKey();
            List<File> projectFiles = filesByProject.getValue();
            if (!projectFiles.isEmpty()) {
                projectForRules = SonarLintDataManagerUtils.getProjectForAnalyse(dataManager, FileUtil.toFileObject(projectFiles.get(0)));
            }
        }
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";

        List<FSClientInputFile> clientInputFiles = new ArrayList<>();
        for (File file : files) {
            // Map file to implementation of ClientInputFile
            Path path = file.toPath();
            try {
                Optional<Charset> encoding = dataManager.getEncoding(file);
                if (encoding.isPresent()) {
                    clientInputFiles.add(new FSClientInputFile(
                        new String(Files.readAllBytes(path), encoding.get()),
                        path.toAbsolutePath(),
                        path.toFile().getName(),
                        dataManager.isTest(file),
                        encoding.get()
                    ));
                } else {
                    LOG.warning("Unable to detect encoding from file \"" + file.getAbsolutePath() + "\"");
                }
            } catch (IOException ex) {
                LOG.warning("Error during getEncoding from \"" + file.getAbsolutePath() + "\": " + ex.getMessage());
            }
        }
        
        SonarLintProjectPreferenceScope preferencesScope = dataManager.getPreferencesScope(project);
        if (preferencesScope == SonarLintProjectPreferenceScope.REMOTE) {
            SonarLintRemoteEngine remoteEngine = Lookup.getDefault().lookup(SonarLintRemoteEngine.class);
            SonarLintRemoteProjectConfiguration sonarLintRemoteProjectConfiguration = SonarLintRemoteProjectConfiguration.fromProject(project);
            ConnectedAnalysisConfiguration build = ConnectedAnalysisConfiguration.builder()
                .setProjectKey(sonarLintRemoteProjectConfiguration.getProjectKey())
                .addInputFiles(clientInputFiles)
                // TODO supprimer le lien avec sonarLintEngine
                .putAllExtraProperties(getMergedExtraPropertiesAndReplaceVariables(sonarLintEngine, project))
                .build();
            return remoteEngine.analyze(
                sonarLintRemoteProjectConfiguration,
                build,
                listener,
                null,
                new ClientProgressMonitor() {
                    @Override
                    public boolean isCanceled() {
                        return sonarLintAnalyzerCancelableTask != null && sonarLintAnalyzerCancelableTask.isCanceled();
                    }

                    @Override
                    public void setMessage(String msg) {
                        LOG.info("ClientProgressMonitor.setMessage(\"" + msg + "\")");
                    }

                    @Override
                    public void setFraction(float fraction) {
                        LOG.info("ClientProgressMonitor.setFraction(" + fraction + ")");
                    }

                    @Override
                    public void setIndeterminate(boolean indeterminate) {
                        LOG.info("ClientProgressMonitor.setIndeterminate(" + indeterminate + ")");
                    }
                }
            );
        }

        List<RuleKey> excludedRules = new ArrayList<>(sonarLintEngine.getExcludedRules(projectForRules));
        List<RuleKey> includedRules = new ArrayList<>(sonarLintEngine.getIncludedRules(projectForRules));

        StandaloneAnalysisConfiguration standaloneAnalysisConfiguration =
            StandaloneAnalysisConfiguration.builder()
            .setBaseDir(new File(sonarLintHome).toPath())
            .addInputFiles(clientInputFiles)
            .addExcludedRules(excludedRules)
            .addIncludedRules(includedRules)
            .addRuleParameters(sonarLintEngine.getRuleParameters(project))
            .putAllExtraProperties(getMergedExtraPropertiesAndReplaceVariables(sonarLintEngine, project))
            .build();

        // Add listener only after configuration to prevent ClientInputFile.uri() call during configuration phase
        clientInputFiles.forEach(file -> file.addListener(clientInputFileInputStreamEvent));
        return sonarLintEngine.analyze(
            standaloneAnalysisConfiguration,
            listener,
            null,
            new ClientProgressMonitor() {
                @Override
                public boolean isCanceled() {
                    return sonarLintAnalyzerCancelableTask != null && sonarLintAnalyzerCancelableTask.isCanceled();
                }

                @Override
                public void setMessage(String msg) {
                    LOG.info("ClientProgressMonitor.setMessage(\"" + msg + "\")");
                }

                @Override
                public void setFraction(float fraction) {
                    LOG.info("ClientProgressMonitor.setFraction(" + fraction + ")");
                }

                @Override
                public void setIndeterminate(boolean indeterminate) {
                    LOG.info("ClientProgressMonitor.setIndeterminate(" + indeterminate + ")");
                }
            }
        );
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
     * @param uri URI to truncate if too long
     * @param maximalLength maximal size wanted
     * @return String URI truncate if too long, String inital URI otherwise
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
                int read = inputStream.read(buffer);
                if (read > 0) {
                    return Optional.of(Version.create(new String(buffer).substring(1)));
                } else {
                    LOG.warning("Cannot detect NodeJS version");
                }
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
    
    public static Optional<String> searchPathEnvVar()
    {
        try {
            Process exec = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", "env"});
            int waitFor = exec.waitFor();
            if(waitFor == 0) {
                try(
                    InputStreamReader isr = new InputStreamReader(exec.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                ) {
                    Optional<String> findFirst = br.lines().filter(line -> line.startsWith("PATH=")).findFirst();
                    if (findFirst.isPresent()) {
                        return Optional.of(findFirst.get().substring(5));
                    }
                }
            } else {
                LOG.log(Level.WARNING, "Error while read environment variables, exit code {0}", waitFor);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Error while found environment variables : {0}", ex.getMessage());
        } catch (InterruptedException ex) {
            LOG.log(Level.WARNING, "Interruped while found environment variables : {0}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public static void tryToSearchDefaultNodeJS(Supplier<String> pathEnvironmentVariable, BiConsumer<Path, Version> consumer) {
        try {
            NodeProcessWrapper nodeProcessWrapper = new NodeProcessWrapper();
            NodeCommand nodeCommandVersion = new NodeCommandBuilderImpl(nodeProcessWrapper)
                .nodeJsArgs("--version")
                .pathResolver(
                    new NodeBundlePathResolver(
                        pathEnvironmentVariable,
                        File.pathSeparator,
                        (String basePath, String pathSearch) -> {
                            String fullPath = basePath + File.separator + pathSearch;
                            File fullPathFile = new File(fullPath);
                            LOG.fine("Node pathResolver for \"" + basePath + "\" and \"" + pathSearch + "\" to \"" + fullPath + "\"");
                            LOG.fine("Node pathResolver \"" + fullPath + "\" exists \"" + fullPathFile.exists() + "\" and isFile \"" + fullPathFile.isFile() + "\"");
                            if (fullPathFile.exists() && fullPathFile.isFile()) {
                                LOG.fine("Node pathResolver return \"" + fullPath + "\"");
                                return fullPath;
                            }
                            return null;
                        }
                    )
                ).build();
            nodeCommandVersion.start();
            if (nodeCommandVersion.waitFor() == 0 && nodeProcessWrapper.getCommandLineUsed().isPresent()) {
                String nodeJSPath = nodeProcessWrapper.getCommandLineUsed().get().get(0);
                Optional<Version> detectNodeJSVersion = SonarLintUtils.detectNodeJSVersion(nodeJSPath);
                if (detectNodeJSVersion.isPresent()) {
                    consumer.accept(Paths.get(nodeJSPath), detectNodeJSVersion.get());
                    Logger.getLogger(SonarLintUtils.class.getName()).log(Level.INFO, "Use default Node.js path");
                }
            }
        } catch (NodeCommandException | IOException ex) {
            Logger.getLogger(SonarLintUtils.class.getName()).log(Level.INFO, "Cannot use default installation of Node.js: " + ex.getMessage());
        }
    }
}
