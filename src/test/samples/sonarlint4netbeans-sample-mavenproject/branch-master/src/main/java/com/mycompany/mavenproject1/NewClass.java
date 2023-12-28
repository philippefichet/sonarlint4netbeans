/*
 * sonarlint4netbeans-sample-mavenproject: Sample for SonarLint integration for Apache Netbeans
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
package com.mycompany.mavenproject1;

import java.nio.file.Path;

public class NewClass {
    private static String resource;
    public static final String const_convition_violation = "TEST";

    /**
     * 
     * @deprecated
     */
    @Deprecated
    public void deprecatedMethod() {}

    public void MethodName_Convention_Violation() {}

     
    // FIXME test
    public static String getInstance() {
        if (resource == null) {
            synchronized (NewClass.class) {
                if (resource == null)
                    resource = "";
            }
        }
        return resource;
    }
    
    public static void lambdaTest()
    {
        String t = "toto";
        Runnable r = () -> {
            for (int i = 0; i < 10; i++) {
                System.out.println(t.trim());
            }
        };
        new Thread(r).start();
    }
    
    public static void tooManyParameter(int param1, int param2, int param3, String param4, long param5, int param6, int param7, int param8, String param9) {
        System.out.println("test");
    }

    /**
     * Use "sonar.java.source=8" property to detected this issue
     */
    public static void checkS3725WithExtraProperties() {
        Path myPath = null;
        if(java.nio.file.Files.exists(myPath)) {  // Noncompliant
            // do something
        }
    }

    /**
     * Use "sonar.java.libraries=${projectDir}/target/lib/*.jar" and 
     * java:S2629 must be enabled and slf4j-api-1.7.36 copied in target/lib
     * to show an issue
     */
    public static void checkJavaS2629WithExtraPropertiesAndProjectDir(String arg1) {
        org.slf4j.LoggerFactory.getLogger("Testing").debug("message: " + arg1);
    }
}
