#!/usr/bin/env python3
"""Planetary 3R-3B-2: independent original MC1.21.1 / NeoForge21.1.215 CI audit.

Usage:
 python scripts/research/phase3_r3b2_original_ao_quad_verify.py <downloaded_GitHub_Actions_artifact.zip>

This is a byte-for-byte check of the selected *untransformed* target JVM trace.
It does not run a client, mixin-transform Minecraft, or load AE2/Create.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ORIGINAL_ZIP_SHA256 = "92f92dc41ea6fe76f77943eec9815a1ca316ad578ea037195d1bd36d6e99c3d0"
MEMBERS = {
    "owners.tsv": "e39bfea9cdb7a8b6fb1e25f557dbca227c97c8d2423f47198e54720a4cc841b1",
    "methods.tsv": "c32261713add8e9d56b77337a4ecbdab386435b7f05d4c12ab69b3a9282b4c05",
    "instructions.tsv": "bfc20636398d85970b36cdac4b3f128d7caa00f90033cd0fefe60b580ce8a7a3",
    "summary.txt": "8bad143ffde52341e4d08cad94c8bb957acc2e76d2fad62c78fb7b815839c99a",
}
PREFIX = "phase3-r3b2-original-quad-ao-"


def sha256(raw):
    return hashlib.sha256(raw).hexdigest()


def verify(zip_path):
    with open(zip_path, "rb") as handle:
        actual = sha256(handle.read())
    assert actual == ORIGINAL_ZIP_SHA256, (actual, ORIGINAL_ZIP_SHA256)
    with zipfile.ZipFile(zip_path) as zf:
        members = {}
        for suffix, expected_hash in MEMBERS.items():
            filenames = [name for name in zf.namelist()
                         if name.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(filenames) == 1, (suffix, filenames)
            raw = zf.read(filenames[0])
            assert sha256(raw) == expected_hash, (suffix, sha256(raw), expected_hash)
            members[suffix] = raw.decode("utf-8")
    summary = dict(line.split("=", 1)
                   for line in members["summary.txt"].splitlines() if "=" in line)
    expected = {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "requestedClassNames": "21", "presentClassfiles": "21",
        "absentClassfiles": "0", "selectedMethodBodies": "90",
        "orderedInstructions": "5484", "directInvokes": "519",
        "fieldAccesses": "369", "invokedynamicSites": "1",
        "rendererGetQuadsCalls": "6", "rendererShouldRenderFaceCalls": "2",
    }
    for key, value in expected.items():
        assert summary.get(key) == value, (key, summary.get(key), value)

    tables = {suffix: list(csv.DictReader(io.StringIO(members[suffix]),
                                          delimiter="\t"))
              for suffix in ("owners.tsv", "methods.tsv", "instructions.tsv")}
    assert len(tables["owners.tsv"]) == 21
    assert len(tables["methods.tsv"]) == 90
    assert len(tables["instructions.tsv"]) == 5484
    assert all(row["present"] == "true" for row in tables["owners.tsv"])
    opcodes = tables["instructions.tsv"]

    def count(owner, method, expected_kind, target):
        return sum(row["owner"].endswith("/" + owner)
                   and row["method"] == method
                   and row["kind"] == expected_kind
                   and target in row["operand"]
                   for row in opcodes)

    checks = {
        "alternate_render_getQuads":
            count("LightPipelineAwareModelBlockRenderer", "render", "CALL",
                  "BakedModel.getQuads") == 2,
        "alternate_render_shouldRenderFace":
            count("LightPipelineAwareModelBlockRenderer", "render", "CALL",
                  "Block.shouldRenderFace") == 1,
        "alternate_render_QuadLighter_process":
            count("LightPipelineAwareModelBlockRenderer", "render", "CALL",
                  "QuadLighter.process") == 4,
        "smooth_face_occlusion_shape":
            count("SmoothQuadLighter", "computeLightingAt", "CALL",
                  "getFaceOcclusionShape") == 2,
        "smooth_world_light":
            count("SmoothQuadLighter", "computeLightingAt", "CALL",
                  "LevelRenderer.getLightColor") >= 1,
        "flat_collision_full_cube":
            count("FlatQuadLighter", "computeLightingAt", "CALL",
                  "BlockState.getCollisionShape") == 1,
        "flat_world_light":
            count("FlatQuadLighter", "computeLightingAt", "CALL",
                  "LevelRenderer.getLightColor") == 2,
        "vanilla_face_model_cache":
            count("Block", "shouldRenderFace", "FIELD", "Block.OCCLUSION_CACHE") == 1,
        "vanilla_face_shape_query":
            count("Block", "shouldRenderFace", "CALL",
                  "BlockState.getFaceOcclusionShape") == 2,
        "model_triState_ao":
            count("ModelBlockRenderer", "tesselateBlock", "CALL",
                  "BakedModel.useAmbientOcclusion") == 1,
        "quad_color_callback":
            count("QuadLighter", "getColorFast", "CALL",
                  "BlockColors.getColor") >= 1,
    }
    assert all(checks.values()), checks
    print(json.dumps({
        "result": "VERIFIED_ORIGINAL_TARGET_SELECTED_CLASSFILE_BYTES",
        "target": summary["target"],
        "artifact_sha256": actual,
        "classfiles": 21, "method_bodies": 90, "ordered_instructions": 5484,
        "direct_invokes": 519, "field_accesses": 369,
        "verified_original_edges": checks,
        "limitations": "selected original JVM method bodies; no loaded mods, applied mixins, client game or dynamic culling acceptance"
    }, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact_zip")
    verify(parser.parse_args().artifact_zip)
