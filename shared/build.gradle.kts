plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dermochelys.utcclock.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            debugSymbolLevel = "FULL"
        }

        testInstrumentationRunner = "com.dermochelys.utcclock.HiltTestRunner"
    }

    buildTypes {
        release {
            // Let minification happen in consuming modules
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Werror")
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                )
        )
    }

    hilt {
        enableAggregatingTask = true
    }

    lint {
        warningsAsErrors = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.foundation)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)

    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
}
