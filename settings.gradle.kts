enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement()
{
	repositories()
	{
		gradlePluginPortal()
		maven(url = "https://jitpack.io")
	}
	
	plugins()
	{
		id("dev.strixpyrr.shorthand") version "0.0.3"
	}
}

dependencyResolutionManagement()
{
	versionCatalogs()
	{
		val deps by creating()
		{
			from(files("versions.toml"))
		}
	}
}

rootProject.name = "Kuid"

include("kuid-core")
