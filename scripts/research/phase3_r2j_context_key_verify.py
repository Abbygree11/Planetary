#!/usr/bin/env python3
"""3R-2j independent original target-JUnit ZIP verifier and modelled cache-key collision.

Usage:
  python scripts/research/phase3_r2j_context_key_verify.py <GitHub-Actions-artifact.zip>

This verifies original recorded per-context virtual-dispatch outcomes. The
hypothetical face-visibility comparison is a mathematical consequence of
shapes for the SAME state/direction key under changing getter context, not
proof that a real Create/AE2 mod already reproduces a Planetary gameplay bug.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile


REPORT = "phase3-r2j-synthetic-dynamic-context-dispatch.tsv"
SUMMARY = "phase3-r2j-synthetic-dynamic-context-summary.txt"


def read_member(zf, basename):
    names = [name for name in zf.namelist() if name.rsplit("/", 1)[-1] == basename]
    assert len(names) == 1, (basename, names)
    return zf.read(names[0])


def check(zip_path):
    with zipfile.ZipFile(zip_path) as zf:
        raw = read_member(zf, REPORT)
        summary = read_member(zf, SUMMARY).decode("utf-8")
    rows = list(csv.DictReader(io.StringIO(raw.decode("utf-8")), delimiter="\t"))
    assert len(rows) == 5, rows
    assert "uniqueBlockStates=1" in summary
    assert "getterIdentities=2" in summary
    assert "positions=2" in summary
    assert "selectedContextSamples=5" in summary
    assert "mutableSameStateToggle=true" in summary
    by_case = {x["case"]: x for x in rows}
    expected = {
        "planet_even_on": "true",
        "planet_odd_on": "false",
        "foreign_even_on": "false",
        "planet_even_off": "false",
        "planet_even_restored": "true",
    }
    assert set(by_case) == set(expected)
    for name, value in expected.items():
        for column in ("collision_full", "occlusion_full", "support_full",
                       "face_occlusion_full", "hides_neighbor_face"):
            assert by_case[name][column] == value, (
                name, column, by_case[name][column], value)
    assert by_case["planet_even_on"]["position"] == by_case["planet_even_off"]["position"]
    assert by_case["planet_even_on"]["getter"] == by_case["planet_even_off"]["getter"]
    assert by_case["planet_even_on"]["toggle"] != by_case["planet_even_off"]["toggle"]

    # Same identical source and neighbor BlockState, same source/target sides,
    # same physical positions, but changed mutable BlockGetter context.
    # For a full face followed by a full opposing face, nothing is visible;
    # when both are empty, Planetary returns visible at sourceShape.isEmpty().
    unchanged_state_side_key = [
        "source-state=one-synthetic-instance",
        "neighbor-state=that-same-instance",
        "source-side=UP",
        "target-side=DOWN",
    ]
    before_face_full = by_case["planet_even_on"]["face_occlusion_full"] == "true"
    after_face_full = by_case["planet_even_off"]["face_occlusion_full"] == "true"
    before_visible = not before_face_full
    after_visible = not after_face_full
    assert before_visible is False and after_visible is True
    assert before_visible != after_visible
    evidence = {
        "target_engine": "Minecraft1.21.1-NeoForge21.1.215",
        "junit_tsv_sha256": hashlib.sha256(raw).hexdigest(),
        "rows": len(rows),
        "state_objects": 1,
        "same_planet_cachekey_fields": unchanged_state_side_key,
        "context_before": "planet_even_on",
        "visibility_before_modelled": before_visible,
        "context_after": "planet_even_off",
        "visibility_after_modelled": after_visible,
        "conclusion": "state/directions-only key can alias context-dependent geometry",
        "limit": "Synthetic original NeoForge entrypoint test, NOT loaded mod or real gameplay.",
    }
    print(json.dumps(evidence, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("zip")
    args = p.parse_args()
    check(args.zip)
