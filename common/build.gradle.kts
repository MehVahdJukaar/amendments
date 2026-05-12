plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val moonlight_version: String by extra
val supplementaries_version: String by extra
val flywheel_version: String by extra
val soul_fire_d_version: String by extra
val fabric_loader_version: String by extra
val mixin_extras_version: String by extra

dependencies {

    modApi("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modImplementation("curse.maven:supplementaries-412082:8044262")

    modCompileOnly("com.jozufozu.flywheel:flywheel-forge-${flywheel_version}")
    modCompileOnly("curse.maven:flan-404578:5290167")
    modCompileOnly("curse.maven:farmers-delight-398521:5051242")
    modCompileOnly("curse.maven:etched-491890:5998004")
    modCompileOnly("com.lowdragmc.shimmer:Shimmer-common:1.19.2-0.1.14")
    modCompileOnly("curse.maven:new-thin-air-878379:5068247")
    modCompileOnly("curse.maven:quark-243121:5093415")
    modCompileOnly("curse.maven:cave-enhancements-597562:4388535")
    modCompileOnly("curse.maven:soul-fire-d-662413:6248772")
}
