#!/usr/bin/env python3
"""Pin and verify exact NeoForge21.1.215 3R-5 original JVM CI archive.

Usage: python scripts/research/phase3_r5_original_section_geometry_verify.py <original_ci_zip>
Only selected source/classfile static evidence. Does not load Create/Flywheel/Iris or Minecraft client.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "acda0f019fa2cf9c45118d54d1e1ccdc540a013189d61fb35a32210a1d47c508"
PREFIX = "phase3-r5-original-independent-render-"
FILES = {
    "owners.tsv": "0ec46d065d263bddb42c85247b9cd9de9f916c316d4df4a6608e13d7f690f5e3",
    "methods.tsv": "35291d0e084eff637479e111273de42e59e005e2d1bbaa40901c80c66f95ebda",
    "calls.tsv": "d57bfa0723eb72e42fa78747b0c41d387efbc00f58e271effe130780a50c018f",
    "instructions.tsv": "aae9f4db1f58ecb5dba4ff906e9e486e1026b617259cccf18bb87d452752691a",
    "summary.txt": "20b9de0d7298c8898eab78a0edaa4c1668b306aa8496ebe9821a0001dc8b9512",
}
EXPECTED = {
    "net/minecraft/client/renderer/LevelRenderer",
    "net/minecraft/client/renderer/block/BlockRenderDispatcher",
    "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher",
    "net/minecraft/client/renderer/chunk/SectionCompiler",
    "net/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection",
    "net/minecraft/client/renderer/entity/EntityRenderDispatcher",
    "net/neoforged/neoforge/client/ClientHooks",
    "net/neoforged/neoforge/client/event/AddSectionGeometryEvent",
    "net/neoforged/neoforge/client/event/AddSectionGeometryEvent$AdditionalSectionRenderer",
    "net/neoforged/neoforge/client/event/AddSectionGeometryEvent$SectionRenderingContext",
}

def digest(b):
    return hashlib.sha256(b).hexdigest()

def verify(path):
    with open(path,"rb") as fh:
        full = fh.read()
    assert digest(full) == ZIP_SHA, (digest(full),ZIP_SHA)
    content = {}
    with zipfile.ZipFile(io.BytesIO(full)) as z:
        for suffix, expected in FILES.items():
            found = [p for p in z.namelist() if p.rsplit("/",1)[-1] == PREFIX+suffix]
            assert len(found) == 1,(suffix,found)
            raw=z.read(found[0])
            assert digest(raw)==expected,(suffix,digest(raw),expected)
            content[suffix]=raw.decode("utf-8")
    summary=dict(line.split("=",1) for line in content["summary.txt"].splitlines() if "=" in line)
    required = {
        "target":"MC1.21.1-NeoForge21.1.215",
        "stage":"3R-5","classfiles":"10","methods":"337","invokes":"3228",
        "selectedInstructions":"2084",
        "mode":"original_static_JVM_and_selected_section_producer_edges_not_Flywheel_Iris_client",
    }
    for k,v in required.items():
        assert summary.get(k)==v,(k,summary.get(k),v)
    tables={s:list(csv.DictReader(io.StringIO(content[s]),delimiter="\t"))
            for s in ("owners.tsv","methods.tsv","calls.tsv","instructions.tsv")}
    assert [len(tables[s]) for s in ("owners.tsv","methods.tsv","calls.tsv","instructions.tsv")] == [10,337,3228,2084]
    owners={row["owner"]:row["sha256"] for row in tables["owners.tsv"]}
    assert owners.keys()==EXPECTED
    assert all(len(h)==64 for h in owners.values())
    calls=tables["calls.tsv"]
    checks=[
        ("ClientHooks","gatherAdditionalRenderers","AddSectionGeometryEvent","<init>"),
        ("ClientHooks","addAdditionalGeometry","AddSectionGeometryEvent$SectionRenderingContext","<init>"),
        ("ClientHooks","addAdditionalGeometry","AddSectionGeometryEvent$AdditionalSectionRenderer","render"),
        ("SectionCompiler","compile","ClientHooks","addAdditionalGeometry"),
        ("SectionRenderDispatcher$RenderSection","createCompileTask","ClientHooks","gatherAdditionalRenderers"),
        ("AddSectionGeometryEvent","addRenderer","List","add"),
    ]
    for src,method,dst,target in checks:
        number=sum(c["owner"].split("/")[-1]==src and c["method"]==method
                   and c["target"].split("/")[-1]==dst and c["called"]==target for c in calls)
        assert number==1,(src,method,dst,target,number)
    methods=tables["methods.tsv"]
    for o,m in [
        ("AddSectionGeometryEvent","getLevel"),
        ("AddSectionGeometryEvent$SectionRenderingContext","getRegion"),
        ("AddSectionGeometryEvent$SectionRenderingContext","getPoseStack"),
        ("AddSectionGeometryEvent$SectionRenderingContext","getOrCreateChunkBuffer")
    ]:
        assert sum(x["owner"].split("/")[-1]==o and x["method"]==m for x in methods)==1,(o,m)
    print(json.dumps({
        "verification":"PASS original target STATIC JVM ONLY",
        "original_ci_archive_sha256":ZIP_SHA,"original_classes":10,
        "methods":337,"invokes":3228,"ordered_selected_instructions":2084,
        "original_exact_nested_call_paths":len(checks),
        "limitations":"No loaded custom renderer, Flywheel binary, Iris shader or Minecraft visual"
    },indent=2))

if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("artifact_zip");verify(p.parse_args().artifact_zip)
