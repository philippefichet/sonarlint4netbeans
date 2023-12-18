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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FICHET Philippe &lt;philippe.fichet@laposte.net&gt;
 */
public final class SonarLintPropertiesTableModel extends DefaultTableModel {
    private static final int KEY_COLUMN_INDEX = 0;
    private static final int VALUE_COLUMN_INDEX = 1;
    public SonarLintPropertiesTableModel(String keyName, String valueName) {
        super();
        addColumn(keyName);
        addColumn(valueName);
    }

    public void setProperties(Map<String, String> properties) {
        while (this.getRowCount() > 0) {
            this.removeRow(0);
        }
        properties.forEach(this::addProperty);
    }

    public void forEach(BiConsumer<String, String> biConsumer) {
        for (int i = 0; i < getRowCount(); i++) {
            biConsumer.accept(
                getPropertyKey(i),
                getPropertyValue(i)
            );
        }
    }

    public String getPropertyKey(int row) {
        return (String)getValueAt(row, KEY_COLUMN_INDEX);
    }

    public String getPropertyValue(int row) {
        return (String)getValueAt(row, VALUE_COLUMN_INDEX);
    }

    private void addProperty(String key, String value) {
        addRow(new Object[] {key, value});
    }

    public void addOrUpdateProperty(String key, String value) {
        for (int i = 0; i < getRowCount(); i++) {
            String propertyName = (String)getValueAt(i, KEY_COLUMN_INDEX);
            if (propertyName.equals(key)) {
                setValueAt(value, i, VALUE_COLUMN_INDEX);
                return;
            }
        }
        addProperty(key, value);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public Map<String, String> toPropertiesMap() {
        Map<String, String> extraProperties = new HashMap<>();
        for (int i = 0; i < getRowCount(); i++) {
            extraProperties.put(
                (String)getValueAt(i, KEY_COLUMN_INDEX),
                (String)getValueAt(i, VALUE_COLUMN_INDEX)
            );
        }
        return extraProperties;
    }
}
