#!/usr/bin/env python3
"""Phase 3R-2c: independently verify both actual original NeoForge 21.1.215
registry research archives against committed 241-class SIX-API owner atlas.
No gameplay/Mixin/prod-code acceptance implied.
Usage: python scripts/research/phase3_shape_six_owner_verify.py \\
    phase2-registry-original.zip phase3-r2c-neo1211.zip \\
    docs/research/PHASE3_R2C_ORIGINAL_SIX_SHAPE_OWNERS_1_21_1.tsv
"""
import csv
import hashlib
import io
import sys
import zipfile
from pathlib import Path

from phase3_shape_effective_owner_verify import source_rows, CANONICAL_SHA, canonical, fnv64

ARTIFACT_TSV_SHA256 = "6b789f089b97cdb20c9b11ebc6d2eeca80e4f73076673d807cb29651946524fa"
FOUR_OWNER_SHA256 = "5c3d8692963daa32bc4394811a744e395801892f8bf753c5223992be956a12a9"
FOUR_OWNER_FNV = 0xCA50E69A59206014
FOUR = ["getVisualShape", "getInteractionShape", "getOcclusionShape", "getBlockSupportShape"]
PARAMS = {
    "getVisualShape": "net.minecraft.world.level.block.state.BlockState,"
                      "net.minecraft.world.level.BlockGetter,"
                      "net.minecraft.core.BlockPos,"
                      "net.minecraft.world.phys.shapes.CollisionContext",
    "getInteractionShape": "net.minecraft.world.level.block.state.BlockState,"
                           "net.minecraft.world.level.BlockGetter,"
                           "net.minecraft.core.BlockPos",
    "getOcclusionShape": "net.minecraft.world.level.block.state.BlockState,"
                         "net.minecraft.world.level.BlockGetter,"
                         "net.minecraft.core.BlockPos",
    "getBlockSupportShape": "net.minecraft.world.level.block.state.BlockState,"
                            "net.minecraft.world.level.BlockGetter,"
                            "net.minecraft.core.BlockPos",
}


def four_archive(path: Path):
    with zipfile.ZipFile(path) as zipfile_obj:
        data = zipfile_obj.read("phase3-neo1211-four-shape-effective-owners.tsv")
    assert hashlib.sha256(data).hexdigest() == ARTIFACT_TSV_SHA256
    rows = list(csv.DictReader(io.StringIO(data.decode("utf-8")), delimiter="\t"))
    assert len(rows) == 241
    assert len({r["java_class"] for r in rows}) == 241
    assert sum(int(r["registered_block_ids"]) for r in rows) == 1060
    for row in rows:
        signatures = row["original_compiled_method_signatures"].split(";")
        assert signatures == [name + "(" + PARAMS[name] + ")" for name in FOUR], row
        for name in FOUR:
            owner = row[name + "_exact_owner"].rsplit(".", 1)[-1]
            assert owner in row["original_exact_hierarchy"].split(">"), row
    return {r["java_class"]: r for r in rows}


def normalize(four_rows):
    data = "".join(
        "|".join([r["java_class"], r["registered_block_ids"],
                  r["original_exact_hierarchy"],
                  *(r[m + "_exact_owner"].rsplit(".", 1)[-1] for m in FOUR)]) + "\n"
        for r in sorted(four_rows.values(), key=lambda item: item["java_class"])
    ).encode()
    assert hashlib.sha256(data).hexdigest() == FOUR_OWNER_SHA256
    assert fnv64(data) == FOUR_OWNER_FNV


def main():
    if len(sys.argv) != 4:
        raise SystemExit("Usage: verifier.py original_phase2.zip new_phase3.zip SIX_API.tsv")
    previous = source_rows(Path(sys.argv[1]))
    assert hashlib.sha256(canonical(previous)).hexdigest() == CANONICAL_SHA
    four = four_archive(Path(sys.argv[2]))
    normalize(four)
    with Path(sys.argv[3]).open(encoding="utf-8", newline="") as f:
        atlas = list(csv.DictReader(f, delimiter="\t"))
    assert len(atlas) == 241
    assert len({r["java_class"] for r in atlas}) == 241
    for row in atlas:
        name = row["java_class"]
        assert name in previous and name in four, name
        count, hierarchy, shape, collision = previous[name]
        assert row["registered_block_ids"] == str(count), name
        assert row["original_exact_class_hierarchy"] == hierarchy, name
        assert row["getShape_original_jvm_owner"] == shape, name
        assert row["getCollisionShape_original_jvm_owner"] == collision, name
        r = four[name]
        assert int(r["registered_block_ids"]) == count, name
        assert r["original_exact_hierarchy"] == hierarchy, name
        for method in FOUR:
            owner = r[method + "_exact_owner"].rsplit(".", 1)[-1]
            assert row[method + "_original_jvm_owner"] == owner, (name, method)
        assert row["original_jvm_proof_getShape_getCollisionShape"] == (
            "ORIGINAL_21_1_215_JVM_REFLECTION_ARCHIVE_11643813158")
        assert row["original_jvm_proof_four_other_shape_apis"] == (
            "ORIGINAL_21_1_215_JVM_REFLECTION_CI_40136919659")
    print("PASS 241/241 original class keys, 1060/1060 registered IDs")
    print("PASS 6 exact original JVM effective shape owners per class (source archive+CI)")
    print("PASS four-owner SHA256", FOUR_OWNER_SHA256, "FNV64", hex(FOUR_OWNER_FNV))
    print("OPEN state API overloads/NeoForge callsites, modded dynamic getter and gameplay")


if __name__ == "__main__":
    main()
