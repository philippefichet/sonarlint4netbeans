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
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
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
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.openide.util.NbPreferences;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.connected.LoadedAnalyzer;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRule;
import org.sonarsource.sonarlint.core.container.standalone.rule.StandaloneRuleParam;

/**
 * Other scanner: https://docs.sonarqube.org/display/PLUG/Plugin+Library TODO:
 * RuleDetails::isActiveByDefault
 *
 * @author FICHET Philippe
 */
public final class SonarLintEngineImpl implements SonarLintEngine {

    // https://search.maven.org/artifact/org.sonarsource.java/sonar-java-plugin/
    public static final String SONAR_JAVA_PLUGIN_VERSION = "6.5.0.22421";
    // https://search.maven.org/artifact/org.sonarsource.javascript/sonar-javascript-plugin/
    public static final String SONAR_JAVASCRIPT_PLUGIN_VERSION = "6.2.1.12157";
    private static final Logger LOG = Logger.getLogger(SonarLintEngineImpl.class.getName());
    private static final String PREFIX_PREFERENCE_RULE_PARAMETER = "rules.parameters.";
    private static final String PREFIX_EXCLUDE_RULE = "excludedRules";
    private final Gson gson = new Gson();
    private boolean instrumentationStarted = false;
    private StandaloneSonarLintEngineImpl standaloneSonarLintEngineImpl;
    private final List<RuleKey> excludedRules = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> consumerWaitingInitialization = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> configurationChanged = new ArrayList<>();
    private final Map<String, URL> pluginURLs = new HashMap<>();

    public SonarLintEngineImpl() throws MalformedURLException {
        pluginURLs.put("java", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-java-plugin-" + SONAR_JAVA_PLUGIN_VERSION + ".jar"));
        pluginURLs.put("javascript", getClass().getResource("/com/github/philippefichet/sonarlint4netbeans/resources/sonar-javascript-plugin-" + SONAR_JAVASCRIPT_PLUGIN_VERSION + ".jar"));
        new Thread(() -> {
            StandaloneGlobalConfiguration config = StandaloneGlobalConfiguration.builder()
                    .addPlugins(pluginURLs.values().toArray(new URL[pluginURLs.values().size()]))
                    .build();
            standaloneSonarLintEngineImpl = new StandaloneSonarLintEngineImpl(config);
            consumerWaitingInitialization.forEach(consumer -> consumer.accept(this));
            consumerWaitingInitialization.clear();
        }).start();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fromJson = gson.fromJson(getPreferences().get(PREFIX_EXCLUDE_RULE, null), List.class);
        if (fromJson == null) {
            whenInitialized(engine -> {
                Collection<RuleDetails> allRuleDetails = engine.getAllRuleDetails();
                for (RuleDetails allRuleDetail : allRuleDetails) {
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

    /**
     * Start instrumentation through local agent but only one time
     */
    private void initInstrumentation() {
        if (!instrumentationStarted) {
            Instrumentation instrumentation = ByteBuddyAgent.install();
            if (instrumentation != null) {
                new AgentBuilder.Default()
                .type(ElementMatchers.nameContains("StandaloneActiveRuleAdapter"))
                .transform((builder, type, classLoader, module) -> 
                    builder.method(ElementMatchers.named("param")
                        .and(ElementMatchers.takesArguments(String.class))
                        .and(ElementMatchers.returns(String.class))
                    ).intercept(MethodDelegation.to(StandaloneActiveRuleAdapterInterceptor.class))
                ).transform((builder, type, classLoader, module) -> 
                    builder.method(ElementMatchers.named("params")
                        .and(ElementMatchers.returns(Map.class))
                    ).intercept(MethodDelegation.to(StandaloneActiveRuleAdapterInterceptor.class))
                ).installOn(instrumentation);
                LOG.info("Instrumentation enabled");
            } else {
                LOG.warning("Instrumentation not available");
            }
            instrumentationStarted = true;
        }
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
        initInstrumentation();
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
    public Collection<RuleDetails> getAllRuleDetails() {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.getAllRuleDetails();
    }

    @Override
    public Collection<LoadedAnalyzer> getLoadedAnalyzers() {
        waitingInitialization();
        return standaloneSonarLintEngineImpl.getLoadedAnalyzers();
    }

    @Override
    public Optional<RuleDetails> getRuleDetails(String ruleKey) {
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
    public void removeRuleParameter(String ruleKey, String parameterName) {
        getPreferences().remove(PREFIX_PREFERENCE_RULE_PARAMETER+ ruleKey.replace(":", ".") + "." + parameterName);
        fireConfigurationChange();
    }

    @Override
    public Optional<String> getRuleParameter(String ruleKey, String parameterName) {
        Optional<RuleDetails> ruleDetails = getRuleDetails(ruleKey);
        if (ruleDetails.isPresent()) {
            RuleDetails ruleDetail = ruleDetails.get();
            if (ruleDetail instanceof StandaloneRule) {
                StandaloneRule standaloneRule = (StandaloneRule)ruleDetail;
                StandaloneRuleParam param = standaloneRule.param(parameterName);
                if (param != null) {
                    String parameterValue = getPreferences().get(PREFIX_PREFERENCE_RULE_PARAMETER + ruleKey.replace(":", ".") + "." + parameterName, param.defaultValue());
                    if (parameterValue != null && !parameterValue.equals(param.defaultValue())) {
                        return Optional.of(parameterValue);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }
}
