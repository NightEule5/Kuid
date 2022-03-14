
import dev.strixpyrr.shorthand.CompilerArgumentScope.Companion.RequiresOptIn
import dev.strixpyrr.shorthand.JvmDefaultMode.All
import dev.strixpyrr.shorthand.applyPlugins
import dev.strixpyrr.shorthand.freeCompilerArgs
import dev.strixpyrr.shorthand.getting

plugins {
	kotlin("multiplatform")                   version "1.6.10"
	kotlin("plugin.serialization")            version "1.6.10" apply false
	id("com.bnorm.power.kotlin-power-assert") version "0.10.0" apply false
	id("dev.strixpyrr.shorthand")
	`maven-publish`
}

// Workaround for #18237
var _deps = deps

allprojects()
{
	applyPlugins()
	{
		kotlin("multiplatform")
		
		id("maven-publish")
	}
	
	group = "dev.strixpyrr.kuid"
	version = "0.0.2"
	
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
					languageVersion = "1.6"
					
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
					languageVersion = "1.6"
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
		"jvmTestImplementation"(_deps.kotest)
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