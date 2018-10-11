package com.AMS.jBEAM.objectInspection.swing.gui.table;

import java.util.HashSet;
import java.util.Set;

abstract class AbstractTableValueFilter implements TableValueFilterIF
{
    private final Set<Runnable> filterChangedListeners  = new HashSet<>();

    @Override
    public void addFilterChangedListener(Runnable listener) {
        filterChangedListeners.add(listener);
    }

    @Override
    public void removeFilterChangedListener(Runnable listener) {
        filterChangedListeners.remove(listener);
    }

    void fireFilterChanged() {
        filterChangedListeners.forEach(Runnable::run);
    }
}
