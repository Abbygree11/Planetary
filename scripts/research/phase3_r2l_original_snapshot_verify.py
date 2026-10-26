#!/usr/bin/env python3
"""Planetary 3R-2l: independently verify original MC 1.21.1 / NeoForge 21.1.215 JVM archive.

Usage:
 python scripts/research/phase3_r2l_original_snapshot_verify.py <downloaded-actions-artifact.zip>

The archive is a static *untransformed* compiled owner/method/instruction trace,
NOT a runtime thread scheduling assertion, loaded mod compatibility check, or
applied client Mixin/gameplay test.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

EXPECTED_ZIP_SHA256 = "598dadaf7b02711be28b4befbd3a162dbb7f66cd7991abf5ee2526ecf7427a8f"
FILES = {
    "classes.tsv": "1d988549291a17ebc60767d5616e84aa62c917a67488293cbf5f6bccc727bb45",
    "methods.tsv": "5ec5b0bb0b508fdbf6a5151fb8a480e906fb555684b8fb01af088bd900f51e1a",
    "instructions.tsv": "46633811a86c0d9d470658bd1f8272e21704633d7b6ca8f6669493c9be5641bd",
    "summary.txt": "adbc11ad8c007fd03a4cfd80b10def471ab8ef2d15f899895631d4915ea3aec2",
}
PREFIX = "phase3-r2l-original-worker-"


def sha256(value):
    return hashlib.sha256(value).hexdigest()


def verify(path):
    with open(path, "rb") as stream:
        actual_zip_sha = sha256(stream.read())
    assert actual_zip_sha == EXPECTED_ZIP_SHA256, (actual_zip_sha, EXPECTED_ZIP_SHA256)

    with zipfile.ZipFile(path) as archive:
        contents = {}
        for suffix, expected_hash in FILES.items():
            matching = [
                n for n in archive.namelist() if n.rsplit("/", 1)[-1] == PREFIX + suffix
            ]
            assert len(matching) == 1, (suffix, matching)
            data = archive.read(matching[0])
            assert sha256(data) == expected_hash, (suffix, sha256(data), expected_hash)
            contents[suffix] = data.decode("utf-8")

    lines = dict(line.split("=", 1) for line in contents["summary.txt"].splitlines()
                 if "=" in line)
    expected = {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "ownerNames": "15",
        "ownerAbsent": "0",
        "selectedMethodBodies": "58",
        "orderedInstructions": "1946",
        "directInvokes": "380",
        "invokeDynamic": "19",
        "regionCreateBodies": "2",
        "workerBodies": "7",
    }
    for name, value in expected.items():
        assert lines[name] == value, (name, lines[name], value)

    rows = {
        key: list(csv.DictReader(io.StringIO(contents[key]), delimiter="\t"))
        for key in ("classes.tsv", "methods.tsv", "instructions.tsv")
    }
    assert len(rows["classes.tsv"]) == 15
    assert len(rows["methods.tsv"]) == 58
    assert len(rows["instructions.tsv"]) == 1946
    assert all(x["present"] == "true" for x in rows["classes.tsv"])
    ins = rows["instructions.tsv"]

    def has(suffix, method, kind, operand):
        return any(
            x["owner"].endswith(suffix)
            and x["method"] == method
            and x["kind"] == kind
            and operand in x["operand"]
            for x in ins
        )

    assertions = {
        "region_cache_explicit_snapshot":
            has("RenderRegionCache", "createRegion", "CALL",
                "ModelDataManager.snapshotSectionRegion"),
        "snapshot_owner_thread_guard":
            has("ModelDataManager", "snapshotSectionRegion", "CALL", "isOtherThread"),
        "snapshot_shallow_map_copy":
            has("ModelDataManager", "snapshotSectionRegion", "CALL", "Long2ObjectMap.putAll"),
        "snapshot_empty_value":
            has("ModelDataManager", "snapshotSectionRegion", "FIELD", "ModelData.EMPTY"),
        "snapshot_empty_map_fallback":
            has("ModelDataManager", "snapshotSectionRegion", "FIELD", "EMPTY_SNAPSHOT"),
        "requested_refresh_owner_level_check":
            has("ModelDataManager", "requestRefresh", "CALL", "BlockEntity.getLevel"),
        "refresh_reads_original_level_be":
            has("ModelDataManager", "refreshAt", "CALL", "Level.getBlockEntity"),
        "refresh_reads_be_model_data":
            has("ModelDataManager", "refreshAt", "CALL", "BlockEntity.getModelData"),
        "render_region_reads_snapshot_field":
            has("RenderChunkRegion", "getModelData", "FIELD", "modelDataSnapshot"),
        "render_region_model_data_blockpos_key":
            has("RenderChunkRegion", "getModelData", "CALL", "BlockPos.asLong"),
        "render_section_creates_explicit_region":
            has("SectionRenderDispatcher$RenderSection", "createCompileTask", "CALL",
                "RenderRegionCache.createRegion"),
        "rebuild_task_invokes_section_compiler":
            has("SectionRenderDispatcher$RenderSection$RebuildTask", "doTask",
                "CALL", "SectionCompiler.compile"),
        "neoforge_additional_geometry_uses_region":
            has("SectionCompiler", "compile", "CALL", "ClientHooks.addAdditionalGeometry"),
    }
    assert all(assertions.values()), assertions
    print(json.dumps({
        "proof": "original target 21.1.215 selected JVM bytes",
        "archive_sha256": actual_zip_sha,
        "owner_classfiles": len(rows["classes.tsv"]),
        "method_bodies": len(rows["methods.tsv"]),
        "instructions": len(ins),
        "assertions": assertions,
        "scope_limitation": "no loaded Minecraft client, applied Mixins, or Create/AE2 runtime"
    }, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("zip")
    args = parser.parse_args()
    verify(args.zip)
