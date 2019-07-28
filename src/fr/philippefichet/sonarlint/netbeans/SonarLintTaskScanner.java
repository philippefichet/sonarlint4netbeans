/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

/**
 *
 * @author FICHET Philippe
 */
public class SonarLintTaskScanner extends FileTaskScanner implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(SonarLintTaskScanner.class.getCanonicalName());
    private Callback callback;

    public SonarLintTaskScanner(String displayName, String description) {
        super(displayName, description, "Java/SonarLint");

    }

    public static SonarLintTaskScanner create() {
        ResourceBundle bundle = NbBundle.getBundle(SonarLintTaskScanner.class);
        return new SonarLintTaskScanner(
            bundle.getString("LBL_sonarlint_group"), //NOI18N
            bundle.getString("HINT_sonarlint_group") //NOI18N
        );
    }

    @Override
    public List<? extends Task> scan(FileObject fo) {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        if (sonarLintEngine == null) {
            return Collections.emptyList();
        }
        sonarLintEngine.waitingInitialization();
        try {
            List<Issue> analyze = SonarLintUtils.analyze(fo, null);
            return analyze.stream()
                .map(issue -> Task.create(fo, "nb-sonarlint", issue.getRuleName(), issue.getStartLine()))
                .collect(Collectors.toList());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error during analyze {0}: {1}", new Object[]{fo.getName(), ex.getMessage()});
            return Collections.emptyList();
        }
    }

    @Override
    public void attach(Callback callback) {
        LOG.log(Level.FINE, "attach = callback = {0}", callback);
        this.callback = callback;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LOG.log(Level.FINE, "propertyChange = evt.getPropertyName() = {0}", evt.getPropertyName());
    }

}
