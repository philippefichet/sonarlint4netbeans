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

import com.google.gson.Gson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.sonarsource.nodejs.NodeCommand;
import org.sonarsource.nodejs.NodeCommandBuilderImpl;
import org.sonarsource.nodejs.NodeCommandException;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.client.api.common.Language;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.Version;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;

/**
 * Other scanner: https://docs.sonarqube.org/display/PLUG/Plugin+Library TODO:
 * RuleDetails::isActiveByDefault
 *
 * @author FICHET Philippe
 */
public final class SonarLintEngineImpl implements SonarLintEngine {

    // https://search.maven.org/artifact/org.sonarsource.java/sonar-java-plugin/
    public static final String SONAR_JAVA_PLUGIN_VERSION = "7.3.0.27589";
    // https://search.maven.org/artifact/org.sonarsource.javascript/sonar-javascript-plugin/
    public static final String SONAR_JAVASCRIPT_PLUGIN_VERSION = "8.4.0.16431";
    // https://search.maven.org/artifact/org.sonarsource.php/sonar-php-plugin/
    private static String SONAR_PHP_PLUGIN_VERSION = "3.19.0.7847";
    // https://mvnrepository.com/artifact/org.sonarsource.html/sonar-html-plugin
    private static String SONAR_HTML_PLUGIN_VERSION = "3.4.0.2754";
    private static final String PREFIX_PREFERENCE_RULE_PARAMETER = "rules.parameters.";
    private static final String PREFIX_EXCLUDE_RULE = "excludedRules";
    private static final String PREFIX_RUNTIME_PREFERENCE= "runtime.";
    private static final String RUNTIME_NODE_JS_PATH_PREFERENCE= "nodejs.path";
    private static final String RUNTIME_NODE_JS_VERSION_PREFERENCE= "nodejs.version";
    private final Gson gson = new Gson();
    private StandaloneSonarLintEngineImpl standaloneSonarLintEngineImpl;
    private final List<RuleKey> excludedRules = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> consumerWaitingInitialization = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> configurationChanged = new ArrayList<>();
    private final Map<String, URL> pluginURLs = new HashMap<>();

