plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "me.rmyhal.contentment"

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    compileSdk = libs.versions.compileSdk.get().toInt()
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
  }
  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
  }
  lint {
    abortOnError = true
  }
}

dependencies {
  api(libs.compose.foundation)
  api(libs.compose.ui)
  implementation(libs.compose.runtime)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
  testImplementation(libs.kotlin.coroutines.test)
}