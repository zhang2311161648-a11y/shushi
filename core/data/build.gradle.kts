plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.di")
    id("naveenapps.plugin.room")
}

android {
    namespace = "com.naveenapps.expensemanager.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:repository"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":core:settings"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    implementation(libs.opencsv)
    implementation(libs.gson)

    implementation(libs.androidx.dataStore.preference)
    implementation("androidx.datastore:datastore-preferences-proto:1.2.0")
    implementation(libs.backup.restore)
    implementation(libs.androidx.exifinterface)

    androidTestImplementation(project(":core:testing"))
    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
}
