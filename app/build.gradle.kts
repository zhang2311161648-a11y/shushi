import java.io.FileInputStream
import java.util.Properties

plugins {
    id("naveenapps.plugin.android.app")
    id("com.google.android.gms.oss-licenses-plugin")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
    alias(libs.plugins.compose.compiler)
}

// Release keys are private. Debug builds need no signing configuration.
val signingDirectory = providers.gradleProperty("shushiSigningDirectory")
    .orElse(rootProject.layout.projectDirectory.dir("keys").asFile.absolutePath)
val credentials = File(signingDirectory.get(), "credentials.properties")
val keystore = File(signingDirectory.get(), "android_keystore.jks")
if (credentials.isFile && keystore.isFile) {
    val properties = Properties().apply { credentials.inputStream().use { load(it) } }
    android {
        signingConfigs {
            create("release") {
                keyAlias = properties.getProperty("KEY_ALIAS")
                storePassword = properties.getProperty("KEY_STORE_PASSWORD")
                keyPassword = properties.getProperty("KEY_PASSWORD")
                storeFile = keystore
            }
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        check(credentials.isFile && keystore.isFile) {
            "Release requires private signing files. Use assembleDebug or configure shushiSigningDirectory."
        }
    }
}

android {

    namespace = "com.naveenapps.expensemanager"

    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "com.naveenapps.expensemanager"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            enableUnitTestCoverage = true
        }
        create("macrobenchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.findByName("release")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    testOptions {
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 27 and higher.
                    apiLevel = 30
                    // To include Google services, use "google".
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

dependencies {
    implementation(libs.room.ktx)
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:navigation"))
    implementation(project(":core:notification"))
    implementation(project(":core:repository"))
    implementation(project(":core:settings"))

    implementation(project(":feature:account"))
    implementation(project(":feature:analysis"))
    implementation(project(":feature:budget"))
    implementation(project(":feature:category"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:transaction"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:filter"))
    implementation(project(":feature:country"))
    implementation(project(":feature:currency"))

    implementation(project(":feature:settings"))
    implementation(project(":feature:theme"))
    implementation(project(":feature:language"))
    implementation(project(":feature:export"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:currency"))
    implementation(project(":feature:about"))

    implementation(libs.androidx.splash.screen)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)

    implementation(libs.google.oss.licenses)

    implementation(libs.androidx.biometric)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation("androidx.datastore:datastore-preferences-proto:1.2.0")
}
