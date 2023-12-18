/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2023 Philippe FICHET.
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


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.sonar.api.utils.ZipUtils;
import org.sonarsource.sonarlint.core.commons.RuleKey;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintTestUtils {
    private static final String NODEJS_VERSION = "14.17.0";
    private SonarLintTestUtils() {
    }

    public static String getNodeJSVersion()
    {
        return NODEJS_VERSION;
    }
    
    public static File getNodeJS()
    {
        if (Utilities.isWindows()) {
            return new File(getNodeJSDirectory(), "node.exe");
        } else {
            return new File(getNodeJSDirectory(), "bin/node");
        }
    }

    public static File getNodeJSDirectory()
    {
        String nodeVerDir = "node-v" + NODEJS_VERSION + getOsNameArch();
        return new File("target/" + nodeVerDir + "/" + nodeVerDir);
    }

    public static void installNodeJS() throws MalformedURLException, IOException
    {
        String nodejsFileName;
        String nodejsFileExtension;
        String osNameArch = getOsNameArch();
        nodejsFileName = "node-v" + NODEJS_VERSION + osNameArch;
        nodejsFileExtension = Utilities.isWindows() ? "zip" : "tar.gz";
        // Download
        Path targetNodeJSZip = Paths.get("./target/" + nodejsFileName + "." + nodejsFileExtension);
        if (!targetNodeJSZip.toFile().exists()) {
            InputStream in = new URL("https://nodejs.org/dist/v" + NODEJS_VERSION + "/" + nodejsFileName + "." + nodejsFileExtension).openStream();
            Files.copy(in, targetNodeJSZip, StandardCopyOption.REPLACE_EXISTING);
        }
        // extract
        File targetDirectory = new File("./target/" + nodejsFileName);
        if (!targetDirectory.exists()) {
            
            if (nodejsFileExtension.equals("zip")) {
                ZipUtils.unzip(targetNodeJSZip.toFile(), targetDirectory);
            } else if (nodejsFileExtension.equals("tar.gz")) {
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(Files.newInputStream(targetNodeJSZip));

                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
                TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
                 // tarIn is a TarArchiveInputStream
                 while (tarEntry != null) {// create a file with the same name as the tarEntry
                     File destPath = new File(targetDirectory, tarEntry.getName());
                     System.out.println("working: " + destPath.getCanonicalPath());
                     if (tarEntry.isDirectory()) {
                         destPath.mkdirs();
                     } else {
                         destPath.createNewFile();
                         //byte [] btoRead = new byte[(int)tarEntry.getSize()];
                         byte [] btoRead = new byte[1024];
                         //FileInputStream fin 
                         //  = new FileInputStream(destPath.getCanonicalPath());
                         BufferedOutputStream bout = 
                             new BufferedOutputStream(new FileOutputStream(destPath));
                         int len = 0;

                         while((len = tarIn.read(btoRead)) != -1)
                         {
                             bout.write(btoRead,0,len);
                         }

                         bout.close();
                         btoRead = null;

                     }
                     tarEntry = tarIn.getNextTarEntry();
                 }
                 tarIn.close();
            }
        }
        if (!Utilities.isWindows()) {
            getNodeJS().setExecutable(true);
        }
    }
    
    private static String getOsNameArch()
    {
        String osArch = System.getProperty("os.arch");
        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.startsWith("windows")) {
                return "-win-x64";
            } else if (osName.startsWith("linux")) {
                return "-linux-x64";
            } else if (osName.contains("mac")) {
                return "-darwin-x64";
            } else {
                throw new IllegalStateException("OS Name \"" + osName + "\" is not supported");
            }  
        } else {
            throw new IllegalStateException("Arch \"" + osArch + "\" is not supported");
        }
    }

    /**
     * Retrieve SonarLintEngine from Lookup but clean all configuration
     * @return clean SonarLintEngine
     * @throws BackingStoreException 
     */
    public static SonarLintEngine getCleanSonarLintEngine() throws BackingStoreException
    {
        SonarLintEngine sonarLintEngine = Lookup.getDefault().lookup(SonarLintEngine.class);
        cleanSonarLintEngine(sonarLintEngine);
        return sonarLintEngine;
    }

    /**
     * Clean all configuration of SonarLintEngine
     * @param sonarLintEngine engine to clean
     * @throws BackingStoreException 
     */
    public static void cleanSonarLintEngine(SonarLintEngine sonarLintEngine) throws BackingStoreException
    {
        List<RuleKey> arrayList = new ArrayList<>(sonarLintEngine.getExcludedRules(SonarLintEngine.GLOBAL_SETTINGS_PROJECT));
        for (RuleKey ruleKey : arrayList) {
            sonarLintEngine.includeRuleKey(ruleKey, SonarLintEngine.GLOBAL_SETTINGS_PROJECT);
        }
        sonarLintEngine.getPreferences(SonarLintEngine.GLOBAL_SETTINGS_PROJECT).removeNode();
    }
    
    /**
     * Clean all configuration of SonarLintEngine from Lookup
     * @throws BackingStoreException 
     */
    public static void cleanSonarLintEngine() throws BackingStoreException
    {
        cleanSonarLintEngine(Lookup.getDefault().lookup(SonarLintEngine.class));
    }

    public static Optional<File> extractSlf4jApiFromCurrentClasspath()
    {
        String[] classpath = System.getProperty("java.class.path").split(File.pathSeparator);
        for (String path : classpath) {
            if (path.contains("slf4j-api"))
            {
                return Optional.of(new File(path).getAbsoluteFile());
            }
        }
        return Optional.empty();
    }
}
