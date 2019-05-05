/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.Position;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.sonarsource.sonarlint.core.client.api.common.RuleDetails;
import org.sonarsource.sonarlint.core.client.api.common.RuleKey;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;

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

    /**
     * Check if file is in test directory from project
     *
     * @param fileObject
     * @return true if file is in test directory from project
     */
    private boolean isTest(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project != null) {
            File projectFile = FileUtil.toFile(project.getProjectDirectory());
            File file = FileUtil.toFile(fileObject);
            if (file.getAbsolutePath().startsWith(projectFile.getAbsolutePath())) {
                String relativeProjectPath = file.getAbsolutePath().replace(projectFile.getAbsolutePath(), "");
                if (relativeProjectPath.contains(File.separator + "test" + File.separator)) {
                    LOG.severe(fileObject.getName() + " is test");
                    return true;
                }
            }
        }
        LOG.severe(fileObject.getName() + " is not test");
        return false;
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

        File toFile = FileUtil.toFile(fileObject);
        Path path = toFile.toPath();
        List<ClientInputFile> files = new ArrayList<>();

        files.add(new FSClientInputFile(
                p.getText(),
                path.toAbsolutePath(),
                path.toFile().getName(),
                isTest(fileObject),
                FileEncodingQuery.getEncoding(fileObject))
        );
        String sonarLintHome = System.getProperty("user.home") + File.separator + ".sonarlint4netbeans";
        List<Issue> issues = new ArrayList<>();
        Collection<RuleDetails> allRuleDetails = standaloneSonarLintEngineImpl.getAllRuleDetails();
        List<RuleKey> excludedRules = new ArrayList<>();
        List<RuleKey> includedRules = new ArrayList<>();
        for (RuleDetails allRuleDetail : allRuleDetails) {
            RuleKey ruleKey = RuleKey.parse(allRuleDetail.getKey());
            if (standaloneSonarLintEngineImpl.isExcluded(allRuleDetail)) {
                excludedRules.add(ruleKey);
            } else {
                includedRules.add(ruleKey);
            }
        }

        AnalysisResults analyze = standaloneSonarLintEngineImpl.analyze(
                new StandaloneAnalysisConfiguration(
                        new File(sonarLintHome).toPath(),
                        new File(sonarLintHome + File.separator + "work").toPath(),
                        files,
                        Collections.emptyMap(),
                        excludedRules,
                        includedRules
                ),
                issues::add,
                null,
                null
        );

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
