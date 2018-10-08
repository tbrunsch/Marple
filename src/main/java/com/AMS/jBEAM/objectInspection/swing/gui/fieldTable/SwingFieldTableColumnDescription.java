package com.AMS.jBEAM.objectInspection.swing.gui.fieldTable;

import java.lang.reflect.Field;
import java.util.function.Function;

public class SwingFieldTableColumnDescription implements SwingFieldTableColumnDescriptionIF
{
    private final String                    name;
    private final Class<?>                  clazz;
    private final Function<Field, Object>   valueExtractor;

    public SwingFieldTableColumnDescription(String name, Class<?> clazz, Function<Field, Object> valueExtractor) {
        this.name = name;
        this.clazz = clazz;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getColumnClass() {
        return clazz;
    }

    @Override
    public Object extractValue(Field field) {
        return valueExtractor.apply(field);
    }
}
