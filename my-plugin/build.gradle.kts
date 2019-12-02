plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(rootProject)
//    implementation("plugin-support-hw.test")
    implementation("javazoom:jlayer:1.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("com.googlecode.soundlibs:tritonus-share:0.3.7-2")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5-1")
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3-1")
}