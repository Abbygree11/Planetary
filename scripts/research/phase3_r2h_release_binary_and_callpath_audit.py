#!/usr/bin/env python3
"""Planetary 3R-2h research-only: original JVM edge dispositions and released JAR ABI.

This is NOT a mod-loaded game integration test. No production code is modified.
Usage:
  python scripts/research/phase3_r2h_release_binary_and_callpath_audit.py calls old-ci.zip output-dir
  python scripts/research/phase3_r2h_release_binary_and_callpath_audit.py binaries output-dir
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import io
import json
from pathlib import Path
import subprocess
import time
from urllib.request import Request, urlopen
from zipfile import ZipFile

PREFIX = "phase3-neo1211-"
COLS = ("source_owner", "source_method", "source_jvm_descriptor",
        "target_owner", "target_method", "target_jvm_descriptor", "invoke_opcode")
AE2_DIGEST = "df15a07f86ca1ca93aa66373f9d5d84dd3d4586631fceb38f4fbc85dad591e39"
EXPECTED_ENGINE_SHA256 = "94aa7f868f428400b9f5539fba9af8107d69ba0aea9dcc56f9a8cd0eaf4bf161"


def read_edges(archive, stem):
    raw = archive.read(PREFIX + stem + ".tsv")
    return (list(csv.DictReader(io.StringIO(raw.decode("utf-8")), delimiter="\t")),
            hashlib.sha256(raw).hexdigest())


def exact(row):
    return tuple(row[k] for k in COLS)


def unrelated_offset(row):
    if row["target_method"] != "getOffset":
        return False
    return not (row["target_owner"].startswith("net/minecraft/world/level/block/state/")
                and "Lnet/minecraft/world/phys/Vec3;" in row["target_jvm_descriptor"])


def family(row):
    o = row["source_owner"]
    method = row["target_method"]
    if method == "getModelData":
        return "3B_MODEL_DATA", "BakedModel/BlockEntity/region ModelData must preserve source world and render producer"
    if o.startswith("net/minecraft/world/level/lighting/"):
        return "3B_LIGHT_TRANSMISSION", "Lighting remains physical; no blind block-local frame rewrite"
    if o.startswith("net/neoforged/neoforge/client/model/lighting/"):
        return "3B_AO_QUAD_LIGHT", "Quad normal, AO sampler and shape-facing frame are separate"
    if o.startswith("net/minecraft/world/level/levelgen/"):
        return "9_WORLDGEN_STRUCTURE", "Generation/schematic producer, separate from runtime Planet section renderer"
    if o.startswith("net/minecraft/world/level/material/"):
        return "2_FLUID_GEOMETRY", "Fluid permeability and face occlusion cannot be collapsed into render culling"
    if o.startswith("net/minecraft/world/level/pathfinder/") or o.startswith("net/minecraft/world/entity/"):
        return "7_ENTITY_PATH_COLLISION", "Entity pose, floor, collision context are not automatically block-local"
    if o.startswith("net/minecraft/client/particle/") or o.startswith("net/minecraft/client/multiplayer/ClientLevel"):
        return "3E_PARTICLE_CLIENT_EFFECT", "Shape calls by effects are not proof of block renderer pipeline"
    if o.startswith("net/minecraft/client/player/"):
        return "7_PLAYER_MOVEMENT", "Physical actor frame and contextual collision"
    if o.startswith("net/minecraft/client/renderer/chunk/"):
        return "3B_SECTION_COMPILE", "Worker/render region producer, model data and physical light"
    if o.startswith("net/minecraft/client/renderer/block/LiquidBlockRenderer"):
        return "3B_FLUID_RENDER", "Per-face opaque-fluid renderer consumes shapes, separate from fluid simulation"
    if o.startswith("net/minecraft/client/renderer/block/") or o.startswith("net/minecraft/client/resources/model/"):
        return "3B_BAKED_RENDER", "Quad and shape provenance separate; no global rotation of baked quads"
    if o.startswith("net/neoforged/neoforge/client/model/"):
        return "3B_NEOFORGE_MODEL_EXTENSION", "ModelData/quad providers may override vanilla pipeline"
    if o.startswith("net/minecraft/client/renderer/"):
        return "3E_CLIENT_RENDER_OTHER", "Selection/shadows/debug effects need own actor/render boundary"
    if o.startswith("net/minecraft/world/item/"):
        return "2_ITEM_PLACEMENT", "Item-specific hit/shape handling before canonical BlockState"
    if o.startswith("net/minecraft/server/"):
        return "7_SERVER_COLLISION_RESPAWN", "Server-side physical shape and spawn behavior"
    if o.startswith("net/minecraft/world/level/"):
        return "3A_WORLD_GEOMETRY", "Contextual shape/support query: preserve actual BlockGetter and position"
    if o.startswith("net/minecraft/client/renderer/debug/"):
        return "3E_DEBUG_RENDER", "Debug effects separate from actual world mesh"
    if o.startswith("net/neoforged/neoforge/"):
        return "3A_NEOFORGE_EXTENSION", "Dispatch through NeoForge extension may bypass vanilla callers"
    return "CROSS_PHASE_OTHER", "Cannot assert a geometry transformation contract from a matched name"


def generate_calls(zip_path: Path, output: Path):
    with ZipFile(zip_path) as z:
        engine, sha = read_edges(z, "engine-shape-consumer-edges")
        registered, _ = read_edges(z, "registered-shape-consumer-edges")
        summary = z.read(PREFIX + "engine-shape-consumer-summary.txt").decode()
    assert sha == EXPECTED_ENGINE_SHA256, sha
    assert len(engine) == 543 and len(registered) == 276
    assert "discoveredClassFiles=9430" in summary
    prior = {exact(row) for row in registered}
    current = {exact(row) for row in engine}
    new = [row for row in engine if exact(row) not in prior]
    assert len(new) == 273
    omitted = [row for row in registered if exact(row) not in current]
    assert len(omitted) == 6 and all(
        row["source_owner"] == "dev/planetary/client/render/PlanetBlockRenderCulling"
        for row in omitted)
    generic = [r for r in new if r["target_method"] == "getBlockEntity"]
    collisions = [r for r in new if unrelated_offset(r)]
    triage = [r for r in new
              if r["target_method"] != "getBlockEntity" and not unrelated_offset(r)]
    assert len(generic) == 114 and len(collisions) == 15 and len(triage) == 144
    output.mkdir(parents=True, exist_ok=True)
    dest = output / "phase3-r2h-144-new-original-consumer-dispositions.tsv"
    fields = list(COLS) + [
        "source_scope", "source_area", "provisional_owner_bucket", "coordinate_contract",
        "evidence", "gameplay_or_mod_acceptance"]
    with dest.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields, delimiter="\t")
        writer.writeheader()
        for row in triage:
            owner, contract = family(row)
            writer.writerow({**{k: row[k] for k in COLS},
                             "source_scope": row["source_scope"],
                             "source_area": row["source_area"],
                             "provisional_owner_bucket": owner,
                             "coordinate_contract": contract,
                             "evidence": "ORIGINAL_21_1_215_ASM_NAME_MATCHED",
                             "gameplay_or_mod_acceptance": "PENDING_SEMANTIC_DISPATCH"})
    grouped = {}
    for row in triage:
        owner, _ = family(row)
        grouped[owner] = grouped.get(owner, 0) + 1
    report = {
        "original_artifact": str(zip_path),
        "original_edge_sha256": sha,
        "original_3r2f_edges": len(registered),
        "original_3r2g_edges": len(engine),
        "new_exact_edges": len(new),
        "new_generic_blockentity_probes": len(generic),
        "new_unrelated_offset_name_matches": len(collisions),
        "new_provisional_owner_dispositions": len(triage),
        "previously_selected_planet_only_edges_not_in_this_original_jar_scan": len(omitted),
        "provisional_buckets": dict(sorted(grouped.items())),
        "warning": "Bucket is producer triage only. Method name does not prove semantic shape consumer or runtime dispatch.",
    }
    (output / "phase3-r2h-disposition-summary.json").write_text(
        json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2, ensure_ascii=False))


def fetch(url, path):
    for retry in range(3):
        try:
            with urlopen(Request(url, headers={"User-Agent": "Planetary-3R-2h-research/1.0"}),
                         timeout=60) as resp:
                path.write_bytes(resp.read())
            return
        except Exception:
            if retry == 2:
                raise
            time.sleep(2 * (retry + 1))


def javap(jar, klass):
    p = subprocess.run(["javap", "-s", "-p", "-classpath", str(jar), klass],
                       text=True, capture_output=True)
    if p.returncode != 0:
        raise AssertionError(f"javap {klass} failed: {p.stderr}")
    return p.stdout


def generate_binaries(output):
    output.mkdir(parents=True, exist_ok=True)
    maven = "https://maven.createmod.net"
    specs = [
        ("create-6.0.10-280",
         maven + "/com/simibubi/create/create-1.21.1/6.0.10-280/create-1.21.1-6.0.10-280-slim.jar",
         None,
         {
             "com.simibubi.create.content.contraptions.ContraptionWorld":
                 ["extends net.createmod.catnip.levelWrappers.WrappedLevel", "getBlockState"],
             "com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld":
                 ["extends net.minecraft.world.level.Level", "getBlockState"],
         }),
        ("ponder-neoforge-1.0.82+mc1.21.1",
         maven + "/net/createmod/ponder/ponder-neoforge/1.0.82+mc1.21.1/ponder-neoforge-1.0.82+mc1.21.1.jar",
         None,
         {"net.createmod.catnip.levelWrappers.WrappedLevel":
              ["extends net.minecraft.world.level.Level", "getLevel", "getBlockState"]}),
        ("ae2-neoforge-19.2.18",
         "https://github.com/AppliedEnergistics/Applied-Energistics-2/releases/download/"
         "neoforge/v19.2.18/appliedenergistics2-19.2.18.jar",
         AE2_DIGEST,
         {
             "appeng.client.render.model.GlassBakedModel":
                 ["implements net.neoforged.neoforge.client.model.IDynamicBakedModel",
                  "getModelData", "getQuads"],
             "appeng.client.render.cablebus.FacadeBlockAccess":
                 ["implements net.minecraft.world.level.BlockAndTintGetter", "getBlockState"],
             "appeng.client.render.cablebus.CableBusBakedModel":
                 ["getModelData", "getQuads"],
             "appeng.decorative.solid.QuartzGlassBlock":
                 ["skipRendering"],
             "appeng.decorative.solid.CertusQuartzClusterBlock":
                 ["getShape", "canSurvive"],
         }),
    ]
    matrix = []
    for name, url, pinned, classes in specs:
        jar = output / (name.replace("+", "_") + ".jar")
        fetch(url, jar)
        binary_sha = hashlib.sha256(jar.read_bytes()).hexdigest()
        if pinned is None:
            checksum_file = output / (jar.name + ".sha256")
            fetch(url + ".sha256", checksum_file)
            expected = checksum_file.read_text().strip().split()[0].lower()
        else:
            expected = pinned
        assert binary_sha == expected, f"{name}: SHA-256 mismatch"
        with ZipFile(jar) as z:
            entries = set(z.namelist())
            for klass, tokens in classes.items():
                path = klass.replace(".", "/") + ".class"
                assert path in entries, f"{name}: class not in released JAR: {path}"
                details = javap(jar, klass)
                for token in tokens:
                    assert token in details, f"{name}: {klass} missing {token}"
                (output / (name.replace("+", "_") + "__" + klass.split(".")[-1] + ".javap.txt"))\
                    .write_text(details, encoding="utf-8")
                matrix.append({
                    "component": name,
                    "binary_sha256": binary_sha,
                    "class": klass,
                    "method_or_type_checks": ",".join(tokens),
                    "status": "RELEASE_OR_RELEASE_EQUIVALENT_JAR_DECLARATION_VERIFIED",
                    "limit": "Not loaded in Planetary client; no method-body behavioral validation",
                })
    columns = ("component", "binary_sha256", "class", "method_or_type_checks", "status", "limit")
    with (output / "phase3-r2h-released-mod-class-abi.tsv").open(
            "w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=columns, delimiter="\t")
        w.writeheader()
        w.writerows(matrix)
    summary = {
        "jar_count": len(specs),
        "declared_classes_confirmed": len(matrix),
        "components": [x[0] for x in specs],
        "binary_sha256": {name: matrix[next(i for i, r in enumerate(matrix) if r["component"] == name)]
                          ["binary_sha256"] for name, _, _, _ in specs},
        "limit": "Declared JVM ABI only, not verified mod load, Mixins, client rendering or dynamic dispatch."
    }
    (output / "phase3-r2h-released-mod-summary.json").write_text(
        json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, indent=2))


def main():
    p = argparse.ArgumentParser()
    p.add_argument("command", choices=("calls", "binaries"))
    p.add_argument("path1", type=Path)
    p.add_argument("path2", type=Path, nargs="?")
    args = p.parse_args()
    if args.command == "calls":
        if args.path2 is None:
            p.error("calls requires ZIP and output directory")
        generate_calls(args.path1, args.path2)
    else:
        if args.path2 is not None:
            p.error("binaries requires only output directory")
        generate_binaries(args.path1)


if __name__ == "__main__":
    main()
