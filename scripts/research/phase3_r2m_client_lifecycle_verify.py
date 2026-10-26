#!/usr/bin/env python3
"""Planetary 3R-2m: reproducible exact original target JVM client lifecycle CI audit.

Usage:
  python scripts/research/phase3_r2m_client_lifecycle_verify.py <GitHub-actions-artifact.zip>

Only original compiled Minecraft1.21.1 / NeoForge21.1.215 methods are examined.
No client/Mixin execution, no world-switch simulation, no loaded mod proof.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA256 = "73a34586bcfb0cee44ebcc040bbd29aaa96f210ad66f50cb801b74d417feb843"
MEMBERS = {
    "owners.tsv": "a652eecc57b0b5f4ace234e3dcec97217d7f72b6afe57ed78d31281656f9ebe1",
    "method-inventory.tsv": "6ed47a3c756920f9e5c5c993185113b6f24d3e1958f676c57b6c82eb6326ac75",
    "instructions.tsv": "7c13bc1281feb2c0b1c462000bc1dad0deafaeb08ea009925e50907bb4a4dec2",
    "summary.txt": "4bba1f4b0090a2d779ed783ba5a1a87e7f55411c777002dbeea4e40de05edc2d",
}
PREFIX = "phase3-r2m-original-client-"


def sha(data):
    return hashlib.sha256(data).hexdigest()


def verify(path):
    with open(path, "rb") as f:
        whole_hash = sha(f.read())
    assert whole_hash == ZIP_SHA256, (whole_hash, ZIP_SHA256)
    with zipfile.ZipFile(path) as archive:
        contents = {}
        for suffix, expected in MEMBERS.items():
            members = [name for name in archive.namelist()
                       if name.rsplit("/", 1)[-1] == PREFIX + suffix]
            assert len(members) == 1, (suffix, members)
            raw = archive.read(members[0])
            assert sha(raw) == expected, (suffix, sha(raw), expected)
            contents[suffix] = raw.decode("utf-8")

    summary = dict(row.split("=", 1) for row in contents["summary.txt"].splitlines()
                   if "=" in row)
    for key, value in {
        "target": "Minecraft1.21.1-NeoForge21.1.215",
        "classNames": "16",
        "classesPresent": "16",
        "methodsDeclared": "801",
        "methodsTraced": "88",
        "orderedInstructions": "2182",
        "directInvokes": "429",
        "indySites": "25",
    }.items():
        assert summary[key] == value, (key, summary.get(key), value)

    tables = {}
    for suffix in ("owners.tsv", "method-inventory.tsv", "instructions.tsv"):
        tables[suffix] = list(csv.DictReader(
            io.StringIO(contents[suffix]), delimiter="\t"))
    assert len(tables["owners.tsv"]) == 16
    assert len(tables["method-inventory.tsv"]) == 801
    assert len(tables["instructions.tsv"]) == 2182
    assert all(x["present"] == "true" for x in tables["owners.tsv"])
    assert sum(x["traced"] == "true" for x in tables["method-inventory.tsv"]) == 88
    ins = tables["instructions.tsv"]
    def contains(owner_suffix, method, kind, value):
        return any(x["owner"].endswith(owner_suffix) and x["method"] == method
                   and x["kind"] == kind and value in x["operand"] for x in ins)
    checks = {
        "minecraft_old_level_unload":
            contains("client/Minecraft", "setLevel", "CALL", "LevelEvent$Unload.<init>"),
        "minecraft_updates_engine_world":
            contains("client/Minecraft", "setLevel", "CALL", "Minecraft.updateLevelInEngines"),
        "minecraft_to_level_renderer":
            contains("client/Minecraft", "updateLevelInEngines",
                     "CALL", "LevelRenderer.setLevel"),
        "minecraft_to_particle_world":
            contains("client/Minecraft", "updateLevelInEngines",
                     "CALL", "ParticleEngine.setLevel"),
        "level_renderer_disposes_old_dispatcher":
            contains("renderer/LevelRenderer", "setLevel",
                     "CALL", "SectionRenderDispatcher.dispose"),
        "level_renderer_clears_old_meshes":
            contains("renderer/LevelRenderer", "setLevel",
                     "CALL", "ViewArea.releaseAllBuffers"),
        "renderer_rebuild_resets_visibility":
            contains("renderer/LevelRenderer", "allChanged",
                     "CALL", "SectionOcclusionGraph.waitAndReset"),
        "dispatcher_dispose_flag":
            contains("chunk/SectionRenderDispatcher", "dispose",
                     "FIELD", "SectionRenderDispatcher.closed"),
        "dispatcher_dispose_pending_tasks":
            contains("chunk/SectionRenderDispatcher", "dispose",
                     "CALL", "SectionRenderDispatcher.clearBatchQueue"),
        "dispatcher_cancel_priority_queue":
            contains("chunk/SectionRenderDispatcher", "clearBatchQueue",
                     "CALL", "CompileTask.cancel"),
        "dispatcher_drain_pending_uploads":
            contains("chunk/SectionRenderDispatcher", "dispose",
                     "CALL", "uploadAllPendingUploads"),
        "dispatcher_future_async_work":
            contains("chunk/SectionRenderDispatcher", "runTask",
                     "CALL", "CompletableFuture.supplyAsync"),
        "dispatcher_future_completion":
            contains("chunk/SectionRenderDispatcher", "runTask",
                     "CALL", "CompletableFuture.whenComplete"),
        "rebuild_cancel_atomic_boolean":
            contains("$RebuildTask", "cancel", "CALL",
                     "AtomicBoolean.compareAndSet"),
        "client_world_owns_model_manager":
            contains("multiplayer/ClientLevel", "<init>",
                     "CALL", "ModelDataManager.<init>"),
        "packet_listener_close_drops_level":
            contains("multiplayer/ClientPacketListener", "close",
                     "CALL", "ClientPacketListener.clearLevel"),
    }
    assert all(checks.values()), checks
    print(json.dumps({
        "archive_sha256": whole_hash,
        "target": summary["target"],
        "original_classfiles": 16,
        "method_declarations": 801,
        "method_bodies": 88,
        "instructions": 2182,
        "direct_calls": 429,
        "indy": 25,
        "confirmed_checks": checks,
        "not_proved": "runtime order of futures, world epoch, loaded mods, Mixins, gameplay"
    }, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact_zip")
    args = parser.parse_args()
    verify(args.artifact_zip)
