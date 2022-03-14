plugins {
	kotlin("plugin.serialization")
	id("com.bnorm.power.kotlin-power-assert")
}

dependencies()
{
	commonMainCompileOnly   (deps.kotlinx.serialization)
	commonMainApi           (deps.kotlinx.datetime     )
	commonMainImplementation(deps.kotlinx.coroutines   )
}