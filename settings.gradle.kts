/*
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/7.1.1/userguide/multi_project_builds.html
 */
rootProject.name = "ATLauncher"

include("app")
include("legacy-launch")

// Workaround for using GetText and MacAppBundle gradle plugin in the new `plugins` block instead of the legacy apply plugin
// solution thanks to: https://kotlinlang.slack.com/archives/C19FD9681/p1715859799017779?thread_ts=1715857310.631859&cid=C19FD9681
// for more about GetText: https://github.com/mini2Dx/gettext/issues/14
// The reason why we are using a forked version of GetText because the original doesn't support Gradle 8
pluginManagement {
    resolutionStrategy {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            maven("https://jitpack.io") {
                content {
                    includeGroup("com.github.RyanTheAllmighty.gettext")
                    includeGroup("com.github.ATLauncher.gradle-macappbundle")
                }
            }
        }
        eachPlugin {
            when (requested.id.id) {
                "org.mini2Dx.gettext" ->
                    useModule("com.github.RyanTheAllmighty.gettext:gettext-gradle-plugin:aab5c30bf8")

                "edu.sc.seis.macAppBundle" -> useModule("com.github.ATLauncher.gradle-macappbundle:edu.sc.seis.macAppBundle.gradle.plugin:d22f8cdb94")
            }
        }
    }
}
