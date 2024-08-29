plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.compose) apply false

    // Must be declared in the same scope as AGP to fix https://github.com/google/dagger/issues/3068
    alias(libs.plugins.google.dagger.hilt.android) apply false
    // Must be declared in the same scope as Hilt
    alias(libs.plugins.google.devtools.ksp) apply false
}
