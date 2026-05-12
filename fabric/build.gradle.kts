import org.gradle.kotlin.dsl.modCompileOnly

plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

loom {
}

val moonlight_version: String by extra
val soul_fire_d_version: String by extra
val minecraft_min_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    modCompileOnly("com.terraformersmc:modmenu:4.0.6")

    modCompileOnly("curse.maven:supplementaries-412082:5147147")
    modCompileOnly("curse.maven:farmers-delight-refabricated-993166:5215068")
    modCompileOnly("curse.maven:spelunkery-790530:5043881")
    modCompileOnly("it.crystalnest:soul-fire-d-common:${minecraft_min_version}-${soul_fire_d_version}")
}
