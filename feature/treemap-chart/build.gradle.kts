plugins {
    id("jyhong.android.feature")
}

android {
    namespace = "com.jyhong.playground.feature.treemap_chart"
}

dependencies {
    implementation(project(":core:chart-engine"))
    implementation(libs.androidx.lifecycle.runtime.compose)
}
