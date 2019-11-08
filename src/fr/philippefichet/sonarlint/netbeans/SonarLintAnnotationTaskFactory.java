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
