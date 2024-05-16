import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    application

    alias(libs.plugins.cadixdev.licenser)
    alias(libs.plugins.test.logger)
    id("edu.sc.seis.macAppBundle")
    alias(libs.plugins.launch4j)
    alias(libs.plugins.undercouch.download)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ben.manes)
    alias(libs.plugins.apollo)
    id("org.mini2Dx.gettext")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "com.atlauncher"
version = rootProject.file("src/main/resources/version").readText().trim().replace(".Beta", "")

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        setUrl("https://libraries.minecraft.net")
    }
    maven {
        setUrl("https://jitpack.io")
        content {
            includeGroup("com.github.Vatuu")
            includeGroup("com.gitlab.doomsdayrs")
            includeGroup("com.github.MCRcortex")
        }
    }
}

dependencies {
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.gson)
    implementation(libs.google.guava)
    implementation(libs.xz)
    implementation(libs.mojang.authlib)
    implementation(libs.base64)
    implementation(libs.discord.rpc)
    implementation(libs.jopt.simple)
    implementation(libs.zt.zip)
    implementation(libs.okhttp)
    implementation(libs.okhttp.tls)
    implementation(libs.sentry)
    implementation(libs.gettext.lib)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.murmur)
    implementation(libs.commons.lang3)
    implementation(libs.commons.text)
    implementation(libs.commons.compress)
    implementation(libs.flatlaf)
    implementation(libs.flatlaf.extras)
    implementation(libs.oshi.core)
    implementation(libs.jlhttp)
    implementation(libs.joda.time)
    implementation(libs.commonmark)
    implementation(libs.dbus.java)
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.http.cache)
    implementation(libs.apollo.rx3.support)
    implementation(libs.nekodetector)

    // RxJava
    implementation(libs.rxjava)
    implementation(libs.rxswing)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.assertj.swing.junit)
    testImplementation(libs.mockserver.netty)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
}

application {
    mainClass = "com.atlauncher.App"
    applicationDefaultJvmArgs = listOf(
        "-Djna.nosys=true",
        "-Djava.net.preferIPv4Stack=true",
        "-Dawt.useSystemAAFontSettings=on",
        "-Dswing.aatext=true"
    )
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()

    testlogger {
        setTheme("mocha")
    }
}

