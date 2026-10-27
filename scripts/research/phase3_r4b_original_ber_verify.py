#!/usr/bin/env python3
"""Verify target original NeoForge21.1.215 BER Sign/Banner/Skull method-body CI ZIP.

python scripts/research/phase3_r4b_original_ber_verify.py <GitHub_Actions_artifact.zip>

This is selected static JVM source verification, not a transformed Minecraft
client, loaded Create/AE2 replacement, or six-face gameplay visual test.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "a9101b837be8c273d766a825208a8029c7a4e6ca0886fa7cf96281a5267429c4"
PREFIX = "phase3-r4b-original-ber-"
MEMBERS = {
    "owners.tsv": "619e304a4b5cee32e4c693090b7fc47bf3f3ca5fe993086c08f92581cf75a643",
    "methods.tsv": "f78681061e82c30b0e3565063947b152026304f85985a6b1cea67e4f8846af98",
    "fields.tsv": "39d92365d4a6e83b8e8c3e50d0cfc3ce03163ab09da8838fe1fcd20530a23ac1",
    "instructions.tsv": "fc8470ac2e7f544fe0f70ea86ea1604d54c62039b19042dfb25432e800bab217",
    "summary.txt": "5252b30c46c9c65893b740e62a5a90db8c6397aecae3542af367e0bb09054374",
}

def sha(b):
    return hashlib.sha256(b).hexdigest()

def check(path):
    with open(path, "rb") as file:
        digest = sha(file.read())
    assert digest == ZIP_SHA, (digest, ZIP_SHA)
    with zipfile.ZipFile(path) as archive:
        data = {}
        for suffix, expected in MEMBERS.items():
            found = [name for name in archive.namelist()
                     if name.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(found) == 1, (suffix, found)
            raw = archive.read(found[0])
            assert sha(raw) == expected, (suffix, sha(raw), expected)
            data[suffix] = raw.decode("utf-8")
    summary = dict(row.split("=", 1) for row in data["summary.txt"].splitlines()
                   if "=" in row)
    for key, expected in {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "stage": "3R-4B", "classNames": "22", "presentClassfiles": "22",
        "methodDeclarations": "297", "selectedBodies": "66",
        "fields": "247", "instructions": "1840",
        "calls": "414", "indys": "5",
    }.items():
        assert summary.get(key) == expected, (key, summary.get(key), expected)
    tables = {suffix: list(csv.DictReader(io.StringIO(data[suffix]), delimiter="\t"))
              for suffix in ("owners.tsv", "methods.tsv", "fields.tsv",
                             "instructions.tsv")}
    assert [len(tables[x]) for x in ("owners.tsv", "methods.tsv", "fields.tsv",
                                     "instructions.tsv")] == [22, 297, 247, 1840]
    assert all(r["present"] == "true" for r in tables["owners.tsv"])
    owners = {r["owner"]: r for r in tables["owners.tsv"]}
    base = "net/minecraft/client/renderer/blockentity/"
    assert owners[base + "HangingSignRenderer"]["superclass"] == (
        base + "SignRenderer")
    ins = tables["instructions.tsv"]

    checks = [
        ("SignRenderer", "renderSignWithText", "CALL", ".getFrontText(", 1),
        ("SignRenderer", "renderSignWithText", "CALL", ".getBackText(", 1),
        ("SignRenderer", "renderSignWithText", "CALL", ".renderSignText(", 2),
        ("SignRenderer", "renderSignWithText", "CALL", ".renderSign(", 1),
        ("HangingSignRenderer", "render", "CALL", ".evaluateVisibleParts(", 1),
        ("HangingSignRenderer", "render", "CALL", ".renderSignWithText(", 1),
        ("HangingSignModel", "evaluateVisibleParts", "FIELD",
         "BlockStateProperties.ATTACHED", 1),
        ("BannerRenderer", "render", "CALL", ".renderPatterns(", 1),
        ("BannerRenderer", "render", "CALL", ".getGameTime(", 1),
        ("BannerRenderer", "renderPatterns", "CALL", ".renderPatternLayer(", 2),
        ("SkullBlockRenderer", "render", "CALL", ".getOwnerProfile(", 1),
        ("SkullBlockRenderer", "render", "CALL", ".renderSkull(", 1),
        ("SkullBlockRenderer", "getRenderType", "CALL",
         "SkinManager.getInsecureSkin", 1),
        ("SkullBlockRenderer", "render", "FIELD", "SkullBlock.ROTATION", 1),
        ("SkullBlockRenderer", "render", "FIELD", "WallSkullBlock.FACING", 1),
    ]
    for owner, method, kind, operand, expected in checks:
        total = sum(row["owner"].split("/")[-1].endswith(owner)
                    and row["method"] == method
                    and row["kind"] == kind
                    and operand in row["operand"] for row in ins)
        assert total == expected, (owner, method, operand, total, expected)
    print(json.dumps({
        "result": "VERIFIED_ORIGINAL_21_1_215_STATIC_CLASSFILES",
        "zip_sha256": digest,
        "classfiles": 22, "selected_method_bodies": 66,
        "ordered_instructions": 1840,
        "direct_calls": 414, "invokedynamic_sites": 5,
        "targeted_path_checks": len(checks),
        "limits": "no loaded mods, actual BER Mixin application or six-face client test"
    }, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("zipfile")
    args = p.parse_args()
    check(args.zipfile)
