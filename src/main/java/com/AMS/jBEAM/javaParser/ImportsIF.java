package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.utils.ClassInfo;

import java.util.Set;

public interface ImportsIF
{
	Set<ClassInfo> getImportedClasses();
	Set<Package> getImportedPackages();
}
