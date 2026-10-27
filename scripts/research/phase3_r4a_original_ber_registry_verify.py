#!/usr/bin/env python3
"""3R-4A original Minecraft1.21.1 NeoForge21.1.215 vanilla BER registry verifier.

python scripts/research/phase3_r4a_original_ber_registry_verify.py <CI_artifact.zip>
Source: untransformed 1.21.1 original target compiled registry and BER classes,
not loaded custom NeoForge renderers, Mixins or live six-face gameplay.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "74a3836cb84be9ef42e2a22bede6fa7c7f5afde96478f1ef290ba37f4ae0c40b"
PREFIX = "phase3-r4a-original-ber-"
MEMBERS = {
    "registrations.tsv": "7eba226aadf512af921ac266024f9845b238c7b5d0f37607461a3910dc10ee42",
    "owners.tsv": "3fe8c0fe5c7688bbec7df56012e6efe4e7336daf5e80042102ea65d9094fccac",
    "methods.tsv": "bf876aec2f7bcfa25a2f7a385a257fc2cbd7f319001a1595bfa5e4f0ceeb81d0",
    "fields.tsv": "ca803f6a52046156a58606321ac98e995f290024843a0a911ab2f7747899a4c6",
    "invokes.tsv": "b7d5f4d7152db033750b7eecee4f12d3936a465c8d4ba550ff2bd755d5beb608",
    "summary.txt": "cccb6ae8881d117a41fa5c8f44a024e9ac560657aba947c0e7e6162e47eb217d",
}
EXPECTED = (
    ("SIGN","SignRenderer"), ("HANGING_SIGN","HangingSignRenderer"),
    ("MOB_SPAWNER","SpawnerRenderer"), ("PISTON","PistonHeadRenderer"),
    ("CHEST","ChestRenderer"), ("ENDER_CHEST","ChestRenderer"),
    ("TRAPPED_CHEST","ChestRenderer"), ("ENCHANTING_TABLE","EnchantTableRenderer"),
    ("LECTERN","LecternRenderer"), ("END_PORTAL","TheEndPortalRenderer"),
    ("END_GATEWAY","TheEndGatewayRenderer"), ("BEACON","BeaconRenderer"),
    ("SKULL","SkullBlockRenderer"), ("BANNER","BannerRenderer"),
    ("STRUCTURE_BLOCK","StructureBlockRenderer"),
    ("SHULKER_BOX","ShulkerBoxRenderer"), ("BED","BedRenderer"),
    ("CONDUIT","ConduitRenderer"), ("BELL","BellRenderer"),
    ("CAMPFIRE","CampfireRenderer"), ("BRUSHABLE_BLOCK","BrushableBlockRenderer"),
    ("DECORATED_POT","DecoratedPotRenderer"),
    ("TRIAL_SPAWNER","TrialSpawnerRenderer"), ("VAULT","VaultRenderer"),
)
PREF = "net/minecraft/client/renderer/blockentity/"


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def verify(path):
    with open(path, "rb") as f:
        found = sha(f.read())
    assert found == ZIP_SHA, (found, ZIP_SHA)
    with zipfile.ZipFile(path) as z:
        content = {}
        for suffix, expected in MEMBERS.items():
            found_paths = [n for n in z.namelist()
                           if n.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(found_paths) == 1, (suffix, found_paths)
            raw = z.read(found_paths[0])
            assert sha(raw) == expected, (suffix, sha(raw), expected)
            content[suffix] = raw.decode("utf-8")
    summary = dict(row.split("=", 1)
                   for row in content["summary.txt"].splitlines() if "=" in row)
    for key, expected in {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "registration_count": "24", "distinct_renderer_factories": "22",
        "original_owner_classfiles": "28", "method_declarations": "219",
        "field_declarations": "109", "direct_invokes": "1320",
    }.items():
        assert summary.get(key) == expected, (key, summary.get(key), expected)
    rows = {suffix: list(csv.DictReader(io.StringIO(content[suffix]), delimiter="\t"))
            for suffix in ("registrations.tsv","owners.tsv","methods.tsv",
                           "fields.tsv","invokes.tsv")}
    assert [len(rows[suffix]) for suffix in ("registrations.tsv","owners.tsv",
            "methods.tsv","fields.tsv","invokes.tsv")] == [24,28,219,109,1320]
    registrations = rows["registrations.tsv"]
    pairs = [(r["block_entity_type"], r["constructor_owner"].split("/")[-1])
             for r in registrations]
    assert pairs == list(EXPECTED), pairs
    assert all(r["bootstrap"] == "java/lang/invoke/LambdaMetafactory.metafactory"
               for r in registrations)
    assert all(r["constructor_desc"] ==
               "(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;)V"
               for r in registrations)
    owners = {x["owner"]: x for x in rows["owners.tsv"]}
    assert owners[PREF + "HangingSignRenderer"]["superclass"] == PREF + "SignRenderer"
    assert owners[PREF + "TheEndGatewayRenderer"]["superclass"] == PREF + "TheEndPortalRenderer"
    assert "IBlockEntityRendererExtension" in owners[PREF + "BlockEntityRenderer"]["interfaces"]

    invokes = rows["invokes.tsv"]

    def owns_call(source, method, target):
        return any(x["owner"] == PREF + source and x["method"] == "render"
                   and x["target_owner"].endswith("/" + target[0])
                   and x["target_method"] == target[1] for x in invokes)
    calls = {
        "sign_nested": owns_call("SignRenderer","render",
                                  ("SignRenderer","renderSignWithText")),
        "hanging_inherited": owns_call("HangingSignRenderer","render",
                                      ("HangingSignRenderer","renderSignWithText")),
        "gateway_portal_super": owns_call("TheEndGatewayRenderer","render",
                                          ("TheEndPortalRenderer","render")),
        "gateway_beam": owns_call("TheEndGatewayRenderer","render",
                                  ("BeaconRenderer","renderBeaconBeam")),
        "skull_nested": owns_call("SkullBlockRenderer","render",
                                  ("SkullBlockRenderer","renderSkull")),
        "banner_patterns": owns_call("BannerRenderer","render",
                                     ("BannerRenderer","renderPatterns")),
    }
    assert all(calls.values()), calls
    print(json.dumps({
        "status": "VERIFIED_ORIGINAL_TARGET",
        "archive_sha256": found, "registered_types": 24,
        "distinct_renderers": 22, "original_classfiles": 28,
        "declared_methods": 219, "direct_invokes": 1320,
        "tested_source_call_paths": calls,
        "limits": "original vanilla factories only; custom NeoForge registrations, applied Mixins, +Y/noPlanet game and mod acceptance OPEN",
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact_zip")
    verify(parser.parse_args().artifact_zip)
