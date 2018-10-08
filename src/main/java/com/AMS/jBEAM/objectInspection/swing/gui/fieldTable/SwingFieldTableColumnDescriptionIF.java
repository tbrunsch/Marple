package com.AMS.jBEAM.objectInspection.swing.gui.fieldTable;

import java.lang.reflect.Field;

public interface SwingFieldTableColumnDescriptionIF
{
    String getName();
    Class<?> getColumnClass();
    Object extractValue(Field field);
}
