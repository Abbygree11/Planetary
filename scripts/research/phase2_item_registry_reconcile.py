#!/usr/bin/env python3
"""Reconcile the immutable NeoForge 21.1.215 Phase-2 registry census.

USAGE: python phase2_item_registry_reconcile.py ORIGINAL_ARTIFACT_ZIP OUTPUT_DIR
Only Python stdlib is required. The generated TSVs are *registered compiled*
owner evidence; they do not claim a passed gameplay/ASM source review.
"""
import csv
import hashlib
import io
import sys
import zipfile
from collections import Counter, defaultdict
from pathlib import Path

EXPECTED_ZIP_SHA = "7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e"
METHODS = ("use", "useOn", "place", "updatePlacementContext", "getPlacementState", "placeBlock", "canPlace", "registerBlocks")
SCHEMA = "phase2-neo1211-item-registry.tsv"


def read_tsv(archive, name):
    return list(csv.DictReader(io.StringIO(archive.read(name).decode("utf-8")), delimiter="\t"))


def extract_owners(text):
    result = {}
    for part in text.split("|"):
        if "=" in part:
            key, signature = part.split("=", 1)
            assert key not in result, (key, text)
            result[key] = signature
    return result


def write_tsv(path, fieldnames, rows):
    with open(path, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, delimiter="\t", fieldnames=fieldnames, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: phase2_item_registry_reconcile.py artifact.zip output-dir")
    zip_path, output = Path(sys.argv[1]), Path(sys.argv[2])
    actual_sha = hashlib.sha256(zip_path.read_bytes()).hexdigest()
    assert actual_sha == EXPECTED_ZIP_SHA, f"Wrong original artifact digest: {actual_sha}"
    with zipfile.ZipFile(zip_path) as archive:
        items = read_tsv(archive, SCHEMA)
        blocks = {r["registry_id"]: r for r in read_tsv(archive, "phase2-neo1211-block-registry.tsv")}
        properties = defaultdict(list)
        for record in read_tsv(archive, "phase2-neo1211-state-properties.tsv"):
            properties[record["registry_id"]].append(record)
    assert len(items) == len({r["registry_id"] for r in items}) == 1333
    assert len(blocks) == 1060
    assert sum(len(x) for x in properties.values()) == 1712
    assert len({r["java_class"] for r in items}) == 87
    assert sum(r["block_item"] == "true" for r in items) == 925
    assert all((r["placed_block"] in blocks) == (r["block_item"] == "true") for r in items)
    assert all(r["placed_block"] == "-" for r in items if r["block_item"] == "false")

    output.mkdir(parents=True, exist_ok=True)
    distinct_classes = defaultdict(list)
    full = []
    divergent = []
    for r in sorted(items, key=lambda x: x["registry_id"]):
        java_class, rid = r["java_class"], r["registry_id"]
        distinct_classes[java_class].append(r)
        owners = extract_owners(r["effective_method_owners"])
        assert "use" in owners and "useOn" in owners, rid
        assert all(m in owners for m in METHODS[2:]) == (r["block_item"] == "true"), rid
        placed = r["placed_block"]
        classed = r["block_item"] == "true"
        if classed and rid != placed:
            divergent.append({"registry_id": rid, "placed_block": placed, "java_class": java_class})
        rec = {
            "registry_id": rid, "java_class": java_class,
            "block_item": r["block_item"], "placed_block": placed,
            "class_hierarchy": r["class_hierarchy"],
            "target_block_class": blocks[placed]["java_class"] if classed else "-",
            "target_block_properties": blocks[placed]["declared_property_names"] if classed else "-",
            **{f"owner_{method}": owners.get(method, "-") for method in METHODS},
            "all_declaring_owners": r["all_declaring_owners"],
            "method_owner_evidence": "EXACT_ORIGINAL_REGISTRY_EFFECTIVE_DECLARING_OWNER",
            "semantic_and_applied_21_1_215_asm": "REVIEW_PENDING",
            "planet_gameplay": "NOT_RUN"
        }
        full.append(rec)
    assert len(full) == 1333 and len(divergent) == 14
    assert set(x["registry_id"] for x in divergent) == {
        "minecraft:beetroot_seeds", "minecraft:carrot", "minecraft:cocoa_beans", "minecraft:glow_berries",
        "minecraft:melon_seeds", "minecraft:pitcher_pod", "minecraft:potato", "minecraft:powder_snow_bucket",
        "minecraft:pumpkin_seeds", "minecraft:redstone", "minecraft:string", "minecraft:sweet_berries",
        "minecraft:torchflower_seeds", "minecraft:wheat_seeds"
    }
    assert {r["registry_id"] for r in distinct_classes["net.minecraft.world.item.Items$1"]} == {"minecraft:big_dripleaf", "minecraft:cauldron"}
    assert len(distinct_classes) == 87
    per_class = []
    for java_class, group in sorted(distinct_classes.items()):
        kinds = {(r["class_hierarchy"], r["effective_method_owners"], r["all_declaring_owners"]) for r in group}
        assert len(kinds) == 1, f"Mixed effective owners inside registered class {java_class}: {len(kinds)}"
        owners = extract_owners(group[0]["effective_method_owners"])
        ids = sorted(r["registry_id"] for r in group)
        cls = {
            "java_class": java_class,
            "registered_ids": len(group),
            "block_item_ids": sum(x["block_item"] == "true" for x in group),
            "non_block_item_ids": sum(x["block_item"] == "false" for x in group),
            "class_hierarchy": group[0]["class_hierarchy"],
            **{f"owner_{method}": owners.get(method, "-") for method in METHODS},
            "ids_sha256": hashlib.sha256(("\n".join(ids) + "\n").encode()).hexdigest(),
            "example_registry_id": ids[0],
            "original_compiled_owners": "VERIFIED",
            "target_21_1_215_asm": "REVIEW_PENDING",
            "planet_gameplay": "NOT_RUN"
        }
        per_class.append(cls)
    assert sum(r["registered_ids"] for r in per_class) == 1333
    assert sum(r["block_item_ids"] for r in per_class) == 925
    assert sum(r["non_block_item_ids"] for r in per_class) == 408
    for r in divergent:
        assert r["placed_block"] in blocks

    f1 = output / "phase2-item-1333-exact-owner-reconciliation.tsv"
    f2 = output / "phase2-item-87-concrete-class-owner-reconciliation.tsv"
    f3 = output / "phase2-item-14-nonidentical-block-linkage.tsv"
    digest1 = write_tsv(f1, list(full[0].keys()), full)
    digest2 = write_tsv(f2, list(per_class[0].keys()), per_class)
    digest3 = write_tsv(f3, list(divergent[0].keys()), divergent)
    print("ORIGINAL_ARTIFACT_SHA256", actual_sha)
    for name, count, digest in [(f1.name, 1333, digest1), (f2.name, 87, digest2), (f3.name, 14, digest3)]:
        print(name, "rows=", count, "bytes=", (output / name).stat().st_size, "sha256=", digest)
    print("TOTAL registered ITEM=1333 concreteJavaClasses=87 BlockItems=925 nonBlockItems=408 id!=placedBlock=14")


if __name__ == "__main__":
    main()
