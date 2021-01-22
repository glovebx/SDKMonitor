buildscript {

    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-beta03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
