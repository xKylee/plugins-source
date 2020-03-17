import ProjectVersions.rlVersion


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
}

version = "0.0.2"

project.extra["PluginName"] = "Never Logout"
project.extra["PluginDescription"] = "Remove 5 minute idle logout timer."

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    kapt(Libraries.pf4j)

    compileOnly("com.openosrs:runelite-api:$rlVersion")
    compileOnly("com.openosrs:runelite-client:$rlVersion")

    compileOnly(Libraries.guice)
    compileOnly(Libraries.pf4j)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjvm-default=enable")
        }
        sourceCompatibility = "11"
    }

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
