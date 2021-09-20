# Kuid

Kuid (**K**otlin **u**nique **id**s) is a simple, multiplatform implementation of [Snowflake](https://github.com/twitter-archive/snowflake/tree/b3f6a3c6ca8e1b6847baa6ff42bf72201e2c2231) for Kotlin.

## Installation

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(group = "dev.strixpyrr.kuid", name = "kuid", version = "0.0.1")
}
```

## Usage

### Simple Example

```kotlin
// The inception of your application
val epoch: Instant = // ...

val layout = SnowflakeLayout(epoch)

// A unique Id for a process within your application, from 0 to 31. This can be 0
// if you only have one type of process.
val processId = 0

// A unique Id for a worker within the above process, from 0 to 31. This can be 0
// if you don't need multiple threads generating Ids.
val workerId = 0

val generator = SnowflakeGenerator(layout, workerId, processId)

// Generate 100 unique Ids
repeat(100) {
    val id = generator.next()

    // ...
}
```