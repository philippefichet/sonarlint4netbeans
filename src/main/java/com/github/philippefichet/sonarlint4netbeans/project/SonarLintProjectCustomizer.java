/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2020 Philippe FICHET.
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
package com.github.philippefichet.sonarlint4netbeans.project;

import com.github.philippefichet.sonarlint4netbeans.SonarLintDataManager;
import com.github.philippefichet.sonarlint4netbeans.SonarLintEngine;
import com.github.philippefichet.sonarlint4netbeans.project.ui.SonarLintProjectCustomizerPanel;
import com.github.philippefichet.sonarlint4netbeans.project.ui.SonarLintProjectCustomizerRulesPanel;
import com.github.philippefichet.sonarlint4netbeans.project.ui.SonarLintProjectPropertiesPanel;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Class to register rule project settings on many project type
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintProjectCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String SONARLINT_CATEGORGY = "SonarLint"; // NOI18N
    private static final String SONARLINT_RULES_CATEGORGY = "Rules"; // NOI18N
    private static final String SONARLINT_PROPERTIES_CATEGORGY = "Properties"; // NOI18N
    
    @ProjectCustomizer.CompositeCategoryProvider.Registrations({
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-ant-freeform", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-apisupport-project", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-j2ee-clientproject", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-j2ee-earproject", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-j2ee-ejbjarproject", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-java-j2seproject", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-java-j2semodule", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-maven", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-gradle", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-cnd-makeproject", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-php-project", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-web-project", position = 5000),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-web-clientproject", position = 5000),
    })
    public static SonarLintProjectCustomizer create() {
        return new SonarLintProjectCustomizer();
    }

    @NbBundle.Messages(value = {
        "LBL_sonarlint_project_category=SonarLint",
        "LBL_sonarlint_project_rule_category=Rules",
        "LBL_sonarlint_project_properties_category=Properties",
    })
    @Override
    public ProjectCustomizer.Category createCategory(Lookup lkp) {
        return ProjectCustomizer.Category.create(
            SONARLINT_CATEGORGY,
            Bundle.LBL_sonarlint_project_category(),
            null,
            ProjectCustomizer.Category.create(
                SONARLINT_RULES_CATEGORGY,
                Bundle.LBL_sonarlint_project_rule_category(),
                null
            ),
            ProjectCustomizer.Category.create(
                SONARLINT_PROPERTIES_CATEGORGY,
                Bundle.LBL_sonarlint_project_properties_category(),
                null
            )
        );
    }
    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup lookup) {
        Project project = lookup.lookup(Project.class);
        if (category.getName().equals(SONARLINT_CATEGORGY))
        {
            SonarLintDataManager sonarLintDataManager = Lookup.getDefault().lookup(SonarLintDataManager.class);
            return new SonarLintProjectCustomizerPanel(sonarLintDataManager, project, category);
        }
        if (category.getName().equals(SONARLINT_RULES_CATEGORGY))
        {
            SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
            return new SonarLintProjectCustomizerRulesPanel(sonarLintEngine, project, category);
        }
        if (category.getName().equals(SONARLINT_PROPERTIES_CATEGORGY))
        {
            SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
            return new SonarLintProjectPropertiesPanel(sonarLintEngine, project, category);
        }
        return null;
    }
}
