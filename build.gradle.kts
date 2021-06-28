import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

plugins {
	kotlin("jvm")                             version "1.5.20"
	kotlin("plugin.serialization")            version "1.5.20"
	id("com.bnorm.power.kotlin-power-assert") version "0.9.0"
	`maven-publish`
	jacoco
}

subprojects {
	apply<KotlinPlatformJvmPlugin>()
	apply<MavenPublishPlugin     >()
}

allprojects {
	group = "strixpyrr.kuid"
	version = "0.0.1"
	
	repositories {
		mavenCentral()
	}
	
	dependencies {
		implementation(kotlin("stdlib"))
		
		   compileOnly(group = "dev.kord",              name = "kord-common",                version = "0.7.+")
		   compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = "1.2.1")
				   api(group = "org.jetbrains.kotlinx", name = "kotlinx-datetime",           version = "0.2.1")
		implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core",    version = "1.5.+")
		
		testImplementation(group = "io.kotest", name = "kotest-runner-junit5", version = "4.6.+")
	}
	
	kotlin.sourceSets()
	{
		val main by getting
		
		create("jvm9")
		{
			kotlin.source(main.kotlin)
			
			dependsOn(main)
		}
	}
	
	val jvm9 by kotlin.target.compilations.creating()
	{
		kotlinOptions.run()
		{
			jvmTarget       = "9"
			languageVersion = "1.5"
			
			freeCompilerArgs =
				listOf(
					"-Xopt-in=kotlin.RequiresOptIn",
					"-Xjvm-default=all"
				)
		}
		
		source(kotlin.sourceSets["jvm9"])
	}
	
	tasks()
	{
		compileKotlin()
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
		
		compileTestKotlin()
		{
			kotlinOptions.run()
			{
				jvmTarget       = "9"
				languageVersion = "1.5"
				
				freeCompilerArgs =
					listOf(
						"-Xopt-in=kotlin.RequiresOptIn"
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
			
			finalizedBy(jacocoTestReport)
		}
		
		jacocoTestReport()
		{
			dependsOn(test)
			
			reports()
			{
				html.required.set(true)
			}
		}
		
		val jvm9Jar by creating(Jar::class)
		{
			archiveClassifier.set("jvm9")
			
			from(sourceSets["jvm9"].output)
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
				artifact(tasks["jvm9Jar"])
			}
		}
	}
}