/*
 * sonarlint4netbeans: SonarLint integration for Apache Netbeans
 * Copyright (C) 2019 Philippe FICHET.
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
