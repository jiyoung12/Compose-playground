plugins {
    id("jyhong.android.library")
    id("jyhong.android.hilt")
}

android {
    namespace = "com.jyhong.playground.core.domain"
}

dependencies {
    implementation(project(":core:model"))
}
