Optional custom models for the dangling lantern body in fancy (animated) and fast wall lantern rendering.

Legacy path: models/block/custom_wall_lanterns/[mod_id]/[lantern_name].json

New per-type override path:
- models/block/[wall_lantern_id]_lantern.json — custom dangling lantern model only

Each wall lantern block has its own blockstate/models for the wall bracket/support.
Override those directly via the dynamically registered wall lantern block id
(e.g. blockstates/wall_soul_lantern.json, models/block/wall_soul_lantern*.json).

Support bracket textures are auto-generated when the source lantern texture matches vanilla lantern layout.
Manual support textures can be placed at textures/block/wall_lanterns/[mod_id]/[lantern_name].png
