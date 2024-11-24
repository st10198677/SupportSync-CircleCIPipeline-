// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()


    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // Gradle build tools
        classpath("com.google.gms:google-services:4.3.15") // Google services classpath for Firebase


    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}


