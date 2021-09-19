import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")                             version "1.5.30"
	kotlin("plugin.serialization")            version "1.5.30"
	id("com.bnorm.power.kotlin-power-assert") version "0.9.0"
	`maven-publish`
}

subprojects()
{
	apply<KotlinPlatformJvmPlugin>()
	apply<MavenPublishPlugin     >()
}

allprojects()
{
	group = "strixpyrr.kuid"
	version = "0.0.1"
	
	repositories()
	{
		mavenCentral()
	}
	
	dependencies()
	{
		implementation(kotlin("stdlib"))
		
		   compileOnly(group = "dev.kord",              name = "kord-common",                version = "0.7.+")
		   compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = "1.2.1")
				   api(group = "org.jetbrains.kotlinx", name = "kotlinx-datetime",           version = "0.2.1")
		implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core",    version = "1.5.+")
		
		testImplementation(group = "io.kotest", name = "kotest-runner-junit5", version = "4.6.+")
	}
	
	tasks()
	{
		withType<KotlinCompile>
		{
			kotlinOptions.run()
			{
				jvmTarget       = "1.8"
				languageVersion = "1.5"
				
				freeCompilerArgs =
					listOf(
						"-Xopt-in=kotlin.RequiresOptIn",
						"-Xjvm-default=all"
					)
			}
		}
		
		kotlinSourcesJar()
		{
			// Include a copy of the license with the source.
			from("LICENSE") { into("META_INF") }
		}
		
		test()
		{
			useJUnitPlatform()
		}
	}
	
	publishing()
	{
		repositories()
		{
			mavenLocal()
		}
		
		publications()
		{
			create("Kuid", MavenPublication::class)
			{
				from(components["kotlin"])
				
				artifact(tasks.kotlinSourcesJar)
			}
		}
	}
}