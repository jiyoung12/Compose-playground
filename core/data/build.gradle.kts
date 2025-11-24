plugins {
    id("jyhong.android.library")
    id("jyhong.android.hilt")
}

android {
    namespace = "com.jyhong.playground.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
}
