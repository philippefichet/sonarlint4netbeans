/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;

/**
 *
 * @author v
 */
public class SonarLintAnnotationTaskFactory extends EditorAwareJavaSourceTaskFactory {

    private static final Logger LOG = Logger.getLogger(SonarLintAnnotationTaskFactory.class.getCanonicalName());
    private static final String SONAR_JAVA_PLUGIN_VERSION = "5.10.0.16874";
    private StandaloneSonarLintEngineImpl standaloneSonarLintEngineImpl;

    public SonarLintAnnotationTaskFactory() throws MalformedURLException {
        super(JavaSource.Phase.UP_TO_DATE, JavaSource.Priority.LOW);
        LOG.log(Level.SEVERE, "SonarLintAnnotationTaskFactory start at {0}", System.nanoTime());
        URL sonarJavaPluginURL = new URL("http://search.maven.org/remotecontent?filepath=org/sonarsource/java/sonar-java-plugin/" + SONAR_JAVA_PLUGIN_VERSION + "/sonar-java-plugin-" + SONAR_JAVA_PLUGIN_VERSION + ".jar");
        Runnable sonarlintInit = () -> {
            StandaloneGlobalConfiguration config = StandaloneGlobalConfiguration.builder()
                .addPlugin(sonarJavaPluginURL)
                .build();
            standaloneSonarLintEngineImpl = new StandaloneSonarLintEngineImpl(config);
            LOG.log(Level.SEVERE, "SonarLintAnnotationTaskFactory end at {0}", System.nanoTime());
        };
        new Thread(sonarlintInit).start();
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject fo) {
        while (standaloneSonarLintEngineImpl == null) {
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ex) {
                Logger.getLogger(SonarLintAnnotationTaskFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        LOG.severe("SonarLintAnnotationTaskFactory createTask, init = " + (standaloneSonarLintEngineImpl == null));
        return new SonarLintAnnotationTask(standaloneSonarLintEngineImpl, fo);
    }

}
