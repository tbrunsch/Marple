package com.AMS.jBEAM.javaParser.settings;

import com.AMS.jBEAM.javaParser.utils.wrappers.ClassInfo;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Imports
{
	private final ImmutableSet<ClassInfo>	importClasses;
	private final ImmutableSet<String>		importPackageNames;

	public Imports(Set<ClassInfo> importClasses, Set<String> importPackageNames) {
		this.importClasses = ImmutableSet.copyOf(importClasses);
		this.importPackageNames = ImmutableSet.copyOf(importPackageNames);
	}

	public Set<ClassInfo> getImportedClasses() {
		return importClasses;
	}

	public ImmutableSet<String> getImportedPackageNames() {
		return importPackageNames;
	}
}
