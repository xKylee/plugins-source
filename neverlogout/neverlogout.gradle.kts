import ProjectVersions.rlVersion


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
}

version = "0.0.1"

project.extra["PluginName"] = "Never Logout"
project.extra["PluginDescription"] = "Remove 5 minute idle logout timer."
project.extra["PluginLicense"] = "GNU GPLv3 https://www.gnu.org/licenses/gpl-3.0.md"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)

    implementation("com.openosrs:runelite-api:$rlVersion")
    implementation("com.openosrs:runelite-client:$rlVersion")

    implementation(Libraries.guice)
    implementation(Libraries.lombok)
    implementation(Libraries.pf4j)

    implementation(kotlin("stdlib-jdk8"))

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
