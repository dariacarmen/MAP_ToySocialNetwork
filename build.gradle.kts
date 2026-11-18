plugins {
    java
    application
}

group = "socialnetwork"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // driverul JDBC pentru PostgreSQL (cerinta 3 din laboratorul 5)
    implementation("org.postgresql:postgresql:42.7.7")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

application {
    mainClass.set("socialnetwork.Main")
}

// aplicatia citeste comenzi de la tastatura si foloseste cai relative
// la folderul proiectului (config.properties, data/)
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    workingDir = rootDir
}