plugins {
    id("jyhong.android.library")
    id("jyhong.android.library.compose")
    id("jyhong.android.hilt")
}

android {
    namespace = "com.jyhong.playground.feature.line_chart"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
}
