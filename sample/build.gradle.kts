plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "me.rmyhal.contentment.sample"

  defaultConfig {
    applicationId = namespace
    minSdk = 31
    compileSdk = libs.versions.compileSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
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
  implementation(project(":library"))
  implementation(libs.androidx.core)
  implementation(libs.compose.activity)
  implementation(libs.compose.runtime)
  implementation(libs.material3)
}