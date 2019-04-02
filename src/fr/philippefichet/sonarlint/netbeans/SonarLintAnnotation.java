/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.sonarlint.netbeans;

import org.openide.text.Annotation;

/**
 *
 * @author FICHET Philippe
 */
public class SonarLintAnnotation extends Annotation {

    private static final String ANNOTATION_TYPE = "fr-philippefichet-sonarlint-netbeans-annotation";
    private final long startOffest;
    private final int length;
    private final String shortDescription;

    public SonarLintAnnotation(String shortDescription, long startOffest, int length) {
        super();
        this.startOffest = startOffest;
        this.length = length;
        this.shortDescription = shortDescription;
    }

    public long getStartOffest() {
        return startOffest;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getAnnotationType() {
        return ANNOTATION_TYPE;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

}
