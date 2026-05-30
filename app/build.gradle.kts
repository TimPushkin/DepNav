import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
}

object Version {
    private const val MAJOR = 1
    private const val MINOR = 4
    private const val PATCH = 5

    const val CODE = MAJOR * 10000 + MINOR * 100 + PATCH
    const val NAME = "$MAJOR.$MINOR.$PATCH"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

android {
    namespace = "ru.spbu.depnav"
    compileSdk = 37

    defaultConfig {
        applicationId = "ru.spbu.depnav"
        minSdk = 24 // ModalNavigationDrawer crashes on 23
        targetSdk = 37
        versionCode = Version.CODE
        versionName = Version.NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = keystorePropertiesFile.inputStream().use {
                Properties().apply { load(it) }
            }
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.google.android.material)

    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    implementation(libs.google.dagger.hilt)
    ksp(libs.google.dagger.hiltCompiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)

    implementation(libs.plrapps.mapcompose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.extJunit)
}

val generateMapsDatabase = tasks.register<GenerateMapsDatabaseTask>("generateMapsDatabase") {
    schemaDirectory = project.layout.projectDirectory
        .dir("schemas/ru.spbu.depnav.data.db.AppDatabase")
    dataDirectory = project.layout.projectDirectory
        .dir("maps/infos")
    outputFile = project.layout.buildDirectory
        .file("intermediates/maps_database/maps.db")
    dependsOn(tasks.named("copyRoomSchemas"))
}
androidComponents {
    onVariants { variant ->
        val copyMapsDatabase = tasks.register<CopyFilesTask>(
            "copyMapsDatabaseTo${variant.name.replaceFirstChar { it.uppercase() }}Assets"
        ) {
            sources.from(generateMapsDatabase.map { it.outputFile })
        }
        variant.sources.assets?.addGeneratedSourceDirectory(
            copyMapsDatabase,
            CopyFilesTask::destination
        )
    }
}
