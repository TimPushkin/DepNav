plugins {
    `kotlin-dsl`
    embeddedKotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.xerial.sqlite.jdbc)
}
