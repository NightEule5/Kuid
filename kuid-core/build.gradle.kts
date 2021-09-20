plugins {
	kotlin("plugin.serialization")
	id("com.bnorm.power.kotlin-power-assert")
}

dependencies()
{
	   jvmMainCompileOnly   (group = "dev.kord",              name = "kord-common",                version = "0.7.+")
	commonMainCompileOnly   (group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = "1.2.1")
	commonMainApi           (group = "org.jetbrains.kotlinx", name = "kotlinx-datetime",           version = "0.2.1")
	commonMainImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core",    version = "1.5.+")
}