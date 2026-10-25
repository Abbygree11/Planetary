#!/usr/bin/env python3
"""Research-only 3R-2g original CI artifact verifier.

Reads GitHub Actions phase2-neo1211-registry-census ZIP from original
NeoForge 21.1.215 JUnit. Compares exact caller+target+opcode tuples with 3R-2f.
No network and no Minecraft executable needed.
"""
import argparse
import csv
import hashlib
import io
import zipfile
from collections import Counter

PREFIX = "phase3-neo1211-"
COLUMNS = ("source_owner", "source_method", "source_jvm_descriptor",
           "target_owner", "target_method", "target_jvm_descriptor", "invoke_opcode")


def load_tsv(zf, name):
    content = zf.read(PREFIX + name + ".tsv")
    rows = list(csv.DictReader(io.StringIO(content.decode("utf-8")), delimiter="\t"))
    assert all(set(COLUMNS).issubset(x) for x in rows)
    return rows, hashlib.sha256(content).hexdigest()


def key(row):
    return tuple(row[field] for field in COLUMNS)


def unrelated_offset(row):
    """Simple descriptor/owner filter, not a general proof of shape semantics."""
    if row["target_method"] != "getOffset":
        return False
    return not (row["target_owner"].startswith("net/minecraft/world/level/block/state/")
                and "Lnet/minecraft/world/phys/Vec3;" in row["target_jvm_descriptor"])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("zip", help="Original GitHub Actions artifact zip")
    args = parser.parse_args()
    with zipfile.ZipFile(args.zip) as zf:
        current, sha = load_tsv(zf, "engine-shape-consumer-edges")
        old, _ = load_tsv(zf, "registered-shape-consumer-edges")
        summary = zf.read(PREFIX + "engine-shape-consumer-summary.txt").decode("utf-8")
    assert len(current) == 543, len(current)
    assert len(old) == 276, len(old)
    assert sha == "94aa7f868f428400b9f5539fba9af8107d69ba0aea9dcc56f9a8cd0eaf4bf161"
    assert "discoveredClassFiles=9430" in summary
    assert "availableDiscoveryRoots=net/minecraft/world/level/block/Block.class," in summary
    old_set = {key(row) for row in old}
    current_set = {key(row) for row in current}
    delta = [row for row in current if key(row) not in old_set]
    removed = [row for row in old if key(row) not in current_set]
    assert len(delta) == 273, len(delta)
    assert len(removed) == 6 and all(
        row["source_owner"] == "dev/planetary/client/render/PlanetBlockRenderCulling"
        for row in removed)
    broad_be = [row for row in delta if row["target_method"] == "getBlockEntity"]
    unrelated_offsets = [row for row in delta if unrelated_offset(row)]
    assert len(broad_be) == 114, len(broad_be)
    assert len(unrelated_offsets) == 15, len(unrelated_offsets)
    relevant_candidates = [row for row in delta
                           if row["target_method"] != "getBlockEntity"
                           and not unrelated_offset(row)]
    assert len(relevant_candidates) == 144, len(relevant_candidates)
    assert Counter(row["target_method"] for row in delta)["getFaceOcclusionShape"] == 3
    print("3R-2g ORIGINAL 21.1.215 ZIP VERIFIED")
    print("edge SHA256:", sha)
    print("compiled class files: 9430; method-name matches:", len(current))
    print("3R-2f old / overlap / newly found / not in scanner:",
          len(old), len(old_set & current_set), len(delta), len(removed))
    print("new generic BE probes:", len(broad_be),
          "new unrelated offsets:", len(unrelated_offsets),
          "new remaining named-call candidates:", len(relevant_candidates))
    print("These are *candidate* calls, NOT proved shape consumers or loaded mod hooks.")
    print("New face-occlusion calls:")
    for row in delta:
        if row["target_method"] == "getFaceOcclusionShape":
            print("  ", row["source_owner"], row["source_method"],
                  row["source_jvm_descriptor"], "->", row["target_owner"])
    print("New non-generic by area:",
          dict(sorted(Counter(row["source_area"] for row in relevant_candidates).items())))


if __name__ == "__main__":
    main()
