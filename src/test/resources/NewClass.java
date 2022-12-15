/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

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
}
