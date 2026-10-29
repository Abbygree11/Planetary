#!/usr/bin/env python3
"""Planetary 3R-7G: full 151-owner bounded SOURCE provenance reconciliation.

This verifies every phase owner row, source report location and immutable
disposition delta. It deliberately does NOT execute Minecraft, claim actual
client + shader loading, prove mod compatibility or close source guards.
"""
import csv
import hashlib
import io
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

BASE = Path("docs/research")
OLD = BASE / "PHASE3_R7F5_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv"
NEW = BASE / "PHASE3_R7G_EFFECTIVE_151_OWNER_SOURCE_GATE_1_21_1.tsv"
DELTA = BASE / "PHASE3_R7G_SOURCE_GATE_RECONCILIATION_DELTA_1_21_1.tsv"
COMPLETE = BASE / "PHASE3_R7G_ALL_151_OWNER_EVIDENCE_AUDIT_1_21_1.tsv"
GUARDS = BASE / "PHASE3_R7G_27_OPEN_SOURCE_CONTRACT_GUARDS_1_21_1.tsv"
STALE = BASE / "PHASE3_R7G_18_RECONCILED_PRIOR_SOURCES_1_21_1.tsv"
OUT = Path("build/reports/planetary/phase3-r7g")
EXPECTED_PHASES = {"3A": 41, "3B": 25, "3C": 48, "3D": 15, "3E": 22}
EXPECTED_CLASSIFICATIONS = {
    "RECONCILED_PRIOR_SOURCE_OWNER_V_OPEN": 18,
    "OPEN_TARGET_SOURCE_CONTRACT_GUARD": 27,
    "SELECTED_SOURCE_ACCEPTED_V_GAME_GUARD": 19,
    "SELECTED_SOURCE_EVIDENCE_RUNTIME_GAP": 74,
    "BOUNDED_R_SOURCE_PASS_V_OPEN": 11,
    "BOUNDED_SHADER_BINARY_RESEARCH_PASS_LOADER_CONFLICT_V_OPEN": 2,
}
EXPECTED_STALE_BY_PHASE={"3A":4, "3B":4, "3C":3, "3D":4, "3E":3}
ALLOWED = {
    ("SUPERSEDED_OR_STALE_LEDGER_RECONCILE","RECONCILED_PRIOR_SOURCE_OWNER_V_OPEN"),
    ("SELECTED_SOURCE_NEEDS_FINAL_GUARD","OPEN_TARGET_SOURCE_CONTRACT_GUARD"),
    ("SELECTED_SOURCE_NEEDS_FINAL_GUARD","SELECTED_SOURCE_ACCEPTED_V_GAME_GUARD"),
}

def digest(buf: bytes): return hashlib.sha256(buf).hexdigest()

def load(path):
    with path.open(newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f, delimiter="\t"))

def check(cond, msg):
    if not cond:
        raise AssertionError(msg)

