plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra
val soul_fire_d_version: String by extra
val minecraft_min_version: String by extra
val flywheel_version: String by extra
val codecui_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")
    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    // Mirror of common deps (new setup requires every common modImplementation/modCompileOnly to live here too)
    modImplementation("curse.maven:supplementaries-412082:8044262")
    //modCompileOnly("com.jozufozu.flywheel:flywheel-forge-${flywheel_version}")
    modCompileOnly("curse.maven:flan-404578:5290167")
    modCompileOnly("curse.maven:farmers-delight-398521:8083481")
    modCompileOnly("curse.maven:etched-491890:5998004")
    modCompileOnly("curse.maven:new-thin-air-878379:5068247")
    modCompileOnly("curse.maven:quark-243121:5093415")
    modCompileOnly("curse.maven:cave-enhancements-597562:4388535")
    modCompileOnly("curse.maven:soul-fire-d-662413:6248772")
    modCompileOnly("curse.maven:modmenu-308702:7808443")
    modCompileOnly("curse.maven:modmenu-308702:7808443")

}
