dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml")) // 루트의 버전 카탈로그 공유
        }
    }
}

rootProject.name = "build-logic"
include(":convention")