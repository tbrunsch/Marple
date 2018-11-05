package com.AMS.jBEAM.javaParser;

class ObjectInfo
{
    private final Object    object;
    private final Class<?>  declaredClass;

    ObjectInfo(Object object) {
    	this(object, object == null ? null : object.getClass());
	}

    ObjectInfo(Object object, Class<?> declaredClass) {
        this.object = object;
        this.declaredClass = declaredClass;
    }

    Object getObject() {
        return object;
    }

    Class<?> getDeclaredClass() {
        return declaredClass;
    }
}
