plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.kotlin.compose)
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
  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
  }
  lint {
    abortOnError = true
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xexplicit-api=strict")
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