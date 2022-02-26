/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.philippefichet.sonarlint4netbeans;

import org.sonarsource.nodejs.BundlePathResolver;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public class NodeBundlePathResolver implements BundlePathResolver
{
    @Override
    public String resolve(String relativePath) {
        System.out.println("relativePath = " + relativePath);
        return null;
    }
    
}
