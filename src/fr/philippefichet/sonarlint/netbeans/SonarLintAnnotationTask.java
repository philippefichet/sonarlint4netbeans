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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;

/**
 *
 * @author FICHET Philippe
 */
public class SonarLintAnnotationTask implements CancellableTask<CompilationInfo> {

    private static final Logger LOG = Logger.getLogger(SonarLintAnnotationTask.class.getCanonicalName());
    private static final Map<FileObject, List<SonarLintAnnotation>> ANNOTATIONS_BY_FILEOBJECT = Collections.synchronizedMap(new HashMap<FileObject, List<SonarLintAnnotation>>());
    private final SonarLintEngine standaloneSonarLintEngineImpl;
    private final FileObject fileObject;

    public SonarLintAnnotationTask(SonarLintEngine sonarLintEngine, FileObject fileObject) {
        this.standaloneSonarLintEngineImpl = sonarLintEngine;
        this.fileObject = fileObject;
        LOG.severe("SonarLintAnnotationTask constructor");
    }

    @Override
    public void cancel() {

    }

    @Override
    public void run(CompilationInfo p) throws Exception {
        // Sonarlint not ready
        if (standaloneSonarLintEngineImpl == null) {
            return;
        }
        LOG.severe("SonarLintAnnotationTask start at " + System.nanoTime());
        final EditorCookie editorCookie = DataObject.find(fileObject).getCookie(EditorCookie.class);
        List<SonarLintAnnotation> currentAnnocationOnFileObject = new ArrayList<>();
        List<SonarLintAnnotation> previousAnnotationOnFileObject = ANNOTATIONS_BY_FILEOBJECT.get(fileObject);

        if (previousAnnotationOnFileObject == null) {
            previousAnnotationOnFileObject = new ArrayList<>();
            ANNOTATIONS_BY_FILEOBJECT.put(fileObject, previousAnnotationOnFileObject);
            final EditorCookie.Observable cookie = DataObject.find(fileObject)
                    .getCookie(EditorCookie.Observable.class);

            // Remove annotation when close
            cookie.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if ((propertyName == null
                            || EditorCookie.Observable.PROP_OPENED_PANES.equals(propertyName))
                            && editorCookie.getOpenedPanes() == null) {
                        ANNOTATIONS_BY_FILEOBJECT.remove(fileObject);
                        cookie.removePropertyChangeListener(this);
                    }
                }
            });
        }

        List<Issue> issues = SonarLintUtils.analyze(fileObject, p.getText());
        issues.forEach(sue -> {
            Integer startLine = sue.getStartLine();
            Integer endLine = sue.getEndLine();
            Integer startLineOffset = sue.getStartLineOffset();
            Integer endLineOffset = sue.getEndLineOffset();
            if (startLine == null || endLine == null) {
                startLine = 1;
                endLine = 1;
                startLineOffset = 0;
                endLineOffset = 0;
            }

            int nbStartLineOffset = NbDocument.findLineOffset(editorCookie.getDocument(), startLine - 1);
            int startOffset = nbStartLineOffset + startLineOffset;
            int nbEndLineOffset = NbDocument.findLineOffset(editorCookie.getDocument(), endLine - 1);
            int endOffset = nbEndLineOffset + endLineOffset;
            int length = endOffset - startOffset;
            currentAnnocationOnFileObject.add(new SonarLintAnnotation(sue.getRuleKey() + " = " + sue.getRuleName(), startOffset, length));
        });

        // Remove all previous Sonarlint annotations
        for (SonarLintAnnotation sonarLintAnnotation : previousAnnotationOnFileObject) {
            NbDocument.removeAnnotation(editorCookie.getDocument(), sonarLintAnnotation);
        }

        // Add current issue as annotations
        for (final SonarLintAnnotation sonarLintAnnotation : currentAnnocationOnFileObject) {
            NbDocument.addAnnotation(editorCookie.getDocument(), new Position() {
                @Override
                public int getOffset() {
                    return (int) sonarLintAnnotation.getStartOffest();
                }
            }, sonarLintAnnotation.getLength(), sonarLintAnnotation);
            sonarLintAnnotation.moveToFront();
        }

        // Current annotation become futur previous annotation
        ANNOTATIONS_BY_FILEOBJECT.put(fileObject, currentAnnocationOnFileObject);
    }

}
