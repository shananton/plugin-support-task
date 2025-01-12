plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(rootProject)
    testImplementation(rootProject.sourceSets.test.get().output)
    testImplementation(rootProject)
//    compileTestJava.dependsOn tasks.getByPath(":plugin-support-hw:testClasses")
    implementation("javazoom:jlayer:1.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("com.googlecode.soundlibs:tritonus-share:0.3.7-2")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5-1")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3-1")
}