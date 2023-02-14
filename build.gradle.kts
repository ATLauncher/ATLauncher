import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import org.mini2Dx.gettext.plugin.GetTextSource
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.mini2Dx:gettext-gradle-plugin:1.11.0")
    }
}

plugins {
    java
    application

    id("org.cadixdev.licenser") version "0.6.1"
    id("com.adarshr.test-logger") version "3.2.0"
    id("edu.sc.seis.macAppBundle") version "2.3.0"
    id("edu.sc.seis.launch4j") version "2.5.3"
    id("de.undercouch.download") version "5.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.ben-manes.versions") version "0.45.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("com.apollographql.apollo") version "2.5.14"
}

apply(plugin = "org.mini2Dx.gettext")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "com.atlauncher"
version = rootProject.file("src/main/resources/version").readText().trim().replace(".Beta", "")

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://libraries.minecraft.net")
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.Vatuu")
        }
    }
}

dependencies {
    implementation("net.java.dev.jna:jna:5.11.0")
    implementation("net.java.dev.jna:jna-platform:5.11.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.tukaani:xz:1.9")
    implementation("com.mojang:authlib:1.5.21")
    implementation("net.iharder:base64:2.3.9")
    implementation("com.github.Vatuu:discord-rpc:1.6.2")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.zeroturnaround:zt-zip:1.15")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:okhttp-tls:4.9.3")
    implementation("net.mikehardy:google-analytics-java:2.0.11")
    implementation("io.sentry:sentry:6.1.4")
    implementation("org.mini2Dx:gettext-lib:1.11.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("com.sangupta:murmur:1.0.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.formdev:flatlaf:2.3")
    implementation("com.formdev:flatlaf-extras:2.3")
    implementation("com.github.oshi:oshi-core:6.1.6")
    implementation("net.freeutils:jlhttp:2.6")
    implementation("joda-time:joda-time:2.10.14")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.commonmark:commonmark:0.19.0")
    implementation("com.github.hypfvieh:dbus-java:3.3.1")
    implementation("com.apollographql.apollo:apollo-runtime:2.5.14")
    implementation("com.apollographql.apollo:apollo-http-cache:2.5.14")
    implementation("com.apollographql.apollo:apollo-coroutines-support:2.5.14")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("org.mockito:mockito-inline:4.6.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.assertj:assertj-swing-junit:3.17.1")
    testImplementation("org.mock-server:mockserver-netty:5.13.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

application {
    mainClass.set("com.atlauncher.App")
    applicationDefaultJvmArgs = listOf(
        "-Djna.nosys=true",
        "-Djava.net.preferIPv4Stack=true",
        "-Dawt.useSystemAAFontSettings=on",
        "-Dswing.aatext=true"
    )
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()

    testlogger {
        setTheme("mocha")
    }
}

tasks.jar {
    manifest {
        attributes(
            "SplashScreen-Image" to "/assets/image/splash-screen.png",
            "Implementation-Title" to project.name,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vender" to "ATLauncher",
            "Main-Class" to "com.atlauncher.App",
            "Multi-Release" to "true"
        )
    }
}

apollo {
    customTypeMapping.set(
        mapOf(
            "ID" to "java.lang.String",
            "DateTime" to "java.util.Date"
        )
    )
    @Suppress("OPT_IN_USAGE") // It's experimental, and that is fine
    packageName.set("com.atlauncher.graphql")
}

extensions.configure<FactoryNamedDomainObjectContainer<GetTextSource>>("gettext") {
    create("translations") {
        srcDir = "src"
        include = "main/java/com/atlauncher/**/*.java"
        excludes = arrayOf(
            "main/java/com/atlauncher/adapter/**/*.java",
            "main/java/com/atlauncher/annot/**/*.java",
            "main/java/com/atlauncher/collection/**/*.java",
            "main/java/com/atlauncher/evnt/**/*.java",
            "main/java/com/atlauncher/exceptions/**/*.java",
            "main/java/com/atlauncher/interfaces/**/*.java",
            "main/java/com/atlauncher/listener/**/*.java",
            "main/java/com/atlauncher/utils/**/*.java"
        )
        commentFormat = " #. "
        outputFilename = "translations.pot"
    }
}

license {
    header(project.file("LICENSEHEADER"))
    include("**/*.java")
    exclude("io/github/**/*.java")
    exclude("net/minecraft/**/*.java")
    exclude("com/atlauncher/graphql/**/*.java")
    exclude("com/atlauncher/gui/layouts/WrapLayout.java")
    newLine.set(false)
    properties {
        set("year", currentYear())
    }
}


tasks.shadowJar {
    archiveClassifier.set(null as String?) // type problem shenanigans
    minimize {
        exclude(dependency("org.apache.logging.log4j:.*:.*"))
        exclude(dependency("com.formdev:.*:.*"))
        exclude(dependency("com.github.jnr:.*:.*"))
        exclude(dependency("com.github.hypfvieh:.*:.*"))
        exclude(dependency("org.apache.commons:commons-compress:.*"))
    }

    // these are included by dbus-java which is only used on Linux
    exclude("jni/x86_64-Windows/")
    exclude("jni/x86_64-SunOS/")
    exclude("jni/x86_64-OpenBSD/")
    exclude("jni/x86_64-FreeBSD/")
    exclude("jni/x86_64-DragonFlyBSD/")
    exclude("jni/sparcv9-SunOS/")
    exclude("jni/ppc-AIX/")
    exclude("jni/ppc64-AIX/")
    exclude("jni/i386-Windows/")
    exclude("jni/i386-SunOS/")
    exclude("jni/Darwin/")

    archiveClassifier.set("")
}

macAppBundle {
    mainClassName = "com.atlauncher.App"
    appName = "ATLauncher"
    appStyle = "universalJavaApplicationStub"
    runtimeConfigurationName = "shadow"
    jarTask = "shadowJar"
    icon = "src/main/resources/assets/image/icon.icns"
    javaProperties["user.dir"] = "\$APP_ROOT/Contents/Java"
    javaProperties["apple.laf.useScreenMenuBar"] = "true"
    javaExtras["-Djna.nosys"] = "true"
    javaExtras["-Djava.net.preferIPv4Stack"] = "true"
    javaExtras["-Dawt.useSystemAAFontSettings"] = "on"
    javaExtras["-Dswing.aatext"] = "true"
    bundleExtras["JVMVersion"] = project.java.targetCompatibility.toString() + "+"
}

tasks.copyToResourcesJava {
    rename("ATLauncher-${project.version}.jar", "ATLauncher.jar")
}

fun currentYear(): String {
    val df = SimpleDateFormat("yyyy")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

launch4j {
    outfile = "ATLauncher-${project.version}.exe"
    jreMinVersion = project.java.targetCompatibility.toString()
    mainClassName = "com.atlauncher.App"
    icon = "${projectDir}/src/main/resources/assets/image/icon.ico"
    version = "${project.version}"
    textVersion = "${project.version}"
    copyright = "2013-${currentYear()} ${project.name}"
    companyName = project.name
    bundledJrePath = "jre/;%JAVA_HOME%;%PATH%"
    jvmOptions = setOf(
        "-Djna.nosys=true",
        "-Djava.net.preferIPv4Stack=true",
        "-Dawt.useSystemAAFontSettings=on",
        "-Dswing.aatext=true"
    )
}

fun getCopyPath(newSuffix: String, newLibs: String): File {
    val archivePath = project.tasks.jar.get().archiveFile.get().asFile.path
    return file(archivePath.replace(".jar", newSuffix).replace("libs", newLibs))
}

artifacts {
    archives(tasks.shadowJar)
    archives(getCopyPath(".exe", "launch4j"))
    archives(getCopyPath(".zip", "distributions"))
}

tasks.test {
    if (JavaVersion.current().isJava9Compatible) {
        jvmArgs("--add-opens=java.base/sun.security.x509=ALL-UNNAMED")
    }
}

tasks.register<Copy>("copyArtifacts") {
    dependsOn(tasks.build)
    from(tasks.shadowJar)
    from(getCopyPath(".exe", "launch4j"))
    from(getCopyPath(".zip", "distributions"))
    into("${projectDir}/dist")
}

tasks.register<Download>("downloadNewerUniversalJavaApplicationStub") {
    description = "Downloads newer universalJavaApplicationStub"
    src("https://raw.githubusercontent.com/tofi86/universalJavaApplicationStub/2dbbf92b35e61194266c985c8bc6b411053a1b4a/src/universalJavaApplicationStub")
    dest(file("$buildDir/macApp/${project.name}.app/Contents/MacOS/universalJavaApplicationStub"))
    overwrite(true)
}

tasks.register("createTestLauncherDir") {
    project.file("testLauncher/dev").mkdirs()
}

tasks.register<Zip>("createMacApp") {
    dependsOn(tasks.named("createApp"), tasks.shadowJar)
    from("$buildDir/macApp") {
        include("${project.name}.app/**")
        exclude("${project.name}.app/Contents/MacOS")
    }
    from("$buildDir/macApp") {
        include("${project.name}.app/Contents/MacOS/**")
        fileMode = 0x777
    }
    archiveFileName.set("${project.name}-${project.version}.zip")
}

tasks.create("copyArtifactsFinal") {
    doFirst {
        println("ATLauncher has been built. Distribution files are located in the dist directory.")
    }
}

tasks.named("copyArtifacts") {
    finalizedBy("copyArtifactsFinal")
}

tasks.clean {
    doFirst {
        delete("${projectDir}/dist")
    }
}

project.afterEvaluate {
    tasks.check {
        /*
        Trick gradle by not "removing" a task
         (which throws an unsupported error),
         Instead filter current tasks and then set the new list.
        */
        setDependsOn(
            dependsOn.filterNot {
                (it as? Task)?.name == "checkLicenses"
            }
        )
    }
}

fun getShouldIgnoreUpdate(version: String): Boolean {
    return arrayOf("ALPHA", "BETA", "RC", "-M").any { version.toUpperCase() == (it) }
}

tasks.dependencyUpdates {
    rejectVersionIf {
        getShouldIgnoreUpdate(candidate.version)
    }
}

tasks {
    build {
        finalizedBy("copyArtifacts")
        dependsOn(createExe, project.tasks.named("createMacApp"))
    }
    shadowJar {
        dependsOn(project.tasks.jar)
    }
    createApp {
        finalizedBy("downloadNewerUniversalJavaApplicationStub")
    }
}