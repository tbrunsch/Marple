package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.utils.ClassInfo;

import java.util.LinkedHashSet;
import java.util.Set;

public class Imports
{
	private final Set<ClassInfo>	classes		= new LinkedHashSet<>();
	private final Set<Package> 			packages	= new LinkedHashSet<>();

	void importClass(ClassInfo classInfo) {
		classes.add(classInfo);
	}

	void importPackage(Package pack) {
		packages.add(pack);
	}

	public Set<ClassInfo> getImportedClasses() {
		return classes;
	}

	public Set<Package> getImportedPackages() {
		return packages;
	}
}
