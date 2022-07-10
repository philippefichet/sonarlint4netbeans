/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2022 Philippe FICHET.
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

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.commons.Version;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;

/**
 * Main implemantation of SonarLintEngine
 * @author FICHET Philippe
 */
public final class SonarLintEngineImpl implements SonarLintEngine {

    private static final Logger LOG = Logger.getLogger(SonarLintEngineImpl.class.getName());

    // https://search.maven.org/artifact/org.sonarsource.java/sonar-java-plugin/
    private static final String SONAR_JAVA_PLUGIN_VERSION = "7.4.0.27839";
    // https://search.maven.org/artifact/org.sonarsource.javascript/sonar-javascript-plugin/
    private static final String SONAR_JAVASCRIPT_PLUGIN_VERSION = "8.6.0.16913";
    // https://search.maven.org/artifact/org.sonarsource.php/sonar-php-plugin/
    private static final String SONAR_PHP_PLUGIN_VERSION = "3.21.2.8292";
    // https://search.maven.org/artifact/org.sonarsource.html/sonar-html-plugin/
    private static final String SONAR_HTML_PLUGIN_VERSION = "3.6.0.3106";
    // https://search.maven.org/artifact/org.sonarsource.xml/sonar-xml-plugin/
    private static final String SONAR_XML_PLUGIN_VERSION = "2.5.0.3376";
    private static final String PREFIX_PREFERENCE_RULE_PARAMETER = "rules.parameters.";
    private static final String PREFIX_EXCLUDE_RULE = "excludedRules";
    private static final String PREFIX_RUNTIME_EXTRA_PROPERTIES_PREFERENCE = "extraProperties";
    private static final String PREFIX_ADDITIONAL_PLUGINS_PREFERENCE = "additionnalPlugins";
    private static final String PREFIX_RUNTIME_PREFERENCE = "runtime.";
    private static final String RUNTIME_NODE_JS_PATH_PREFERENCE = "nodejs.path";
    private static final String RUNTIME_NODE_JS_VERSION_PREFERENCE = "nodejs.version";
    private final Gson gson = new Gson();
    private StandaloneSonarLintEngineImpl standaloneSonarLintEngineImpl;
    private final List<Consumer<SonarLintEngine>> consumerWaitingInitialization = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<SonarLintEngine>> consumerRestarted = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<SonarLintEngine>> configurationChanged = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Path> pluginPaths = new HashMap<>();
    private final Lookup lookup = Lookup.getDefault();

    public SonarLintEngineImpl() {
        SonarLintDataManager sonarLintDataManager = getSonarLintDataManager();
        pluginPaths.put("java", sonarLintDataManager.getInstalledFile("sonar/plugins/sonar-java-plugin-" + SONAR_JAVA_PLUGIN_VERSION + ".jar").toPath());
        pluginPaths.put("javascript", sonarLintDataManager.getInstalledFile("sonar/plugins/sonar-javascript-plugin-" + SONAR_JAVASCRIPT_PLUGIN_VERSION + ".jar").toPath());
        pluginPaths.put("php", sonarLintDataManager.getInstalledFile("sonar/plugins/sonar-php-plugin-" + SONAR_PHP_PLUGIN_VERSION + ".jar").toPath());
        pluginPaths.put("web", sonarLintDataManager.getInstalledFile("sonar/plugins/sonar-html-plugin-" + SONAR_HTML_PLUGIN_VERSION + ".jar").toPath());
        pluginPaths.put("xml", sonarLintDataManager.getInstalledFile("sonar/plugins/sonar-xml-plugin-" + SONAR_XML_PLUGIN_VERSION + ".jar").toPath());
        createInternalEngine();
    }

    /**
     * Retrieve all base plugins
     * @return all  additionnal plugins
     */
    @Override
    public Set<String> getBasePlugins() {
        return pluginPaths.keySet();
    }

