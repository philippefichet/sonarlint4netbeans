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
package com.github.philippefichet.sonarlint4netbeans.treenode;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;
import org.sonarsource.sonarlint.core.client.api.common.analysis.DefaultClientIssue;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class TypeProperty extends PropertySupport.ReadOnly<String> {
    public static final String NAME = "type";
    public static final String DISPLAY_NAME = "Type";

    private final String value;
    public TypeProperty(DefaultClientIssue issue) {
        super(NAME, String.class, DISPLAY_NAME, DISPLAY_NAME);
        value = issue.getType().name();
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }
}