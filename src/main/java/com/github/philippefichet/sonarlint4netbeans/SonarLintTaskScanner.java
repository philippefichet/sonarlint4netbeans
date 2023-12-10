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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;

/**
 *
 * @author FICHET Philippe
 */
public class SonarLintTaskScanner extends FileTaskScanner implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(SonarLintTaskScanner.class.getCanonicalName());

    public SonarLintTaskScanner(String displayName, String description) {
        super(displayName, description, "Miscellaneous/SonarLint");
    }

    public static SonarLintTaskScanner create() {
        ResourceBundle bundle = NbBundle.getBundle(SonarLintTaskScanner.class);
        return new SonarLintTaskScanner(
            bundle.getString("LBL_sonarlint_group"), //NOI18N
            bundle.getString("HINT_sonarlint_group") //NOI18N
        );
    }

    @Override
    public List<? extends Task> scan(FileObject fileObject) {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return Collections.emptyList();
        }
        sonarLintEngine.waitingInitialization();
        try {
            List<Issue> analyze = SonarLintUtils.analyze(fileObject, null);
            return analyze.stream()
                .map(issue -> {
                    Integer startLine = issue.getStartLine();
                    Optional<StandaloneRuleDetails> ruleDetails = sonarLintEngine.getRuleDetails(issue.getRuleKey());
                    return Task.create(
                        fileObject,
                        "nb-sonarlint-" + issue.getSeverity().name().toLowerCase(),
                        issue.getRuleKey() + " = " + ruleDetails.map(StandaloneRuleDetails::getName).orElse("unknown"),
                        startLine == null ? 1 : startLine
                    );
                })
                .collect(Collectors.toList());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error during analyze {0}: {1}", new Object[]{fileObject.getName(), ex.getMessage()});
            return Collections.emptyList();
        }
    }

    @Override
    public void attach(Callback callback) {
        // Do nothing
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Do nothing
    }

}
