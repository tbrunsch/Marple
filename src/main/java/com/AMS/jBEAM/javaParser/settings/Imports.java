package com.AMS.jBEAM.javaParser.settings;

import com.AMS.jBEAM.javaParser.utils.ClassInfo;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class Imports
{
	private final ImmutableSet<ClassInfo>	importClasses;
	private final ImmutableSet<Package>		importPackages;

	public Imports(ImmutableSet<ClassInfo> importClasses, ImmutableSet<Package> importPackages) {
		this.importClasses = importClasses;
		this.importPackages = importPackages;
	}

	public Set<ClassInfo> getImportedClasses() {
		return importClasses;
	}

	public Set<Package> getImportedPackages() {
		return importPackages;
	}
}
