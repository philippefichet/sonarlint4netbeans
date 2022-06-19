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

import com.github.philippefichet.sonarlint4netbeans.ui.SonarLintAnalyzerOutlineContainer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.commons.progress.CanceledException;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintAnalyzerCancelableTask implements Runnable, Cancellable {

    private static final Logger LOG = Logger.getLogger(SonarLintAnalyzerCancelableTask.class.getName());
    private final SonarLintAnalyzerOutlineContainer sonarLintAnalyzerContainer;
    private final Node[] nodes;
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private ProgressHandle handle;

    public SonarLintAnalyzerCancelableTask(
        SonarLintAnalyzerOutlineContainer sonarLintAnalyzerContainer,
        Node[] nodes
    ) {
        this.sonarLintAnalyzerContainer = sonarLintAnalyzerContainer;
        this.nodes = nodes;
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        handle.setDisplayName("SonarLint Analyzer (canceled)");
        handle.progress("canceled");
        handle.switchToIndeterminate();
        return true;
    }

    public boolean isCanceled()
    {
        return canceled.get();
    }

    @Override
    public void run() {
        handle = ProgressHandle.createHandle("SonarLint Anylazer (init)", this);
        handle.start();
        sonarLintAnalyzerContainer.starting();
        handle.progress(0);
        handle.progress("Init");
        List<File> files = SonarLintUtils.toFiles(nodes);
        // Exclude file supposed not analyzed by an analyzer
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        List<String> fileSuffix = sonarLintEngine.getPluginDetails().stream().map(PluginDetails::key).collect(Collectors.toList());
        List<String> uriFormFiles = files.stream()
            .filter(file -> {
                String[] absolutePathsplit = file.getAbsolutePath().split("\\.");
                return absolutePathsplit.length > 0 && fileSuffix.contains(absolutePathsplit[absolutePathsplit.length - 1]);
            })
            .map(File::toPath)
            .map(Path::toUri)
            .map(URI::getPath)
            .collect(Collectors.toList());
        int maxFileSupposedAnalyzed = uriFormFiles.size();
        handle.switchToDeterminate(maxFileSupposedAnalyzed);
        ClientInputFileListener clientInputFileInputStreamEvent = (URI uri) -> {
            if (!canceled.get()) {
                uriFormFiles.remove(uri.getPath());
                handle.setDisplayName("SonarLint Analyzer");
                handle.progress(maxFileSupposedAnalyzed - uriFormFiles.size());
                handle.progress(SonarLintUtils.toTruncateURI(uri, 75));
            }
        };
        try {
            AnalysisResults analyze = SonarLintUtils.analyze(
                files,
                (Issue issue) -> {
                    sonarLintEngine.getRuleDetails(issue.getRuleKey()).ifPresent((StandaloneRuleDetails ruleDetails) -> {
                        String ruleName = ruleDetails.getName();
                        sonarLintAnalyzerContainer.handle(issue, ruleName);
                    });
                },
                clientInputFileInputStreamEvent,
                this
            );
            if (analyze.failedAnalysisFiles().size() > 0) {
                LOG.warning("SonarLint analyze finish with \"" + analyze.failedAnalysisFiles().size() + "\" failed analysis files.");
            }
        } catch (CanceledException ex) {
            LOG.info("SonarLint analyze canceled");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        sonarLintAnalyzerContainer.ending();
        handle.finish();
    }
}
