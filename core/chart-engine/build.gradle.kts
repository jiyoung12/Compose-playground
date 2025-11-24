plugins {
    id("jyhong.android.library")
    id("jyhong.android.library.compose")
    id("jyhong.android.hilt")
}

android {
    namespace = "com.jyhong.playground.core.chart_engine"
}

dependencies {
    implementation(project(":core:model"))
}
