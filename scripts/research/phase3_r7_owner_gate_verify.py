#!/usr/bin/env python3
"""Verify original GitHub Actions 3R-7 phase-wide owner-gate integrity archive.

Usage: python scripts/research/phase3_r7_owner_gate_verify.py <ci_zip>
This checks a research ledger, NOT NeoForge classfile ABI, Minecraft client,
third-party GPU rendering, applied Mixins or any live gameplay.
"""
import argparse
import csv
import hashlib
import io
import json
import zipfile

ZIP_SHA256 = "62cd8db21f3292732a6ae8e388e5a7d06de7d1800ed02e0dc2a2beb8542b4a7d"
REPORT_SHA256 = {
    "phase3-r7-blocking-source-groups.tsv":
        "8bd9eee72477b9e32ee3643e4b414788702703da6276a9da2bbf7e7d77fa8ef0",
    "phase3-r7-whole-phase-owner-gate-summary.txt":
        "102608ea9239b28e3761fb3d30d4b20447625b09a0d9358e488b48eed191e240",
}
SOURCE_LEDGER_SHA256 = "b6ea163a9b996f5873112f9a80a433db2cf18ae354996baa9cda8367849124aa"
SOURCE_DISPOSITION_SHA256 = "696ab69681278c832b8b9840f6ecd68d197de766b1b7c083cec2440e180a10a9"
GROUPS = {
    "G01": {"B05"},
    "G02": {"C05"},
    "G03": {"A17", "A35", "B11"},
    "G04": {"A25"},
    "G05": {"D10", "D11", "E20"},
    "G06": {"E03", "E10"},
}
SUMMARY = {
    "target": "PlanetaryPhase3R7ResearchIntegrityNotMinecraftOriginalJVM",
    "status": "PASS_RECORD_RECONCILIATION_ONLY",
    "researchGate": "R_OPEN",
    "visualGate": "V_OPEN",
    "phase3Rows": "151", "auditRows": "151",
    "3A": "41", "3B": "25", "3C": "48", "3D": "15", "3E": "22",
    "BLOCKS_R_SOURCE_GATE": "11",
    "OPTIONAL_SHADER_SOURCE_SCOPE_UNVERIFIED": "2",
    "SELECTED_SOURCE_EVIDENCE_RUNTIME_GAP": "74",
    "SELECTED_SOURCE_NEEDS_FINAL_GUARD": "46",
    "SUPERSEDED_OR_STALE_LEDGER_RECONCILE": "18",
    "sixSourceBlockerClusters": "6",
    "optionalShaderOwnerRows": "2",
    "sourceLedgerSHA256": SOURCE_LEDGER_SHA256,
    "sourceDispositionSHA256": SOURCE_DISPOSITION_SHA256,
}

def sha(b):
    return hashlib.sha256(b).hexdigest()

def verify(archive):
    with open(archive, "rb") as f:
        raw = f.read()
    assert sha(raw) == ZIP_SHA256, (sha(raw), ZIP_SHA256)
    files={}
    with zipfile.ZipFile(io.BytesIO(raw)) as z:
        for name, expected in REPORT_SHA256.items():
            found=[n for n in z.namelist() if n.rsplit("/", 1)[-1] == name]
            assert len(found) == 1, (name, found)
            data=z.read(found[0])
            assert sha(data) == expected, (name, sha(data), expected)
            files[name]=data.decode("utf-8")
    summary=dict(x.split("=",1) for x in files["phase3-r7-whole-phase-owner-gate-summary.txt"].splitlines() if "=" in x)
    assert summary == SUMMARY, summary
    blockers=list(csv.DictReader(io.StringIO(files["phase3-r7-blocking-source-groups.tsv"]),delimiter="\t"))
    assert len(blockers)==11,len(blockers)
    found={}
    for x in blockers:
        found.setdefault(x["cluster"],set()).add(x["id"])
    assert found==GROUPS,(found,GROUPS)
    assert all(x["phase"] in {"3A","3B","3C","3D","3E"} and x["owner_family"] for x in blockers)
    print(json.dumps({
        "result":"PASS research-ledger-integrity ONLY",
        "phase3_owner_rows":151,
        "six_source_blocker_clusters":len(found),
        "blocking_source_rows":len(blockers),
        "research_gate":"R_OPEN","game_visual_gate":"V_OPEN",
        "ci_archive_sha256":ZIP_SHA256,
        "original_ledger_sha256":SOURCE_LEDGER_SHA256,
        "original_source_disposition_sha256":SOURCE_DISPOSITION_SHA256,
        "limits":"Not original Minecraft bytecode CI or mod/client visual acceptance",
    },ensure_ascii=False,indent=2))

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("artifact_zip")
    verify(p.parse_args().artifact_zip)
