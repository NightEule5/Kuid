
import dev.strixpyrr.shorthand.CompilerArgumentScope.Companion.RequiresOptIn
import dev.strixpyrr.shorthand.JvmDefaultMode.All
import dev.strixpyrr.shorthand.applyPlugins
import dev.strixpyrr.shorthand.freeCompilerArgs
import dev.strixpyrr.shorthand.getting

buildscript {
	repositories()
	{
		maven(url = "https://jitpack.io")
		mavenLocal()
	}
	
	dependencies()
	{
		classpath(group = "dev.strixpyrr", name = "shorthand", version = "0.0.1")
	}
}

plugins {
	kotlin("multiplatform")                   version "1.5.30"
	kotlin("plugin.serialization")            version "1.5.30" apply false
	id("com.bnorm.power.kotlin-power-assert") version "0.10.0" apply false
	`maven-publish`
}

allprojects()
{
	applyPlugins()
	{
		kotlin("multiplatform")
		
		id("maven-publish")
	}
	
	group = "strixpyrr.kuid"
	version = "0.0.1"
	
	repositories()
	{
		mavenCentral()
	}
	
	kotlin()
	{
		jvm()
		
		js(IR)
		{
			nodejs()
			
			useCommonJs()
			
			binaries.library()
		}
	}
}

subprojects()
{
	kotlin()
	{
		jvm()
		{
			compilations.all()
			{
				kotlinOptions.run()
				{
					jvmTarget       = "1.8"
					languageVersion = "1.5"
					
					freeCompilerArgs()
					{
						jvmDefault = All
					}
				}
			}
		}
		
		js(IR)
		{
			compilations.all()
			{
				kotlinOptions.run()
				{
					languageVersion = "1.5"
				}
			}
		}
		
		sourceSets()
		{
			val commonMain by getting()
			{
				dependencies()
				{
					implementation(kotlin("stdlib-common"))
				}
			}
			
			all()
			{
				languageSettings()
				{
					optIn(RequiresOptIn)
				}
			}
		}
	}
	
	dependencies()
	{
		"jvmTestImplementation"(group = "io.kotest", name = "kotest-runner-junit5", version = "4.6.+")
	}
	
	tasks()
	{
		val jvmTest: Test by getting()
		{
			useJUnitPlatform()
		}
	}
}

dependencies()
{
	commonMainApi(projects.kuidCore)
}