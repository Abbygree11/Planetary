#!/usr/bin/env python3
"""Independent 3R-4C original NeoForge21.1.215 compiled BER archive verifier.

Usage:
    python scripts/research/phase3_r4c_original_ber_verify.py <Actions_artifact.zip>

Verifies pinned GitHub Actions archive, all 18 factory classfiles, five member
hashes, original method/call/opcode counts and 16 nested-call invariants.
Not a loaded client, mod factory override or six-face game acceptance test.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "f655f4d74b16accd2f0ac230a4837b3f20562137d09addceaa0f4cb39d142633"
PREFIX = "phase3-r4c-remaining-ber-"
MEMBERS = {
    "owners.tsv": "769fe9f393a43b1585484608be96710c616565b3c7b2da8df879488979d840ce",
    "methods.tsv": "c18d5dd3e1575f5373bee08eb43529af19eac7580e0ac647afc7c037515a3bb2",
    "invokes.tsv": "38de3b8185beef696e567a571ad0a103fba0b6fb25345b4524ce1e94ed3ac13e",
    "instructions.tsv": "8ccb81e675ae205dddd80b44a834dc0887a016c46e4b4796b369fd5948f79dcd",
    "summary.txt": "3847d961a0b756a12a0839b3a72bf2146f3117e6801e83d6c0f4e9e64b6429f2",
}
EXPECTED_OWNERS = {
    "SpawnerRenderer", "PistonHeadRenderer", "ChestRenderer",
    "EnchantTableRenderer", "LecternRenderer", "TheEndPortalRenderer",
    "TheEndGatewayRenderer", "BeaconRenderer", "StructureBlockRenderer",
    "ShulkerBoxRenderer", "BedRenderer", "ConduitRenderer", "BellRenderer",
    "CampfireRenderer", "BrushableBlockRenderer", "DecoratedPotRenderer",
    "TrialSpawnerRenderer", "VaultRenderer",
}
ROOT = "net/minecraft/client/renderer/blockentity/"

def sha256(raw):
    return hashlib.sha256(raw).hexdigest()

def verify(path):
    with open(path, "rb") as stream:
        actual_zip_sha = sha256(stream.read())
    assert actual_zip_sha == ZIP_SHA, (actual_zip_sha, ZIP_SHA)

    with zipfile.ZipFile(path) as zf:
        text = {}
        for suffix, expected_sha in MEMBERS.items():
            matching = [n for n in zf.namelist()
                        if n.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(matching) == 1, (suffix, matching)
            raw = zf.read(matching[0])
            assert sha256(raw) == expected_sha, (suffix, sha256(raw), expected_sha)
            text[suffix] = raw.decode("utf-8")

    summary = dict(row.split("=", 1) for row in text["summary.txt"].splitlines()
                   if "=" in row)
    for key, expected in {
        "target": "Minecraft1.21.1-NeoForge21.1.215", "stage": "3R-4C",
        "owners": "18", "methods": "133", "invokes": "899",
        "tracedInstructions": "2620",
        "scope": "original_classfile_static_JVM_not_modded_client",
    }.items():
        assert summary.get(key) == expected, (key, summary.get(key), expected)

    tables = {key: list(csv.DictReader(io.StringIO(text[key]), delimiter="\t"))
              for key in ("owners.tsv", "methods.tsv", "invokes.tsv",
                          "instructions.tsv")}
    assert len(tables["owners.tsv"]) == 18
    assert len(tables["methods.tsv"]) == 133
    assert len(tables["invokes.tsv"]) == 899
    assert len(tables["instructions.tsv"]) == 2620
    originals = {row["owner"] for row in tables["owners.tsv"]}
    assert originals == {ROOT + name for name in EXPECTED_OWNERS}
    assert all(len(row["class_sha256"]) == 64
               for row in tables["owners.tsv"])
    assert all(any(row["owner"] == ROOT + name and row["method"] == "render"
                   for row in tables["methods.tsv"])
               for name in EXPECTED_OWNERS)
    assert any(row["owner"] == ROOT + "TheEndGatewayRenderer"
               and row["superclass"] == ROOT + "TheEndPortalRenderer"
               for row in tables["owners.tsv"])

    calls = tables["invokes.tsv"]
    # Count only exact original direct source-owner and target-owner paths.
    checks = [
        ("BedRenderer", "render", "BedRenderer", "renderPiece", 3),
        ("BedRenderer", "renderPiece", "PoseStack", "pushPose", 1),
        ("BedRenderer", "renderPiece", "PoseStack", "popPose", 1),
        ("PistonHeadRenderer", "render", "PistonMovingBlockEntity", "getMovedState", 1),
        ("ChestRenderer", "render", "PoseStack", "pushPose", 1),
        ("TheEndGatewayRenderer", "render", "BeaconRenderer", "renderBeaconBeam", 1),
        ("TheEndGatewayRenderer", "render", "TheEndPortalRenderer", "render", 1),
        ("CampfireRenderer", "render", "CampfireBlockEntity", "getItems", 1),
        ("BrushableBlockRenderer", "render", "BrushableBlockEntity", "getHitDirection", 1),
        ("BrushableBlockRenderer", "render", "LevelRenderer", "getLightColor", 1),
        ("ConduitRenderer", "render", "Camera", "getYRot", 1),
        ("ConduitRenderer", "render", "Camera", "getXRot", 1),
        ("VaultRenderer", "renderItemInside", "ItemEntityRenderer",
         "renderMultipleFromCount", 1),
        ("DecoratedPotRenderer", "render", "DecoratedPotRenderer", "renderSide", 4),
        ("TheEndPortalRenderer", "renderCube", "TheEndPortalRenderer", "renderFace", 6),
        ("ShulkerBoxRenderer", "render", "ShulkerBoxBlockEntity", "getProgress", 2),
    ]
    for owner, method, target_owner, target_method, expected in checks:
        count = sum(row["owner"] == ROOT + owner
                    and row["method"] == method
                    and row["target_owner"].rsplit("/", 1)[-1] == target_owner
                    and row["target_method"] == target_method for row in calls)
        assert count == expected, (owner, method, target_owner, target_method, count, expected)

    print(json.dumps({
        "status": "VERIFIED_ORIGINAL_1_21_1_215_STATIC_CLASSFILES",
        "archive_sha256": actual_zip_sha,
        "classfiles": 18, "method_declarations": 133,
        "original_direct_invokes": 899, "ordered_trace_instructions": 2620,
        "independent_call_path_checks": len(checks),
        "limits": "no transformed client Mixins, loaded NeoForge mod replacements or 6/12/8/+Y/noPlanet game acceptance",
    }, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("archive_zip")
    verify(parser.parse_args().archive_zip)
