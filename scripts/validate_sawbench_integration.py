#!/usr/bin/env python3
"""Static integration checks for material-backed sawbench features.

These checks verify source wiring and resource coverage; they are not a substitute
for launching Minecraft and exercising the client/server in-game.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/dev/archcraft"
fabric = (JAVA / "ArchitectureCraftFabric.java").read_text(encoding="utf-8")
bench = (JAVA / "SawbenchBlockEntity.java").read_text(encoding="utf-8")
handler = (JAVA / "SawbenchScreenHandler.java").read_text(encoding="utf-8")
screen = (JAVA / "SawbenchScreen.java").read_text(encoding="utf-8")
shape_entity = (JAVA / "MaterialShapeBlockEntity.java").read_text(encoding="utf-8")
texture_model = (JAVA / "MaterialTextureBakedModel.java").read_text(encoding="utf-8")
client = (JAVA / "ArchitectureCraftClient.java").read_text(encoding="utf-8")

checks = {
    "large shape registry is present": len(re.findall(r'register\("([^"]+)", new (?:ShapeBlock|AxisShapeBlock)\(', fabric)) >= 100,
    "generic custom shapes exist": all(f'register("{name}"' in fabric for name in ("beam_custom", "post_custom", "slab_custom", "pillar_custom")),
    "one selector entry per geometric shape": "getUniqueSawbenchShapes()" in fabric and "canonicalShapeKey(path)" in fabric and "getSawbenchShapeId(selectedMode)" in bench,
    "sawbench accepts arbitrary block-item materials": "stack.getItem() instanceof net.minecraft.item.BlockItem" in bench and 'return "custom";' in bench,
    "source material is written to output item NBT": 'putString("MaterialBlock", materialId.toString())' in bench,
    "source material is restored on placement": "copyMaterialFromStack(world, pos, stack)" in fabric and 'tag.getString("MaterialBlock")' in fabric,
    "material is persisted by the shape block entity": 'nbt.putString("MaterialBlock", materialBlock)' in shape_entity and 'nbt.getString("MaterialBlock")' in shape_entity,
    "visual shape grid is present": "drawItem(new ItemStack(ArchitectureCraftFabric.getSawbenchShapeBlock(index))" in screen and "PAGE_SIZE = COLUMNS * ROWS" in screen and "clickButton(handler.syncId, index)" in screen,
    "selected shape is synced through handler properties": "addProperties(selectedModeProperty)" in handler and "selectedModeProperty.set(0, id)" in handler,
    "changing shape resumes crafting when output is free": "markDirty();\n        craft();" in bench,
    "material texture model is registered": "new MaterialTextureBakedModel(model)" in client,
    "item and world rendering remap UVs": "emitItemQuads" in texture_model and "emitBlockQuads" in texture_model and "quad.sprite(vertex, 0" in texture_model,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(f'{"PASS" if ok else "FAIL"}: {name}')
if failed:
    print(f"Integration source checks failed: {len(failed)}")
    sys.exit(1)
print(f"Integration source checks passed: {len(checks)} checks. Note: in-game runtime behavior is not tested here.")
