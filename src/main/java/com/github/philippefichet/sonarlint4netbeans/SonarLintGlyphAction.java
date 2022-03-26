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

import com.github.philippefichet.sonarlint4netbeans.annotation.SonarLintAnnotation;
import com.github.philippefichet.sonarlint4netbeans.annotation.SonarLintAnnotationHandler;
import com.github.philippefichet.sonarlint4netbeans.ui.SonarRuleDetailsTopComponent;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintGlyphAction extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(SonarLintGlyphAction.class.getCanonicalName());

    public SonarLintGlyphAction() {
        LOG.severe("SonarLintGlyphAction");
        putValue(NAME, 
            NbBundle.getMessage(
                SonarLintGlyphAction.class, 
                "SonarLintGlyphAction.TXT_GlyphActionName" // NOI18N
            )
        );
        putValue("supported-annotation-types", new String[] {
            SonarLintAnnotation.ANNOTATION_TYPE_GENERIC,
            SonarLintAnnotation.ANNOTATION_TYPE_BLOCKER,
            SonarLintAnnotation.ANNOTATION_TYPE_CRITIAL,
            SonarLintAnnotation.ANNOTATION_TYPE_INFO,
            SonarLintAnnotation.ANNOTATION_TYPE_MAJOR,
            SonarLintAnnotation.ANNOTATION_TYPE_MINOR,
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JEditorPane editorPane = (JEditorPane)e.getSource();
        final Document doc = editorPane.getDocument();
        if (doc instanceof BaseDocument) {
            final int currentPosition = editorPane.getCaretPosition();
            final Annotations annotations = ((BaseDocument) doc).getAnnotations();
            final DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (od == null) {
                return;
            }
            SonarLintDataManager dataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
            FileObject primaryFile = od.getPrimaryFile();
            final Project project = dataManager.getProject(primaryFile).orElse(SonarLintEngine.GLOBAL_SETTINGS_PROJECT);

            doc.render(() -> {
                try {
                    int line = LineDocumentUtils.getLineIndex((BaseDocument)doc, currentPosition);
                    AnnotationDesc desc = annotations.getActiveAnnotation(line);
                    Optional<SonarLintAnnotation> sonarLintAnnotation = SonarLintAnnotationHandler.getSonarLintAnnotation(
                        od.getPrimaryFile(),
                        desc.getOffset(),
                        desc.getLength(),
                        desc.getShortDescription()
                    );
                    sonarLintAnnotation.ifPresent(sla -> {
                        TopComponent topComponent = WindowManager.getDefault().findTopComponent("SonarRuleDetailsTopComponent");
                        if (topComponent instanceof SonarRuleDetailsTopComponent) {
                            SonarRuleDetailsTopComponent sonarRuleDetailsTopComponent = (SonarRuleDetailsTopComponent)topComponent;
                            sonarRuleDetailsTopComponent.open(project);
                            sonarRuleDetailsTopComponent.requestActive();
                            sonarRuleDetailsTopComponent.requestFocusInWindow();
                            sonarRuleDetailsTopComponent.setSonarRuleKeyFilter(sla.getRuleKey(), project);
                        }
                    });
                } catch (BadLocationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            });
        }
        
    }
}
