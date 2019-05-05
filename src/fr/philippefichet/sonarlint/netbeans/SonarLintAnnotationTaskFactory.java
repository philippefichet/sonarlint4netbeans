/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import java.net.MalformedURLException;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author v
 */
public class SonarLintAnnotationTaskFactory extends EditorAwareJavaSourceTaskFactory {

    private static final Logger LOG = Logger.getLogger(SonarLintAnnotationTaskFactory.class.getCanonicalName());

    public SonarLintAnnotationTaskFactory() throws MalformedURLException {
        super(JavaSource.Phase.UP_TO_DATE, JavaSource.Priority.LOW);
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject fo) {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        sonarLintEngine.whenConfigurationChanged((engine) ->  reschedule(fo) );
        return new SonarLintAnnotationTask(sonarLintEngine, fo);
    }

}