    public SonarLintEngineImpl() throws MalformedURLException {
        pluginURLs.put("java", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-java-plugin-" + SONAR_JAVA_PLUGIN_VERSION + ".jar"));
        pluginURLs.put("javascript", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-javascript-plugin-" + SONAR_JAVASCRIPT_PLUGIN_VERSION + ".jar"));
        pluginURLs.put("php", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-php-plugin-" + SONAR_PHP_PLUGIN_VERSION + ".jar"));
        pluginURLs.put("web", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-html-plugin-" + SONAR_HTML_PLUGIN_VERSION + ".jar"));
        createInternalEngine();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> fromJson = gson.fromJson(getPreferences().get(PREFIX_EXCLUDE_RULE, null), List.class);
        if (fromJson == null) {
            whenInitialized(engine -> {
                Collection<StandaloneRuleDetails> allRuleDetails = engine.getAllRuleDetails();
                for (StandaloneRuleDetails allRuleDetail : allRuleDetails) {
                    if (!allRuleDetail.isActiveByDefault()) {
                        excludedRules.add(RuleKey.parse(allRuleDetail.getKey()));
                    }
                }
                getPreferences().put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
            });
        } else {
            for (Map<String, String> ruleKey : fromJson) {
                excludedRules.add(RuleKey.parse(ruleKey.get("repository") + ":" + ruleKey.get("rule")));
            }
        }
    }

    private void createInternalEngine() {
        standaloneSonarLintEngineImpl = null;
        new Thread(() -> {
            StandaloneGlobalConfiguration.Builder configBuilder = StandaloneGlobalConfiguration.builder()
                .addEnabledLanguages(Language.values())
                .addPlugins(pluginURLs.values().toArray(new URL[pluginURLs.values().size()]));
            if (getNodeJSPath().isPresent() && getNodeJSVersion().isPresent()) {
                String nodeJSPath = getNodeJSPath().get();
                Version nodeJSVersion = getNodeJSVersion().get();
                Path nodeJS = Paths.get(nodeJSPath);
                configBuilder.setNodeJs(nodeJS, nodeJSVersion);
            } else {
                tryToSetDefaultNodeJS(configBuilder);
            }
            standaloneSonarLintEngineImpl = new StandaloneSonarLintEngineImpl(configBuilder.build());
            consumerWaitingInitialization.forEach(consumer -> consumer.accept(this));
            consumerWaitingInitialization.clear();
        }).start();
    }
    
    private void tryToSetDefaultNodeJS(StandaloneGlobalConfiguration.Builder configBuilder) {
        try {
            NodeProcessWrapper nodeProcessWrapper = new NodeProcessWrapper();
            NodeCommand nodeCommandVersion = new NodeCommandBuilderImpl(nodeProcessWrapper)
                .nodeJsArgs("--version")
                .build();
            nodeCommandVersion.start();
            if (nodeCommandVersion.waitFor() == 0 && nodeProcessWrapper.getCommandLineUsed().isPresent()) {
                String nodeJSPath = nodeProcessWrapper.getCommandLineUsed().get().get(0);
                Optional<Version> detectNodeJSVersion = SonarLintUtils.detectNodeJSVersion(nodeJSPath);
                if (detectNodeJSVersion.isPresent()) {
                    configBuilder.setNodeJs(Paths.get(nodeJSPath), detectNodeJSVersion.get());
                    Logger.getLogger(SonarLintEngineImpl.class.getName()).log(Level.SEVERE, "Use default nodejs path");
                }
            }
        } catch (NodeCommandException | IOException ex) {
            Logger.getLogger(SonarLintEngineImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        

    @Override
    public Optional<String> getNodeJSPath() {
        return Optional.ofNullable(getPreferences().get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_PATH_PREFERENCE, null));
    }

    @Override
    public Optional<Version> getNodeJSVersion() {
        return Optional.ofNullable(
            getPreferences().get(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_VERSION_PREFERENCE, null)
        ).map(Version::create);
    }

    @Override
    public void setNodeJSPathAndVersion(String nodeJSPath, Version nodeJSversion) {
        getPreferences().put(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_PATH_PREFERENCE, nodeJSPath);
        getPreferences().put(PREFIX_RUNTIME_PREFERENCE + RUNTIME_NODE_JS_VERSION_PREFERENCE, nodeJSversion.toString());
        // Re-create SonarLint Engine
        createInternalEngine();
    }

    @Override
    public Collection<RuleKey> getExcludedRules() {
        return excludedRules;
    }

    @Override
    public void excludeRuleKeys(List<RuleKey> ruleKeys) {
        excludedRules.addAll(ruleKeys);
        getPreferences().put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void includeRuleKeys(List<RuleKey> ruleKeys) {
        excludedRules.removeAll(ruleKeys);
        getPreferences().put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public boolean isExcluded(RuleDetails ruleDetails) {
        for (RuleKey excludedRule : excludedRules) {
            if (ruleDetails.getKey().equals(excludedRule.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AnalysisResults analyze(StandaloneAnalysisConfiguration configuration, IssueListener issueListener, LogOutput logOutput, ProgressMonitor monitor) {
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
    public Preferences getPreferences() {
        return NbPreferences.forModule(SonarLintEngineImpl.class);
    }

    @Override
    public void whenConfigurationChanged(Consumer<SonarLintEngine> consumer) {
        configurationChanged.add(consumer);
    }

    private void fireConfigurationChange() {
        configurationChanged.forEach(consumer -> consumer.accept(this));
    }

    @Override
    public void includeRuleKey(RuleKey ruleKey) {
        excludedRules.remove(ruleKey);
        getPreferences().put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void excludeRuleKey(RuleKey ruleKey) {
        excludedRules.add(ruleKey);
        getPreferences().put(PREFIX_EXCLUDE_RULE, gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void setRuleParameter(String ruleKey, String parameterName, String parameterValue) {
        getPreferences().put(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName, parameterValue);
        fireConfigurationChange();
    }

    @Override
    public Map<RuleKey, Map<String, String>> getRuleParameters()
    {
        Map<RuleKey, Map<String, String>> ruleParameters = new HashMap<>();
        for (StandaloneRuleDetails standaloneRule : getAllRuleDetails()) {
            String ruleKey = standaloneRule.getKey();
            for (StandaloneRuleParam param : standaloneRule.paramDetails()) {
                if (param instanceof StandaloneRuleParam) {
                    StandaloneRuleParam ruleParam = (StandaloneRuleParam)param;
                    String parameterValue = getPreferences().get(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + ruleParam.name(), ruleParam.defaultValue());
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
    public void removeRuleParameter(String ruleKey, String parameterName) {
        getPreferences().remove(PREFIX_PREFERENCE_RULE_PARAMETER+ ruleKey.replace(":", ".") + "." + parameterName);
        fireConfigurationChange();
    }

    @Override
    public Optional<String> getRuleParameter(String ruleKey, String parameterName) {
        return getRuleDetails(ruleKey).flatMap(ruleDetail -> 
            SonarLintUtils.searchRuleParameter(ruleDetail, parameterName)
                .flatMap(param -> {
                    String parameterValue = getPreferences().get(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName, param.defaultValue());
                    if (parameterValue != null && !parameterValue.equals(param.defaultValue())) {
                        return Optional.of(parameterValue);
                    } else {
                        return Optional.empty();
                    }
                })
        );
    }
}
