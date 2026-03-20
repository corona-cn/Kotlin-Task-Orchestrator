/* === PLUGIN === */
plugins {
    kotlin("jvm")
    `maven-publish`
}


/* === CONFIGURATION === */
kotlin {
    sourceSets {
        all {
            dependencies {
                /* Kotlin & Kotlinx */
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}