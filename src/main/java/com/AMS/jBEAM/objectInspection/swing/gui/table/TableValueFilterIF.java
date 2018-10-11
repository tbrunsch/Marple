package com.AMS.jBEAM.objectInspection.swing.gui.table;

import javax.swing.*;
import java.util.function.Predicate;

public interface TableValueFilterIF extends Predicate<Object>
{
    boolean isActive();
    void addAvailableValue(Object o);
    JComponent getEditor();
    void addFilterChangedListener(Runnable listener);
    void removeFilterChangedListener(Runnable listener);
}