    /**
     * Retrieve all additionnal plugins
     * @return all  additionnal plugins
     */
    @Override
    public Map<String, String> getAdditionnalPlugins() {
        return gson.fromJson(
            getPreferences(GLOBAL_SETTINGS_PROJECT)
            .get(PREFIX_ADDITIONAL_PLUGINS_PREFERENCE, "{}"),
            Map.class
        );
    }

    /**
     * Change all additionnal plugins
     * @param additionnalPluging all additionnal plugins
     */
    @Override
    public void setAdditionnalPlugins(Map<String, String> additionnalPluging) {
        getPreferences(GLOBAL_SETTINGS_PROJECT)
            .put(PREFIX_ADDITIONAL_PLUGINS_PREFERENCE, gson.toJson(additionnalPluging));
        createInternalEngine();
    }

    private void createInternalEngine() {
        StandaloneSonarLintEngineImpl oldStandaloneSonarLintEngineImpl = standaloneSonarLintEngineImpl;
        standaloneSonarLintEngineImpl = null;
        consumerRestarted.forEach(consumer -> consumer.accept(this));
        consumerRestarted.clear();
        new Thread(() -> {
            createInternalEngine(oldStandaloneSonarLintEngineImpl);
        }).start();
    }

    private void createInternalEngine(StandaloneSonarLintEngineImpl oldStandaloneSonarLintEngineImpl) {
        if (oldStandaloneSonarLintEngineImpl != null) {
            oldStandaloneSonarLintEngineImpl.stop();
        }
        Map<String, Path> allPlugins = new HashMap<>(pluginPaths);
        getAdditionnalPlugins().forEach((String key, String url) -> {
            try {
                allPlugins.put(key, Paths.get(url).toRealPath());
            } catch (NoSuchFileException ex) {
                LOG.warning("Additional plugin \"" + key + "\" with path \"" + url + "\" not exists");
            } catch (InvalidPathException ex) {
                LOG.warning("Additional plugin \"" + key + "\" has mal formed path : " + url);
            } catch (IOException ex) {
                LOG.warning("Additional plugin \"" + key + "\" has an error with path : " + url + " : " + ex.getMessage());
            }
        });
        
        List<Path> allPluginPaths = new ArrayList<>(allPlugins.values());
        StandaloneGlobalConfiguration.Builder configBuilder = StandaloneGlobalConfiguration.builder()
            .addEnabledLanguages(Language.values())
            .addPlugins(allPluginPaths.toArray(new Path[allPluginPaths.size()]));
        Optional<String> nodeJSPathOptional = getNodeJSPath();
        Optional<Version> nodeJSVersionOptional = getNodeJSVersion();
        if (nodeJSPathOptional.isPresent() && nodeJSVersionOptional.isPresent()) {
            String nodeJSPath = nodeJSPathOptional.get();
            Version nodeJSVersion = nodeJSVersionOptional.get();
            Path nodeJS = Paths.get(nodeJSPath);
            configBuilder.setNodeJs(nodeJS, nodeJSVersion);
        } else {
            tryToSetDefaultNodeJS(configBuilder);
        }
        standaloneSonarLintEngineImpl = new StandaloneSonarLintEngineImpl(configBuilder.build());
        consumerWaitingInitialization.forEach(consumer -> consumer.accept(this));
        consumerWaitingInitialization.clear();
    }

    private void tryToSetDefaultNodeJS(StandaloneGlobalConfiguration.Builder configBuilder) {
        SonarLintUtils.tryToSearchDefaultNodeJS(
            () -> SonarLintUtils.searchPathEnvVar().orElse(""),
            configBuilder::setNodeJs
        );
    }

