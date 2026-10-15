#!/usr/bin/env python3
"""Validate the mod's registered block resources before building."""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/dev/archcraft/ArchitectureCraftFabric.java"
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/archcraft"
DATA = RES / "data/archcraft"

source = JAVA.read_text(encoding="utf-8")
blocks = set(re.findall(r'register\("([^"]+)"\s*,', source))
errors = []

if not blocks:
    errors.append("No registered block IDs found in ArchitectureCraftFabric.java")

for file in RES.rglob("*.json"):
    try:
        json.loads(file.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        errors.append(f"Invalid JSON {file.relative_to(ROOT)}: {exc}")

lang_path = ASSETS / "lang/en_us.json"
try:
    lang = json.loads(lang_path.read_text(encoding="utf-8"))
except (OSError, json.JSONDecodeError) as exc:
    lang = {}
    errors.append(f"Cannot read English translations: {exc}")

for block in sorted(blocks):
    required = [
        ASSETS / f"blockstates/{block}.json",
        ASSETS / f"models/block/{block}.json",
        ASSETS / f"models/item/{block}.json",
        DATA / f"loot_tables/blocks/{block}.json",
    ]
    for path in required:
        if not path.is_file():
            errors.append(f"Missing resource for {block}: {path.relative_to(ROOT)}")
    if f"block.archcraft.{block}" not in lang:
        errors.append(f"Missing English translation key: block.archcraft.{block}")

recipes = DATA / "recipes"
for path in sorted(recipes.glob("*.json")):
    try:
        recipe = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        continue
    result = recipe.get("result", {})
    item = result.get("item") or result.get("id")
    if item and item.startswith("archcraft:") and item.split(":", 1)[1] not in blocks:
        errors.append(f"Recipe {path.name} outputs unregistered item: {item}")

if errors:
    print("Resource validation failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)

print(f"Resource validation passed: {len(blocks)} registered blocks; JSON, models, loot tables, recipes, and English names checked.")
