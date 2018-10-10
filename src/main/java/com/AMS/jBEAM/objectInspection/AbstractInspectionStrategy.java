package com.AMS.jBEAM.objectInspection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class AbstractInspectionStrategy implements InspectionStrategyIF
{
    abstract void inspect(ObjectInspector inspector);

    @Override
    public void inspect() {
        ObjectInspector inspector = ObjectInspector.getInspector();
        inspector.runLater(() -> inspect(inspector));
    }
}
