#!/usr/bin/env python3
"""Verify original NeoForge 21.1.215 BER event/provider/dispatcher CI artifact.

Usage: python scripts/research/phase3_r4d_original_ber_provider_verify.py <CI_zip>

Static JVM evidence only. No loaded third-party provider/Mixin/rendering acceptance.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "7d1d239e800dca80119870c5fd99955ffb9485e33b2ebb717d264ec7a8b5e444"
PREFIX = "phase3-r4d-original-ber-provider-"
MEMBERS = {
    "owners.tsv": "fa5c4bdc60c97e9775129060dd6e799d3682ea1a99c777d3ad38017e16aa0e20",
    "methods.tsv": "24e870e49f58b405b7a375a4c4159473a47867022644a2a9c81a73f80a3260a0",
    "invokes.tsv": "d5f1491aaf4c72951194253240bc39778ba88ca3d47365d58293d38a58d56c3f",
    "instructions.tsv": "fbd6d0c1ae8855b15be0033b444c94deadc00e8a105cdd6f236224b5727bb1c5",
    "summary.txt": "2f39642d76724aba06584f650909420bed954812e035aad2addf87afdde295b9",
}
BER = "net/minecraft/client/renderer/blockentity/"
NEO = "net/neoforged/neoforge/client/"
EXPECTED = {
    BER+"BlockEntityRenderDispatcher", BER+"BlockEntityRenderer",
    BER+"BlockEntityRendererProvider", BER+"BlockEntityRendererProvider$Context",
    BER+"BlockEntityRenderers",
    NEO+"ClientHooks", NEO+"event/EntityRenderersEvent$RegisterRenderers",
    NEO+"extensions/IBlockEntityRendererExtension",
}

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

def verify(path):
    with open(path, "rb") as f:
        digest = sha(f.read())
    assert digest == ZIP_SHA, (digest, ZIP_SHA)
    with zipfile.ZipFile(path) as z:
        data = {}
        for suffix, expected in MEMBERS.items():
            paths = [n for n in z.namelist() if n.rsplit("/", 1)[-1] == PREFIX+suffix]
            assert len(paths) == 1, (suffix, paths)
            raw = z.read(paths[0])
            assert sha(raw) == expected, (suffix, sha(raw), expected)
            data[suffix] = raw.decode("utf-8")
    summary = dict(line.split("=", 1) for line in data["summary.txt"].splitlines() if "=" in line)
    for k, v in {
        "target":"Minecraft1.21.1-NeoForge21.1.215",
        "stage":"3R-4D", "classfiles":"8","methods":"160","invokes":"616",
        "selectedInstructions":"213",
        "scope":"original_factory_and_dispatcher_static_JVM_not_loaded_mod",
    }.items():
        assert summary.get(k) == v, (k,summary.get(k),v)
    tables = {key:list(csv.DictReader(io.StringIO(data[key]),delimiter="\t"))
              for key in ("owners.tsv","methods.tsv","invokes.tsv","instructions.tsv")}
    assert [len(tables[k]) for k in ("owners.tsv","methods.tsv","invokes.tsv","instructions.tsv")] == [8,160,616,213]
    owners = {r["owner"]:r for r in tables["owners.tsv"]}
    assert owners.keys() == EXPECTED
    assert all(len(r["sha256"]) == 64 for r in owners.values())
    assert "IBlockEntityRendererExtension" in owners[BER+"BlockEntityRenderer"]["interfaces"]
    calls = tables["invokes.tsv"]
    # original declaring source and target JVM member, not a simple string occurrence
    checks = [
        ("EntityRenderersEvent$RegisterRenderers","registerBlockEntityRenderer","BlockEntityRenderers","register",1),
        ("BlockEntityRenderers","register","Map","put",1),
        ("BlockEntityRenderers","createEntityRenderers","Map","forEach",1),
        ("BlockEntityRenderers","lambda$createEntityRenderers$0","BlockEntityRendererProvider","create",1),
        ("BlockEntityRenderDispatcher","onResourceManagerReload","BlockEntityRenderers","createEntityRenderers",1),
        ("BlockEntityRenderDispatcher","getRenderer","Map","get",1),
        ("BlockEntityRenderDispatcher","render","BlockEntityRenderDispatcher","getRenderer",1),
        ("BlockEntityRenderDispatcher","renderItem","BlockEntityRenderDispatcher","getRenderer",1),
        ("BlockEntityRenderDispatcher","render","BlockEntityRenderer","shouldRender",1),
    ]
    for source,method,target,called,count in checks:
        actual = sum(row["owner"].split("/")[-1] == source and row["method"] == method
                     and row["target_owner"].split("/")[-1] == target
                     and row["target_method"] == called for row in calls)
        assert actual == count, (source,method,target,called,actual,count)
    print(json.dumps({
        "status":"VERIFIED_ORIGINAL_TARGET_NEOFORGE21_1_215_STATIC_DISPATCH",
        "zip_sha256":digest, "owners":8, "methods":160, "invokes":616,
        "ordered_selected_instructions":213, "independent_nested_path_checks":len(checks),
        "limitations":"no loaded mod, provider swap in client, actual six-face world or shader runtime"
    },ensure_ascii=False,indent=2))

if __name__ == "__main__":
    a=argparse.ArgumentParser();a.add_argument("archive_zip");verify(a.parse_args().archive_zip)
