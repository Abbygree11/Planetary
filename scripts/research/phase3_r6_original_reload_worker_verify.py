#!/usr/bin/env python3
"""Verify actual 3R-6 original NeoForge 21.1.215 CI archive.

Usage: python scripts/research/phase3_r6_original_reload_worker_verify.py <ci_zip>
Only original JVM/static source owners; never a running mod, game, worker race or GPU proof.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA256 = "48a2b2574b044795d7d6630654929a3ba9dbef3383882461a4000a3dcc18a46e"
PREFIX = "phase3-r6-original-reload-worker-"
MEMBER_SHA256 = {
    "owners.tsv": "7d2b18eb7cad651d8f3ab4174a0261e1004c67b2a54b56032fea70005706f65e",
    "methods.tsv": "81ba3dbd3881201e2b393b1ec6e22e1cf8b002b205129b3c18c5a779fb08808c",
    "calls.tsv": "60109327a8faff492d5fd4f5f54d4a1b3c32a2d990881a6449226f771d7b263f",
    "instructions.tsv": "295634c5f706550251f037d3f3274686ed0bf35ac9b5b8aaa49157c7fdba66d1",
    "summary.txt": "96b44fe900a56c8694926a4ad3642de525237f01bc4d29d8bfc1e943d2c6e4cc",
}
SELECTED_CLASSES = {
    "net/minecraft/client/Minecraft",
    "net/minecraft/client/renderer/LevelRenderer",
    "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher",
    "net/minecraft/client/renderer/chunk/RenderRegionCache",
    "net/minecraft/client/renderer/chunk/SectionCompiler",
    "net/minecraft/client/renderer/chunk/SectionRenderDispatcher",
    "net/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask",
    "net/minecraft/client/renderer/entity/EntityRenderDispatcher",
    "net/minecraft/client/renderer/texture/TextureManager",
    "net/minecraft/client/resources/model/ModelManager",
    "net/minecraft/server/packs/resources/PreparableReloadListener",
    "net/minecraft/server/packs/resources/ReloadableResourceManager",
    "net/neoforged/neoforge/client/ClientHooks",
    "net/neoforged/neoforge/client/event/AddSectionGeometryEvent",
    "net/neoforged/neoforge/client/model/data/ModelDataManager",
}

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

def verify(archive):
    with open(archive,"rb") as f:
        data = f.read()
    assert sha(data)==ZIP_SHA256,(sha(data),ZIP_SHA256)
    content={}
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for suffix,expected in MEMBER_SHA256.items():
            candidates=[p for p in z.namelist() if p.rsplit("/",1)[-1]==PREFIX+suffix]
            assert len(candidates)==1,(suffix,candidates)
            raw=z.read(candidates[0])
            assert sha(raw)==expected,(suffix,sha(raw),expected)
            content[suffix]=raw.decode("utf-8")
    summary=dict(line.split("=",1) for line in content["summary.txt"].splitlines() if "=" in line)
    assert summary=={
        "target":"MC1.21.1-NeoForge21.1.215","stage":"3R-6",
        "classfiles":"15","methods":"656","invokes":"5126",
        "selectedInstructions":"1533",
        "mode":"original_selected_static_JVM_not_resource_reload_client_mod_or_GPU_runtime",
    },summary
    rows={s:list(csv.DictReader(io.StringIO(content[s]),delimiter="\t"))
          for s in ("owners.tsv","methods.tsv","calls.tsv","instructions.tsv")}
    assert {s:len(rows[s]) for s in rows}=={
        "owners.tsv":15,"methods.tsv":656,"calls.tsv":5126,"instructions.tsv":1533,
    }
    assert {x["owner"] for x in rows["owners.tsv"]}==SELECTED_CLASSES
    assert all(len(x["sha256"])==64 for x in rows["owners.tsv"])
    R="net/minecraft/client/renderer/"
    C="net/neoforged/neoforge/client/"
    pairs=[
        (R+"blockentity/BlockEntityRenderDispatcher","onResourceManagerReload",R+"blockentity/BlockEntityRenderers","createEntityRenderers"),
        (R+"chunk/SectionRenderDispatcher","dispose",R+"chunk/SectionRenderDispatcher","uploadAllPendingUploads"),
        (C+"ClientHooks","addAdditionalGeometry",C+"event/AddSectionGeometryEvent$AdditionalSectionRenderer","render"),
        (R+"chunk/SectionCompiler","compile",C+"ClientHooks","addAdditionalGeometry"),
    ]
    for src,method,target,called in pairs:
        matches=[c for c in rows["calls.tsv"] if c["owner"]==src and c["method"]==method
                and c["target"]==target and c["called"]==called]
        assert len(matches)==1,(src,method,target,called,len(matches))
    for owner,method in [
        ("net/minecraft/client/resources/model/ModelManager","reload"),
        (R+"texture/TextureManager","reload"),
        ("net/minecraft/server/packs/resources/ReloadableResourceManager","registerReloadListener"),
        (C+"model/data/ModelDataManager","snapshotSectionRegion"),
    ]:
        assert sum(x["owner"]==owner and x["method"]==method for x in rows["methods.tsv"])==1,(owner,method)
    print(json.dumps({
        "status":"PASS original NeoForge21.1.215 selected static JVM",
        "zip_sha256":ZIP_SHA256,
        "original_classes":15,"declared_methods":656,"direct_invokes":5126,
        "selected_ordered_instructions":1533,
        "verified_original_direct_call_paths":4,"verified_lifecycle_method_declarations":4,
        "limits":"Not a live reload, world switch, mod-loaded or GPU outcome",
    },ensure_ascii=False,indent=2))

if __name__=="__main__":
    a=argparse.ArgumentParser()
    a.add_argument("archive_zip")
    verify(a.parse_args().archive_zip)
