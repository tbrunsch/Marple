package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.utils.JavaClassInfo;

import java.util.LinkedHashSet;
import java.util.Set;

public class Imports
{
	private final Set<JavaClassInfo>	classes		= new LinkedHashSet<>();
	private final Set<Package> 			packages	= new LinkedHashSet<>();

	void importClass(JavaClassInfo classInfo) {
		classes.add(classInfo);
	}

	void importPackage(Package pack) {
		packages.add(pack);
	}

	public Set<JavaClassInfo> getImportedClasses() {
		return classes;
	}

	public Set<Package> getImportedPackages() {
		return packages;
	}
}
