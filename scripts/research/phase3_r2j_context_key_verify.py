#!/usr/bin/env python3
"""3R-2j independent original NeoForge 21.1.215 CI ZIP contextual-collision verifier.

Usage:
  python scripts/research/phase3_r2j_context_key_verify.py <GitHub-Actions-artifact.zip>

Checks actual original registered MOVING_PISTON BlockState virtual dispatch to
PistonMovingBlockEntity shape with distinct BlockGetter, position, BE contents.
Does NOT claim occlusion-cache gameplay failure or installed Create/AE2 behavior.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile


REPORT = "phase3-r2j-original-moving-be-context-dispatch.tsv"
SUMMARY = "phase3-r2j-original-moving-be-context-summary.txt"


def member(zf, basename):
    names = [x for x in zf.namelist() if x.rsplit("/", 1)[-1] == basename]
    assert len(names) == 1, (basename, names)
    return zf.read(names[0])


def verify(artifact_zip):
    with zipfile.ZipFile(artifact_zip) as zf:
        raw = member(zf, REPORT)
        summary = member(zf, SUMMARY).decode("utf-8")
    assert "registeredBlock=minecraft:moving_piston" in summary
    assert "uniqueBlockStates=1" in summary
    assert "getterIdentities=2" in summary
    assert "positions=2" in summary
    assert "selectedContextSamples=5" in summary
    assert "sameStateSameGetterSamePositionBlockEntitySwap=true" in summary
    rows = list(csv.DictReader(io.StringIO(raw.decode("utf-8")), delimiter="\t"))
    expected = {
        "planet_even_stone": ("planet", "stone", "true"),
        "planet_odd_air": ("planet", "air", "false"),
        "foreign_even_air": ("foreign", "air", "false"),
        "planet_even_air_after_be_change": ("planet", "air", "false"),
        "planet_even_stone_restored": ("planet", "stone", "true"),
    }
    assert len(rows) == len(expected)
    indexed = {r["case"]: r for r in rows}
    assert set(indexed) == set(expected)
    for name, (getter, moved, is_nonempty) in expected.items():
        row = indexed[name]
        assert (row["getter"], row["moved_state"], row["collision_nonempty"]) == (
            getter, moved, is_nonempty), (name, row)
    assert indexed["planet_even_stone"]["position"] == indexed["foreign_even_air"]["position"]
    assert indexed["planet_even_stone"]["position"] == indexed["planet_even_air_after_be_change"]["position"]
    assert indexed["planet_even_stone"]["position"] == indexed["planet_even_stone_restored"]["position"]
    # The collision result changes with identical state, getter, physical pos
    # after replacing only the BlockEntity. This is a concrete source-context
    # counterexample for any collision cache that uses BlockState alone.
    assert indexed["planet_even_stone"]["collision_nonempty"] != (
        indexed["planet_even_air_after_be_change"]["collision_nonempty"])
    proof = {
        "version": "Minecraft1.21.1-NeoForge21.1.215",
        "engine_entrypoint": "MOVING_PISTON BlockState.getCollisionShape",
        "original_ci_tsv_sha256": hashlib.sha256(raw).hexdigest(),
        "context_samples": len(rows),
        "same_state_same_getter_same_pos_be_change_changes_collision": True,
        "distinct_getters_at_same_pos_change_collision": True,
        "distinct_positions_at_same_getter_change_collision": True,
        "scope": "registered vanilla dynamic BlockEntity collision, no new Block registration",
        "NOT_PROVED": [
            "actual third-party mod runtime",
            "Planetary client applied Mixins",
            "face-occlusion visibility/caching divergence",
            "six-face/edge/corner gameplay acceptance"
        ],
    }
    print(json.dumps(proof, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("zip")
    args = p.parse_args()
    verify(args.zip)
