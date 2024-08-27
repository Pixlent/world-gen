plugins {
    id("java")
}

group = "me.pixlent"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:a521c4e7cd")
    implementation("de.articdive:jnoise-pipeline:4.1.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("org.slf4j:slf4j-simple:2.0.16")
}