/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2019 Philippe FICHET.
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
package fr.philippefichet.sonarlint.netbeans;

import com.google.gson.Gson;
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

/**
 * Other scanner: https://docs.sonarqube.org/display/PLUG/Plugin+Library TODO:
 * RuleDetails::isActiveByDefault
 *
 * @author FICHET Philippe
 */
public final class SonarLintEngineImpl implements SonarLintEngine {

    public static final String SONAR_JAVA_PLUGIN_VERSION = "5.13.1.18282";
    public static final String SONAR_JAVASCRIPT_PLUGIN_VERSION = "5.2.1.7778";
    private static final Logger LOG = Logger.getLogger(SonarLintEngine.class.getCanonicalName());
    private final Gson gson = new Gson();
    private StandaloneSonarLintEngineImpl standaloneSonarLintEngineImpl;
    private final List<RuleKey> excludedRules = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> consumerWaitingInitialization = new ArrayList<>();
    private final List<Consumer<SonarLintEngine>> configurationChanged = new ArrayList<>();
    private final Map<String, URL> pluginURLs = new HashMap<>();

    public SonarLintEngineImpl() throws MalformedURLException {
        long startAt = System.currentTimeMillis();
        pluginURLs.put("java", getClass().getResource("/fr/philippefichet/sonarlint/netbeans/resources/sonar-java-plugin-" + SONAR_JAVA_PLUGIN_VERSION + ".jar"));
        pluginURLs.put("javascript", getClass().getResource("/fr/philippefichet/sonarlint/netbeans/resources/sonar-javascript-plugin-" + SONAR_JAVASCRIPT_PLUGIN_VERSION + ".jar"));
        LOG.log(Level.SEVERE, "plugins: {0}", pluginURLs);
        new Thread(() -> {
            StandaloneGlobalConfiguration config = StandaloneGlobalConfiguration.builder()
                    .addPlugins(pluginURLs.values().toArray(new URL[pluginURLs.values().size()]))
                    .build();
            standaloneSonarLintEngineImpl = new StandaloneSonarLintEngineImpl(config);
            consumerWaitingInitialization.forEach(consumer -> consumer.accept(this));
            consumerWaitingInitialization.clear();
            LOG.log(Level.SEVERE, "SonarLintAnnotationTaskFactory end at {0}", System.nanoTime());
        }).start();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fromJson = gson.fromJson(getPreferences().get("excludedRules", null), List.class);
        if (fromJson == null) {
            whenInitialized(engine -> {
                Collection<RuleDetails> allRuleDetails = engine.getAllRuleDetails();
                for (RuleDetails allRuleDetail : allRuleDetails) {
                    if (!allRuleDetail.isActiveByDefault()) {
                        excludedRules.add(RuleKey.parse(allRuleDetail.getKey()));
                    }
                }
                getPreferences().put("excludedRules", gson.toJson(excludedRules));
            });
        } else {
            for (Map<String, String> ruleKey : fromJson) {
                excludedRules.add(RuleKey.parse(ruleKey.get("repository") + ":" + ruleKey.get("rule")));
            }
        }
        long endAt = System.currentTimeMillis();
        LOG.log(Level.SEVERE, "init SonarLintEngine done in {0}ms.", endAt - startAt);
    }

    @Override
    public Collection<RuleKey> getExcludedRules() {
        return excludedRules;
    }

    @Override
    public void excludeRuleKeys(List<RuleKey> ruleKeys) {
        excludedRules.addAll(ruleKeys);
        getPreferences().put("excludedRules", gson.toJson(excludedRules));
        fireConfigurationChange();
    }

    @Override
    public void includeRuleKyes(List<RuleKey> ruleKeys) {
        excludedRules.removeAll(ruleKeys);
        getPreferences().put("excludedRules", gson.toJson(excludedRules));
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
                Logger.getLogger(SonarLintAnnotationTaskFactory.class.getName()).log(Level.SEVERE, null, ex);
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
}
