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
package com.github.philippefichet.sonarlint4netbeans;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class SonarLintOptions {
    private FileSystem createMemoryFileSystem;
    private FileObject stylesheet;

    public FileObject getSonarLintDetailsStyle() throws IOException
    {
        if (stylesheet == null) {
            OutputStream createAndOpen = getFileSystem().getRoot().createAndOpen("sonar-rule-details-style.css");
            byte[] byteArray = getPreferences().getByteArray("options.stylesheet", null);
            if (byteArray == null || byteArray.length == 0) {
                byteArray = "/**\n  Style for SonarLint Details window rule\n  See javax.swing.text.html.CSS for more available attributes\n */".getBytes();
            }
            createAndOpen.write(byteArray);
            createAndOpen.close();
            stylesheet = getFileSystem().getRoot().getFileObject("sonar-rule-details-style", "css");
            stylesheet.addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    try {
                        getPreferences().putByteArray("options.stylesheet", fe.getFile().asText().getBytes());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
        return stylesheet;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(SonarLintOptions.class);
    }

    private FileSystem getFileSystem()
    {
        if (createMemoryFileSystem == null)
        {
            createMemoryFileSystem = FileUtil.createMemoryFileSystem();
        }
        return createMemoryFileSystem;
    }
}