    @Override
    public Optional<String> getNodeJSPath() {
        return Optional.ofNullable(getPreferences(GLOBAL_SETTINGS_PROJECT).get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_PATH_PREFERENCE, null));
    }

    @Override
    public Optional<Version> getNodeJSVersion() {
        return Optional.ofNullable(
            getPreferences(GLOBAL_SETTINGS_PROJECT).get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_VERSION_PREFERENCE, null)
        ).map(Version::create);
    }

    @Override
    public void setNodeJSPathAndVersion(String nodeJSPath, Version nodeJSversion) {
        getPreferences(GLOBAL_SETTINGS_PROJECT).put(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_PATH_PREFERENCE, nodeJSPath);
        getPreferences(GLOBAL_SETTINGS_PROJECT).put(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_VERSION_PREFERENCE, nodeJSversion.toString());
        // Re-create SonarLint Engine
        createInternalEngine();
    }

    @Override
    public Collection<RuleKey> getExcludedRules(Project project) {
        String excludedRulesJson = getPreferences(project).get(PREFIX_EXCLUDE_RULE, null);
        Set<Map<String, String>> fromJson = null;
        if (excludedRulesJson != null) {
            fromJson = gson.fromJson(excludedRulesJson, Set.class);
        }

        if (fromJson == null) {
            if (standaloneSonarLintEngineImpl != null) {
                Collection<RuleKey> excludedRules = new HashSet<>();
                Collection<StandaloneRuleDetails> allRuleDetails = standaloneSonarLintEngineImpl.getAllRuleDetails();
                for (StandaloneRuleDetails allRuleDetail : allRuleDetails) {
                    if (!allRuleDetail.isActiveByDefault()) {
                        excludedRules.add(RuleKey.parse(allRuleDetail.getKey()));
                    }
                }
                getPreferences(project).put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
                return excludedRules;
            } else {
                return new ArrayList<>(0);
            }
        } else {
            Collection<RuleKey> excludedRules = new HashSet<>();
            for (Map<String, String> ruleKey : fromJson) {
                excludedRules.add(RuleKey.parse(ruleKey.get("repository") + ":" + ruleKey.get("rule")));
            }
            return excludedRules;
        }
    }

    @Override
    public void excludeRuleKeys(List<RuleKey> ruleKeys, Project project) {
        Collection<RuleKey> excludedRules = getExcludedRules(project);
        excludedRules.addAll(ruleKeys);
        getPreferences(project).put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void includeRuleKeys(List<RuleKey> ruleKeys, Project project) {
        Collection<RuleKey> excludedRules = getExcludedRules(project);
        excludedRules.removeAll(ruleKeys);
        getPreferences(project).put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public boolean isExcluded(RuleDetails ruleDetails, Project project) {
        Collection<RuleKey> excludedRules = getExcludedRules(project);
        for (RuleKey excludedRule : excludedRules) {
            if (ruleDetails.getKey().equals(excludedRule.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AnalysisResults analyze(StandaloneAnalysisConfiguration configuration, IssueListener issueListener, ClientLogOutput logOutput, ClientProgressMonitor monitor) {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.analyze(configuration, issueListener, logOutput, monitor);
    }

    @Override
    public void waitingInitialization() {
        while (standaloneSonarLintEngineImpl == null) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Logger.getLogger(SonarLintEngineImpl.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void whenInitialized(Consumer<SonarLintEngine> consumer) {
        if (standaloneSonarLintEngineImpl != null) {
            consumer.accept(this);
        } else {
            consumerWaitingInitialization.add(consumer);
        }
    }

    @Override
    public void whenRestarted(Consumer<SonarLintEngine> consumer) {
        consumerRestarted.add(consumer);
    }

    @Override
    public Collection<StandaloneRuleDetails> getAllRuleDetails() {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.getAllRuleDetails();
    }

    @Override
    public Collection<PluginDetails> getPluginDetails() {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.getPluginDetails();
    }

    @Override
    public Optional<StandaloneRuleDetails> getRuleDetails(String ruleKey) {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.getRuleDetails(ruleKey);
    }

    @Override
    public Preferences getPreferences(Project project) {
        SonarLintDataManager dataManager = getSonarLintDataManager();
        if (project == SonarLintEngine.GLOBAL_SETTINGS_PROJECT) {
            return dataManager.getGlobalSettingsPreferences();
        } else {
            return dataManager.getPreferences(project);
        }
    }

    @Override
    public void whenConfigurationChanged(Consumer<SonarLintEngine> consumer) {
        configurationChanged.add(consumer);
    }

    private void fireConfigurationChange() {
        configurationChanged.forEach(consumer -> consumer.accept(this));
    }

    @Override
    public void includeRuleKey(RuleKey ruleKey, Project project) {
        Collection<RuleKey> excludedRules = getExcludedRules(project);
        excludedRules.remove(ruleKey);
        getPreferences(project).put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void excludeRuleKey(RuleKey ruleKey, Project project) {
        Collection<RuleKey> excludedRules = getExcludedRules(project);
        excludedRules.add(ruleKey);
        getPreferences(project).put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void setRuleParameter(String ruleKey, String parameterName, String parameterValue, Project project) {
        getPreferences(project).put(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName, parameterValue);
        fireConfigurationChange();
    }

    @Override
    public Map<RuleKey, Map<String, String>> getRuleParameters(Project project) {
        Map<RuleKey, Map<String, String>> ruleParameters = new HashMap<>();
        for (StandaloneRuleDetails standaloneRule : getAllRuleDetails()) {
            String ruleKey = standaloneRule.getKey();
            for (StandaloneRuleParam param : standaloneRule.paramDetails()) {
                if (param instanceof StandaloneRuleParam) {
                    StandaloneRuleParam ruleParam = (StandaloneRuleParam)param;
                    String parameterValue = getPreferences(project).get(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + ruleParam.name(), ruleParam.defaultValue());
                    if (parameterValue != null && !parameterValue.equals(ruleParam.defaultValue())) {
                        RuleKey key = RuleKey.parse(standaloneRule.getKey());
                        Map<String, String> params = ruleParameters.get(key);
                        if (params == null) {
                            params = new HashMap<>();
                            ruleParameters.put(key, params);
                        }
                        params.put(ruleParam.name(), parameterValue);
                    }
                }
            }
        }
        return ruleParameters;
    }

    @Override
    public void removeRuleParameter(String ruleKey, String parameterName, Project project) {
        getPreferences(project).remove(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName);
        fireConfigurationChange();
    }

    @Override
    public Optional<String> getRuleParameter(String ruleKey, String parameterName, Project project) {
        return getRuleDetails(ruleKey).flatMap(ruleDetail -> 
            SonarLintUtils.searchRuleParameter(ruleDetail, parameterName)
                .flatMap(param -> {
                    String parameterValue = getPreferences(project).get(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName, param.defaultValue());
                    if (parameterValue != null && !parameterValue.equals(param.defaultValue())) {
                        return Optional.of(parameterValue);
                    } else {
                        return Optional.empty();
                    }
                })
        );
    }

    @Override
    public Map<String, String> getMergedExtraProperties(Project project) {
        Map<String, String> globalExtraProperties = 
            gson.fromJson(
                getSonarLintDataManager().getGlobalSettingsPreferences().get(PREFIX_RUNTIME_EXTRA_PROPERTIES_PREFERENCE, "{}"),
                Map.class
            );
        if (project != null) {
            globalExtraProperties.putAll(
                gson.fromJson(
                    getSonarLintDataManager().getPreferences(project).get(PREFIX_RUNTIME_EXTRA_PROPERTIES_PREFERENCE, "{}"),
                    Map.class
                )
            );
        }
        return globalExtraProperties;
    }

    @Override
    public Map<String, String> getExtraProperties(Project project) {
        return gson.fromJson(
            getPreferences(project).get(PREFIX_RUNTIME_EXTRA_PROPERTIES_PREFERENCE, "{}"),
            Map.class
        );
    }

    @Override
    public void setExtraProperties(Map<String, String> extraProperties, Project project) {
        getPreferences(project).put(PREFIX_RUNTIME_EXTRA_PROPERTIES_PREFERENCE, gson.toJson(extraProperties));
    }

    private SonarLintDataManager getSonarLintDataManager()
    {
        return lookup.lookup(SonarLintDataManager.class);
    }

    @Override
    public void stop() {
        if (standaloneSonarLintEngineImpl != null) {
            standaloneSonarLintEngineImpl.stop();
        }
    }

}
