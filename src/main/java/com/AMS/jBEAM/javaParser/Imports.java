package com.AMS.jBEAM.javaParser;

import java.util.LinkedHashSet;
import java.util.Set;

class Imports
{
	private final Set<JavaClassInfo>	classes		= new LinkedHashSet<>();
	private final Set<Package> 			packages	= new LinkedHashSet<>();

	void importClass(JavaClassInfo classInfo) {
		classes.add(classInfo);
	}

	void importPackage(Package pack) {
		packages.add(pack);
	}

	Set<JavaClassInfo> getImportedClasses() {
		return classes;
	}

	Set<Package> getImportedPackages() {
		return packages;
	}
}
