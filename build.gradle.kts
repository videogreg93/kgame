plugins {
   kotlin("jvm") version "1.7.20"
   application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.kweb:kweb-core:1.1.2.1")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.google.code.gson:gson:2.10")
}
