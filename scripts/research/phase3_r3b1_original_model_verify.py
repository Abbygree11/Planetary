#!/usr/bin/env python3
"""Planetary 3R-3B-1: exact original NeoForge21.1.215 BakedModel/quad/cull ABI archive verifier.

python scripts/research/phase3_r3b1_original_model_verify.py <GitHub-Actions-artifact.zip>

Validates immutable original selected classfiles, declarations and static invoke
edges. Not an applied Mixin, rendered frame, dynamic model override or gameplay
test; model state/ModelData and loaded mod behavior remain separate.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "b086d7a59f8b204854d791a6b6aa511dc11a87e769c88f20d03ca45826bbaa08"
PREFIX = "phase3-r3b1-original-model-"
REPORTS = {
    "owners.tsv": "985d13bcaf04167c56138d0f64c6857dcf1e3319b582de5ffc85de41b995ec8d",
    "methods.tsv": "2ae159c49a1bcba92df85fb9178544605688d7b3d93b084746d4389a81ddaf2f",
    "fields.tsv": "9d35340ae79b197f32f142cd691a5a9b9d13310b30ff8cd2c090b4dab266425e",
    "invokes.tsv": "e3422d25e44d7aac8c73518c43e864e3124abbe3027c866d48d8b65d457b1ec8",
    "summary.txt": "062e9dc972cd5b97dc05f2f10caa0c85e6b3eab274ffe171c3bb4e8e480ce97a",
}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def verify(path):
    with open(path, "rb") as inp:
        actual_zip_sha = digest(inp.read())
    assert actual_zip_sha == ZIP_SHA, (actual_zip_sha, ZIP_SHA)
    with zipfile.ZipFile(path) as archive:
        data = {}
        for suffix, expected in REPORTS.items():
            names = [name for name in archive.namelist()
                     if name.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(names) == 1, (suffix, names)
            raw = archive.read(names[0])
            assert digest(raw) == expected, (suffix, digest(raw), expected)
            data[suffix] = raw.decode("utf-8")

    summary = dict(line.split("=", 1) for line in data["summary.txt"].splitlines()
                   if "=" in line)
    for key, expected in {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "ownersRequested": "17", "ownersPresent": "17", "ownersAbsent": "0",
        "declaredMethods": "539", "fields": "183", "selectedInvokes": "135",
        "modelRendererGetQuadsSites": "6",
        "modelRendererShouldRenderFaceSites": "2",
        "blockFaceOcclusionSites": "2",
    }.items():
        assert summary[key] == expected, (key, summary.get(key), expected)

    tables = {}
    for suffix in ("owners.tsv", "methods.tsv", "fields.tsv", "invokes.tsv"):
        tables[suffix] = list(csv.DictReader(io.StringIO(data[suffix]), delimiter="\t"))
    assert (len(tables["owners.tsv"]), len(tables["methods.tsv"]),
            len(tables["fields.tsv"]), len(tables["invokes.tsv"])) == (17, 539, 183, 135)
    assert all(row["present"] == "true" for row in tables["owners.tsv"])
    edges = tables["invokes.tsv"]
    mc = "net/minecraft/"
    model_renderer = mc + "client/renderer/block/ModelBlockRenderer"
    block = mc + "world/level/block/Block"
    compiler = mc + "client/renderer/chunk/SectionCompiler"
    baked = mc + "client/resources/model/BakedModel"
    quad = mc + "client/renderer/block/model/BakedQuad"
    dispatcher = mc + "client/renderer/block/BlockRenderDispatcher"

    def count(source, target, method, source_method=None):
        return sum(row["source_owner"] == source
                   and row["target_owner"] == target
                   and row["target_method"] == method
                   and (source_method is None or row["source_method"] == source_method)
                   for row in edges)
    assert count(model_renderer, baked, "getQuads") == 6
    assert count(model_renderer, block, "shouldRenderFace") == 2
    assert count(block, mc + "world/level/block/state/BlockState",
                 "getFaceOcclusionShape", "shouldRenderFace") == 2
    assert count(compiler, baked, "getModelData") == 1
    assert count(compiler, baked, "getRenderTypes") == 1
    assert count(compiler, dispatcher, "renderBatched") == 1
    assert count(model_renderer, quad, "getDirection") == 5

    owners = {r["owner"]: r for r in tables["owners.tsv"]}
    assert owners[baked]["interfaces"] == (
        "net/neoforged/neoforge/client/extensions/IBakedModelExtension")
    assert owners["net/neoforged/neoforge/client/model/IDynamicBakedModel"]["interfaces"] == baked
    assert owners["net/neoforged/neoforge/client/model/lighting/SmoothQuadLighter"]["present"] == "true"
    assert owners["net/neoforged/neoforge/client/model/lighting/FlatQuadLighter"]["present"] == "true"

    print(json.dumps({
        "source": "original Minecraft1.21.1/NeoForge21.1.215 compiled bytes",
        "zip_sha256": actual_zip_sha,
        "original_owners": 17,
        "method_declarations": 539,
        "selected_invoke_edges": 135,
        "model_getQuads_calls": 6,
        "original_shouldRenderFace_call_sites": 2,
        "faceOcclusion_original_block_calls": 2,
        "limits": "static selected JVM callsites; actual rendered client, BER, dynamic mod dispatch and game acceptance open"
    }, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    args = argparse.ArgumentParser()
    args.add_argument("artifact_zip")
    verify(args.parse_args().artifact_zip)
