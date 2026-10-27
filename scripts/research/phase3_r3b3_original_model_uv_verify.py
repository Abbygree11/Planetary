#!/usr/bin/env python3
"""Verify 3R-3B-3 original Minecraft 1.21.1/NeoForge 21.1.215 JVM CI artifact.

Usage: python scripts/research/phase3_r3b3_original_model_uv_verify.py <actions_artifact.zip>

Original classfile source evidence only. Does not execute a loaded Minecraft
client, third-party model, transformed Mixins, or an actual image/UV render.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "beefadffb9b23c85870fb27c6a50107c59cb9357fc53614ced90a6918d1f19ba"
PREFIX = "phase3-r3b3-model-uv-tint-"
MEMBERS = {
    "owners.tsv": "c1b70637f64ebf6a4d5b849762a22c5921832567fa3edc0999c3aefd5f2ef74a",
    "methods.tsv": "54b2138f85f020fb56222519c7fdf221569bf90b3534a8db43b0460a3cee1ad1",
    "fields.tsv": "c8d4c9b22b421a7c4f5a1cf5fd9e4fbe09c64cff859b18688c2032439f7d2eae",
    "invokes.tsv": "5022beee7ae13e1baf8165162f49e3a1a3ab44a6e42fa1a892c9a1c70bc3bde1",
    "instructions.tsv": "0b54fbeef694c88f5b97e8223a30c110c43a2a2691a5286909755f9b37eb5768",
    "summary.txt": "f04778d8612e9df445fef9be57efd06ee0bd9b4948a1138be47e3bcad41c90b6",
}


def digest(value):
    return hashlib.sha256(value).hexdigest()


def verify(path):
    with open(path, "rb") as file:
        archive_digest = digest(file.read())
    assert archive_digest == ZIP_SHA, (archive_digest, ZIP_SHA)
    decoded = {}
    with zipfile.ZipFile(path) as archive:
        for suffix, expected in MEMBERS.items():
            matches = [name for name in archive.namelist()
                       if name.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(matches) == 1, (suffix, matches)
            raw = archive.read(matches[0])
            assert digest(raw) == expected, (suffix, digest(raw), expected)
            decoded[suffix] = raw.decode("utf-8")
    rows = {suffix: list(csv.DictReader(io.StringIO(decoded[suffix]),
                                       delimiter="\t"))
            for suffix in ("owners.tsv", "methods.tsv", "fields.tsv",
                           "invokes.tsv", "instructions.tsv")}
    summary = dict(x.split("=", 1)
                   for x in decoded["summary.txt"].splitlines() if "=" in x)
    expected = {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "classNames": "26", "classesPresent": "26", "classesAbsent": "0",
        "methodDeclarations": "342", "methodBodiesTraced": "78",
        "fields": "153", "allDirectInvokes": "1296",
        "tracedOrderedInstructions": "1964", "tracedCallInstructions": "334",
        "faceBakeryBakingMethods": "1", "modelRendererGetQuadsSites": "6",
    }
    for key, value in expected.items():
        assert summary.get(key) == value, (key, summary.get(key), value)
    assert tuple(len(rows[name]) for name in ("owners.tsv", "methods.tsv",
            "fields.tsv", "invokes.tsv", "instructions.tsv")) == (
            26, 342, 153, 1296, 1964)
    assert all(row["present"] == "true" for row in rows["owners.tsv"])
    assert sum(row["traced"] == "true" for row in rows["methods.tsv"]) == 78

    inv = rows["invokes.tsv"]

    def hits(owner, caller, callee, target=None):
        return sum(x["source_owner"].endswith("/" + owner)
                   and x["source_method"] == caller
                   and x["target_method"] == callee
                   and (target is None or
                        x["target_owner"].endswith("/" + target)) for x in inv)

    checks = {
        "face_bakery_uv_lock": hits("FaceBakery", "bakeQuad", "isUvLocked") == 1,
        "face_bakery_sprite_shrink":
            hits("FaceBakery", "bakeQuad", "uvShrinkRatio") == 1,
        "face_bakery_neoforge_normal_hook":
            hits("FaceBakery", "bakeQuad", "fillNormal", "ClientHooks") == 1,
        "face_bakery_U": hits("FaceBakery", "fillVertex", "getU",
                              "BlockFaceUV") == 1,
        "face_bakery_V": hits("FaceBakery", "fillVertex", "getV",
                              "BlockFaceUV") == 1,
        "face_bakery_atlas_U": hits("FaceBakery", "fillVertex", "getU",
                                    "TextureAtlasSprite") == 1,
        "face_bakery_atlas_V": hits("FaceBakery", "fillVertex", "getV",
                                    "TextureAtlasSprite") == 1,
        "multipart_nested_quads": hits("MultiPartBakedModel", "getQuads",
                                       "getQuads", "BakedModel") == 1,
        "multipart_model_data_resolve": hits("MultiPartBakedModel", "getQuads",
                                             "resolve", "MultipartModelData") == 1,
        "multipart_render_type": hits("MultiPartBakedModel", "getQuads",
                                      "getRenderTypes", "BakedModel") == 1,
        "weighted_model_selection": hits("WeightedBakedModel", "getQuads",
                                         "getWeightedItem") == 1,
        "wrapper_quads_both_overloads": hits("BakedModelWrapper", "getQuads",
                                             "getQuads", "BakedModel") == 2,
        "wrapper_mod_model_data": hits("BakedModelWrapper", "getModelData",
                                      "getModelData", "BakedModel") == 1,
        "wrapper_render_passes": hits("BakedModelWrapper", "getRenderPasses",
                                     "getRenderPasses", "BakedModel") == 1,
        "registered_block_tint_callback": hits("BlockColors", "getColor",
                                               "getColor", "BlockColor") == 2,
        "section_model_data": hits("SectionCompiler", "compile",
                                    "getModelData", "BakedModel") == 1,
        "section_render_types": hits("SectionCompiler", "compile",
                                     "getRenderTypes", "BakedModel") == 1,
    }
    assert all(checks.values()), checks
    print(json.dumps({
        "source": summary["target"],
        "verification": "original compiled target 21.1.215 JVM artifact",
        "archive_sha256": archive_digest,
        "owners": 26, "methods": 342, "method_bodies": 78,
        "direct_invokes": 1296, "traced_instructions": 1964,
        "checks": checks,
        "not_proven": "loaded mods, actual UV rendering, applied Mixins or gameplay"
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("artifact_zip")
    verify(p.parse_args().artifact_zip)