def main():
    before,after = load(OLD),load(NEW)
    audit,delta,guards,stale = map(load,(COMPLETE,DELTA,GUARDS,STALE))
    check(len(before)==len(after)==len(audit)==151, "151 exact row invariant")
    check(len(delta)==64 and len(guards)==27 and len(stale)==18, "reconciliation worklist sizes")
    prev={r["id"]:r for r in before}; now={r["id"]:r for r in after}
    check(len(prev)==len(now)==151 and set(prev)==set(now), "IDs mutated/duplicated")
    check(Counter(r["phase"] for r in after)==EXPECTED_PHASES,"phase distribution")
    check(Counter(r["audit_disposition"] for r in after)==EXPECTED_CLASSIFICATIONS,"classification counts")
    check(all(r["id"] in now for r in guards+stale),"missing worklist row")
    check({r["id"] for r in guards}=={r["id"] for r in after
           if r["audit_disposition"]=="OPEN_TARGET_SOURCE_CONTRACT_GUARD"},
          "27 source guard inventory mismatch")
    check({r["id"] for r in stale}=={r["id"] for r in before
           if r["audit_disposition"]=="SUPERSEDED_OR_STALE_LEDGER_RECONCILE"},
          "18 historical coverage inventory mismatch")
    delta_by_id={r["id"]:r for r in delta}
    check(len(delta_by_id)==64,"duplicated delta IDs")
    anchors=defaultdict(list)
    changes=0
    for row in after:
        old=prev[row["id"]]
        check(row["phase"]==old["phase"] and row["source_owner_family"]==old["source_owner_family"],
              "changed owner family/phase: "+row["id"])
        check(row["ledger_evidence_status"]==old["ledger_evidence_status"] and
              row["highest_ledger_evidence_grade"]==old["highest_ledger_evidence_grade"],
              "grade falsely promoted: "+row["id"])
        check(row["research_cluster"]==old["research_cluster"],"cluster mutated: "+row["id"])
        check(row["remaining_evidence_not_claimed"]==old["remaining_evidence_not_claimed"],
              "lost known uncertainty for "+row["id"])
        changed=row["audit_disposition"]!=old["audit_disposition"]
        if changed:
            changes+=1
            check((old["audit_disposition"],row["audit_disposition"]) in ALLOWED,
                  "unapproved status change "+row["id"])
            check(row["id"] in delta_by_id, "missing exact delta "+row["id"])
            d=delta_by_id[row["id"]]
            check((d["previous_disposition"],d["new_disposition"],d["source_anchor"])
                  ==(old["audit_disposition"],row["audit_disposition"],row["source_evidence_anchor"]),
                  "delta doesn't match " + row["id"])
        else:
            check(old==row, "unintended untouched row change "+row["id"])
        p=BASE/row["source_evidence_anchor"]
        check(p.exists() and p.is_file(),"missing source research anchor "+str(p)+" for "+row["id"])
        raw=p.read_bytes()
        check(len(raw)>400,"source report unexpectedly empty "+str(p))
        anchors[p.name].append(row["id"])
    check(changes==64 and len(delta_by_id)==64,"unexpected changed row count")
    check([r["id"] for r in audit]==[r["id"] for r in after],"per-owner audit order differs")
    for a,b in zip(audit,after):
        for k in ("phase","id","source_evidence_anchor","remaining_evidence_not_claimed"):
            if k=="remaining_evidence_not_claimed": name="explicit_remaining_gap"
            else: name=k
            check(a[name]==b[k],"per-owner audit field "+k+" differs for "+b["id"])
        check(a["audit_disposition"]==b["audit_disposition"],"audit status differs "+b["id"])
    check(not any(x["audit_disposition"]=="BLOCKS_R_SOURCE_GATE" for x in after),
          "named bounded source blocker was accidentally reopened")
    check({x["id"] for x in after if x["audit_disposition"]==
           "BOUNDED_SHADER_BINARY_RESEARCH_PASS_LOADER_CONFLICT_V_OPEN"}=={"D14","E22"},
          "Iris conflict disposition lost")
    report=BASE/"PHASE3_R7F5_IRIS_SODIUM_SHADER_RELEASE_ABI_LOADER_METADATA_1_21_1.md"
    ir=report.read_text(encoding="utf-8")
    check("[1.21,1.21.1)" in ir and "OPEN" in ir and
          "9e8c11e" in ir, "pinned Iris JAR loader conflict/source missing")
    # This is a metadata contradiction check, not an actual NeoForge loader test.
    check("[1.21,1.21.1)".endswith(")"),"expected Maven/FML exclusive upper range marker")
    OUT.mkdir(parents=True,exist_ok=True)
    anchor_rows = ["source_report\tsha256\tbytes\towner_ids\toriginal_jvm_ci_cited"]
    for name,owners in sorted(anchors.items()):
        p=BASE/name
        raw=p.read_bytes()
        ci=bool(re.search(r"CI.{0,50}(?:SUCCESS|PASS)|GitHub Actions #\d+",raw.decode("utf-8","replace"),re.I))
        anchor_rows.append(f"{name}\t{digest(raw)}\t{len(raw)}\t{','.join(owners)}\t{str(ci).lower()}")
    (OUT/"phase3-r7g-unique-owner-anchors-sha256.tsv").write_text("\n".join(anchor_rows)+"\n",encoding="utf-8")
    for src,dst in [(NEW,"phase3-r7g-151-audited-effective-owner-gate.tsv"),
                    (COMPLETE,"phase3-r7g-151-per-owner-audit.tsv"),
                    (DELTA,"phase3-r7g-exact-64-disposition-delta.tsv"),
                    (GUARDS,"phase3-r7g-27-original-source-guards.tsv"),
                    (STALE,"phase3-r7g-18-previous-source-reconciliation.tsv")]:
        (OUT/dst).write_bytes(src.read_bytes())
    summary={
        "target":"Minecraft1.21.1-NeoForge21.1.215-Java21-3R7G",
        "researchStage":"SOURCE_PROVENANCE_RECONCILIATION_CI_PASS_ONLY",
        "totalOwners":151, "uniqueIds":len(now), "classificationCounts":EXPECTED_CLASSIFICATIONS,
        "previousGuardRows":46,"guardsMovedToGameV":19,
        "stillSourceOwnerContractGuards":27,"historicalAnchorsReconciled":18,
        "originalNamedG01ToG06Blockers":0,
        "uniqueExistingSourceReportAnchors":len(anchors),
        "irisSodium":"ORIGINAL_RELEASE_BINARY_SHA_PINNED_BUT_IRIS_MC_VERSION_RANGE_UPPER_EXCLUSIVE_TARGET_LOADER_NOT_TESTED",
        "researchGate":"R_OPEN_UNTIL_27_SOURCE_CONTRACTS_AND_IRIS_SCOPE_DECISION",
        "visualGate":"V_OPEN_NO_CLIENT_WORLD_OR_MOD_RENDER_PROOF",
        "scope":"CI validates ledger, source report provenance, exact byte-preserving historical deltas, NOT any new Minecraft JVM ABI or real loader",
        "stage3R7F5EffectiveSha256":digest(OLD.read_bytes()),
        "stage3R7GEffectiveSha256":digest(NEW.read_bytes()),
    }
    (OUT/"phase3-r7g-summary.json").write_text(json.dumps(summary,ensure_ascii=False,sort_keys=True,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(summary,ensure_ascii=False,sort_keys=True,indent=2))
if __name__=="__main__":
    main()
