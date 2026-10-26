#!/usr/bin/env python3
"""Planetary 3R-2k: independently verify original NeoForge 21.1.215 region producer CI ZIP.

Run: python scripts/research/phase3_r2k_original_region_verify.py <artifact.zip>

This validates byte-extracted JVM owners, fields and invocations; it does not
execute a client renderer, apply Mixins or load any Create/AE2 mod.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

PREFIX = "phase3-r2k-region-model-"
HASHES = {
    "owner-types.tsv": "faf3a17676632715bb22fb6736ef586460b8c055fde9aed1fc8f4e035a5a9859",
    "fields.tsv": "bf6619e164fb733fe38e22f5f8a6f3c9c389b078edaf41a861f7da396db2cd9a",
    "methods.tsv": "5a584f01fa7fa3d0f88f1c7c8c81ebc41d1394375df120f45732aaf7a8425139",
    "invocations.tsv": "fb50d02a51cd0593ae3cd2b6904c532df4b4ff0fc30eb9973e33c3940562f33c",
}


def main(archive):
    with zipfile.ZipFile(archive) as zf:
        def read(filename):
            matches = [name for name in zf.namelist() if name.endswith(PREFIX + filename)]
            assert len(matches) == 1, (filename, matches)
            return zf.read(matches[0])
        records = {}
        for suffix, expected in HASHES.items():
            raw = read(suffix)
            actual = hashlib.sha256(raw).hexdigest()
            assert actual == expected, (suffix, actual, expected)
            records[suffix] = list(csv.DictReader(
                io.StringIO(raw.decode("utf-8")), delimiter="\t"))
        summary = read("summary.txt").decode("utf-8")
    for expected in (
        "ownersRequested=21", "ownersPresent=19", "ownersAbsent=2",
        "declaredSelectedMethods=52", "selectedFields=206",
        "selectedMethodInvocationEdges=188",
        "sectionCompilerGetModelDataCallsites=2",
        "regionBlockStateMethodDeclarations=1",
    ):
        assert expected in summary, expected
    types = {r["owner"]: r for r in records["owner-types.tsv"]}
    fields = {(r["owner"], r["field"]): r for r in records["fields.tsv"]}
    methods = records["methods.tsv"]
    calls = records["invocations.tsv"]
    mc = "net/minecraft/"
    neo = "net/neoforged/neoforge/"
    region = mc + "client/renderer/chunk/RenderChunkRegion"
    compiler = mc + "client/renderer/chunk/SectionCompiler"
    regioncache = mc + "client/renderer/chunk/RenderRegionCache"
    baked = mc + "client/resources/model/BakedModel"
    dynamic = neo + "client/model/IDynamicBakedModel"
    assert types[region]["present"] == "true"
    assert mc + "world/level/BlockAndTintGetter" in types[region]["interfaces"]
    assert types[regioncache]["present"] == "true"
    assert types[mc + "client/renderer/chunk/RenderChunkRegionCache"]["present"] == "false"
    assert types[mc + "client/renderer/chunk/RenderSectionRegion"]["present"] == "false"
    assert types[dynamic]["interfaces"] == baked
    assert fields[(region, "level")]["jvm_descriptor"] == "L" + mc + "world/level/Level;"
    assert "Long2ObjectFunction;" in fields[(region, "modelDataSnapshot")]["jvm_descriptor"]
    assert "ModelDataManager;" in fields[(
        mc + "client/multiplayer/ClientLevel", "modelDataManager")]["jvm_descriptor"]
    assert any(x["owner"] == region and x["method"] == "getModelData" for x in methods)
    assert any(x["owner"] == dynamic and x["method"] == "getQuads" for x in methods)
    assert not any(x["owner"] == dynamic and x["method"] == "getModelData" for x in methods)
    def has(source, method, target, invoked):
        return any(x["source_owner"] == source and x["source_method"] == method
                   and x["target_owner"] == target and x["target_method"] == invoked
                   for x in calls)
    assert has(regioncache, "createRegion", mc + "world/level/Level", "getModelDataManager")
    assert has(region, "getBlockState", mc + "client/renderer/chunk/RenderChunk", "getBlockState")
    assert has(compiler, "compile", region, "getBlockState")
    assert has(compiler, "compile", region, "getBlockEntity")
    assert has(compiler, "compile", region, "getModelData")
    assert has(compiler, "compile", baked, "getModelData")
    assert has(compiler, "compile", baked, "getRenderTypes")
    print(json.dumps({
        "status": "ORIGINAL_TARGET_JVM_CI_ARCHIVE_RECONCILED",
        "target": "Minecraft 1.21.1 / NeoForge 21.1.215",
        "fields": len(records["fields.tsv"]),
        "methods": len(methods),
        "invocations": len(calls),
        "producer": "Level->RenderRegionCache->RenderChunkRegion->SectionCompiler",
        "model_data": "Level.getModelDataManager + region snapshot + BakedModel.getModelData",
        "not_proved": "runtime position translation, Mixins, loaded Create/AE2, visual acceptance"
    }, indent=2))


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("artifact_zip")
    args = p.parse_args()
    main(args.artifact_zip)