tasks.withType(Jar::class) {
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

configure<com.apollographql.apollo.gradle.api.ApolloExtension> {
    customTypeMapping = mapOf(
        "ID" to "java.lang.String",
        "DateTime" to "java.util.Date"
    )
    @OptIn(com.apollographql.apollo.api.ApolloExperimental::class)
    packageName = "com.atlauncher.graphql"
}

val translations by gettext.registering {
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

configure<org.cadixdev.gradle.licenser.LicenseExtension> {
    setHeader(project.file("LICENSEHEADER"))
    include("'**/*.java'")
    exclude("io/github/**/*.java")
    exclude("net/minecraft/**/*.java")
    exclude("com/atlauncher/graphql/**/*.java")
    exclude("com/atlauncher/gui/layouts/WrapLayout.java")
    newLine = false

    ext.set("year", currentYear())
}


tasks.withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveClassifier.set(null as String?)
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

configure<edu.sc.seis.macAppBundle.MacAppBundlePluginExtension> {
    mainClassName = "com.atlauncher.App"
    appName = "ATLauncher"
    appStyle = "universalJavaApplicationStub"
    runtimeConfigurationName = "shadow"
    jarTask = tasks.shadowJar.name
    icon = "src/main/resources/assets/image/icon.icns"
    javaProperties["user.dir"] = "\$APP_ROOT/Contents/Java"
    javaProperties["apple.laf.useScreenMenuBar"] = "true"
    javaExtras["-Djna.nosys"] = "true"
    javaExtras["-Djava.net.preferIPv4Stack"] = "true"
    javaExtras["-Dawt.useSystemAAFontSettings"] = "on"
    javaExtras["-Dswing.aatext"] = "true"
    bundleExtras["JVMVersion"] = project.java.targetCompatibility.toString() + "+"
}

tasks.named("copyToResourcesJava") {
    doLast {
        val buildDirectory = project.layout.buildDirectory
        val sourceFile = buildDirectory.file("libs/ATLauncher-${project.version}.jar").get().asFile
        val targetFile = project.layout.buildDirectory.file("libs/ATLauncher.jar").get().asFile
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}

fun currentYear(): String? {
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
    jvmOptions = listOf(
        "-Djna.nosys=true",
        "-Djava.net.preferIPv4Stack=true",
        "-Dawt.useSystemAAFontSettings=on",
        "-Dswing.aatext=true"
    )
}

artifacts {
    archives(tasks.shadowJar)

    archives(
        file(
            project.tasks.jar.get().archiveFile.get().asFile.path.replace(".jar", ".exe").replace("libs", "launch4j")
        )
    )
    archives(
        file(
            project.tasks.jar.get().path.replace(".jar", ".zip")
                .replace("libs", "distributions")
        )
    )
}

tasks.withType<Test> {
    if (JavaVersion.current().isJava9Compatible) {
        jvmArgs = listOf("--add-opens", "java.base/sun.security.x509=ALL-UNNAMED")
    }
}

val copyArtifacts = tasks.register("copyArtifacts", Copy::class) {
    dependsOn(tasks.build)

    val jarArchivePath = project.tasks.jar.get().archiveFile.get().asFile.path
    val exeFilePath = jarArchivePath.replace(".jar", ".exe").replace("libs", "launch4j")
    val zipFilePath = jarArchivePath.replace(".jar", ".zip").replace("libs", "distributions")

    from(project.tasks.shadowJar.get().archiveFile)
    from(exeFilePath)
    from(zipFilePath)

    into("$projectDir/dist")
}

val downloadNewerUniversalJavaApplicationStub =
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadNewerUniversalJavaApplicationStub") {
        description = "Downloads newer universalJavaApplicationStub"
        src("https://raw.githubusercontent.com/tofi86/universalJavaApplicationStub/404f5c1b008d6296065de7a93406b387c9f3dce1/src/universalJavaApplicationStub")
        dest(file("$buildDir/macApp/${project.name}.app/Contents/MacOS/universalJavaApplicationStub"))
        overwrite(true)
    }


tasks.register("createTestLauncherDir") {
    project.file("testLauncher/dev").mkdirs()
}

val createMacApp = tasks.register<Zip>("createMacApp") {
    dependsOn(tasks.createApp, tasks.shadowJar, downloadNewerUniversalJavaApplicationStub)
    from("$buildDir/macApp") {
        include("${project.name}.app/**")
        exclude("${project.name}.app/Contents/MacOS")
    }
    archiveFileName = "${project.name}-${project.version}.zip"
}

val printCopyArtifactsFinished = tasks.register("printCopyArtifactsFinished") {
    println("ATLauncher has been built. Distribution files are located in the dist directory.")
}
copyArtifacts.get().finalizedBy(printCopyArtifactsFinished)

tasks.clean {
    doFirst {
        println("Deleting `$projectDir/dist`")
        delete("$projectDir/dist")
    }
}

// TODO: This has error: * What went wrong:
//Could not determine the dependencies of task ':build'.
//> Could not create task ':check'.
//   > Removing a task dependency from a task instance is not supported.

//project.afterEvaluate {
//    tasks.check {
//        dependsOn -= tasks.find { it.name == tasks.checkLicenses.name }
//    }
//}

val shouldIgnoreUpdate: (String) -> Boolean = { version ->
    listOf("ALPHA", "BETA", "RC", "-M").any { version.uppercase().contains(it) }
}

tasks.dependencyUpdates.configure {
    rejectVersionIf {
        shouldIgnoreUpdate(candidate.version)
    }
}

tasks.build.get().finalizedBy(copyArtifacts)
tasks.build.get().finalizedBy(copyArtifacts)
tasks.shadowJar.get().dependsOn(tasks.jar)
tasks.build.get().dependsOn(tasks.createExe, createMacApp)
tasks.startScripts.get().dependsOn(tasks.shadowJar)
tasks.createExe.get().dependsOn(tasks.shadowJar)
tasks.createAppZip.get().dependsOn(downloadNewerUniversalJavaApplicationStub)
tasks.createDmg.get().dependsOn(downloadNewerUniversalJavaApplicationStub)
