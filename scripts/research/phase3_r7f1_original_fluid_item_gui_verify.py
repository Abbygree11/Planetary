#!/usr/bin/env python3
"""Verify the pinned successful 3R-7F-1 original 21.1.215 CI artifact.

python scripts/research/phase3_r7f1_original_fluid_item_gui_verify.py <ci_zip>
Source static call proof only: no Minecraft client, mods, GUI, shader or GPU PASS.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA = "7b7981ebadc46f6cb26571ac499e99eb70fd13cdec96dee2de792a5f04ba0325"
FILE_SHA = {
    "owners.tsv":"b1966d2b4f142973b69abd4ab4c39cbe8c29ad7bc9c0ee2c9e5404c9114ab9ac",
    "methods.tsv":"39bcefb97927adc6d0022f9fafc6d02887a9b2a095b70d6f1764aaa1145100ac",
    "calls.tsv":"e67a08737b16cbb23a5b88910dde5b075af242d463a6fb7786e988690cb607de",
    "instructions.tsv":"5b48d6a1feadd1df470d4c70a41c40b37998885f2e476968ee1fb50010fc9682",
    "summary.txt":"f513d3ab838b374e41fc36728b41a655781c81f920e05a65b176bcce9b1f27f9"
}
PREFIX = "phase3-r7f1-original-fluid-item-gui-"
EXPECTED = [
    "net/minecraft/client/renderer/BlockEntityWithoutLevelRenderer",
    "net/minecraft/client/renderer/ItemInHandRenderer",
    "net/minecraft/client/renderer/block/BlockRenderDispatcher",
    "net/minecraft/client/renderer/block/LiquidBlockRenderer",
    "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher",
    "net/minecraft/client/renderer/chunk/SectionCompiler",
    "net/minecraft/client/renderer/entity/ItemEntityRenderer",
    "net/minecraft/client/renderer/entity/ItemRenderer",
    "net/minecraft/world/level/material/Fluid",
    "net/minecraft/world/level/material/FluidState",
    "net/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions",
    "net/neoforged/neoforge/client/extensions/common/IClientItemExtensions",
]

def sha(b):
    return hashlib.sha256(b).hexdigest()

def verify(path):
    with open(path,"rb") as f:
        raw=f.read()
    assert sha(raw)==ZIP_SHA, (sha(raw),ZIP_SHA)
    contents={}
    with zipfile.ZipFile(io.BytesIO(raw)) as z:
        for suffix,digest in FILE_SHA.items():
            matches=[n for n in z.namelist() if n.rsplit("/",1)[-1]==PREFIX+suffix]
            assert len(matches)==1,(suffix,matches)
            b=z.read(matches[0])
            assert sha(b)==digest,(suffix,sha(b),digest)
            contents[suffix]=b.decode("utf-8")
    summary=dict(line.split("=",1) for line in contents["summary.txt"].splitlines() if "=" in line)
    assert summary=={
        "target":"MC1.21.1-NeoForge21.1.215","stage":"3R-7F-1",
        "classfiles":"12","methods":"191","invokes":"1040",
        "selectedInstructions":"2240",
        "mode":"selected_original_static_JVM_not_game_mod_or_GUI_runtime"
    },summary
    rows={suffix:list(csv.DictReader(io.StringIO(contents[suffix]),delimiter="\t"))
          for suffix in ["owners.tsv","methods.tsv","calls.tsv","instructions.tsv"]}
    assert [len(rows[s]) for s in ["owners.tsv","methods.tsv","calls.tsv","instructions.tsv"]] == [12,191,1040,2240]
    assert [r["owner"] for r in rows["owners.tsv"]]==EXPECTED
    assert all(len(r["sha256"])==64 for r in rows["owners.tsv"])
    paths=[
        ("BlockRenderDispatcher","renderLiquid","IClientFluidTypeExtensions","renderFluid"),
        ("BlockRenderDispatcher","renderLiquid","LiquidBlockRenderer","tesselate"),
        ("ItemRenderer","render","IClientItemExtensions","getCustomRenderer"),
        ("ItemRenderer","render","BlockEntityWithoutLevelRenderer","renderByItem"),
        ("ItemRenderer","render","BakedModel","getRenderPasses"),
        ("ItemRenderer","render","BakedModel","getRenderTypes"),
        ("ItemRenderer","render","IClientItemExtensions","of"),
        ("BlockRenderDispatcher","renderSingleBlock","IClientItemExtensions","getCustomRenderer"),
        ("BlockRenderDispatcher","renderSingleBlock","BlockEntityWithoutLevelRenderer","renderByItem"),
    ]
    for owner,method,target,called in paths:
        n=sum(r["owner"].split("/")[-1]==owner and r["method"]==method
              and r["target"].split("/")[-1]==target and r["called"]==called for r in rows["calls.tsv"])
        assert n==1,(owner,method,target,called,n)
    for owner,name in [
        ("LiquidBlockRenderer","tesselate"),
        ("LiquidBlockRenderer","shouldRenderFace"),
        ("IClientFluidTypeExtensions","renderFluid"),
        ("IClientItemExtensions","getCustomRenderer"),
        ("BlockEntityWithoutLevelRenderer","renderByItem")
    ]:
        assert any(r["owner"].split("/")[-1]==owner and r["method"]==name for r in rows["methods.tsv"]), (owner,name)
    print(json.dumps({
        "result":"PASS original target static-JVM artifact only",
        "zip_sha256":ZIP_SHA,
        "classfiles":12,"methods":191,"invokes":1040,"selected_instructions":2240,
        "selected_exact_direct_original_calls":len(paths),
        "research":"G01 B05 + G02 C05 bounded source owner",
        "not_proven":"loaded mods, fluid visuals, GUI/noLevel behavior, shader, GPU or game V",
    },ensure_ascii=False,indent=2))

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("artifact_zip")
    verify(p.parse_args().artifact_zip)
