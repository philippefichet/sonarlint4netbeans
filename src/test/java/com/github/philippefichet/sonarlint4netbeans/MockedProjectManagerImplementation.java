/*
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
package com.github.philippefichet.sonarlint4netbeans;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

/**
 * Implementation of ProjectManagerImplementation to use FileEncodingQuery.getEncoding
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class MockedProjectManagerImplementation implements ProjectManagerImplementation {

    @Override
    public void init(ProjectManagerCallBack callBack) {

    }

    @Override
    public Mutex getMutex() {
        throw new UnsupportedOperationException("getMutex Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mutex getMutex(boolean autoSave, Project project, Project... otherProjects) {
        throw new UnsupportedOperationException("getMutex2 Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Project findProject(FileObject projectDirectory) throws IOException, IllegalArgumentException {
        return null;
    }

    @Override
    public ProjectManager.Result isProject(FileObject projectDirectory) throws IllegalArgumentException {
        throw new UnsupportedOperationException("isProject Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearNonProjectCache() {
        throw new UnsupportedOperationException("clearNonProjectCache Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Project> getModifiedProjects() {
        throw new UnsupportedOperationException("getModifiedProjects Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isModified(Project p) {
        throw new UnsupportedOperationException("isModified Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isValid(Project p) {
        throw new UnsupportedOperationException("isValid Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveProject(Project p) throws IOException {
        throw new UnsupportedOperationException("saveProject Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveAllProjects() throws IOException {
        throw new UnsupportedOperationException("saveAllProjects Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
