#!/usr/bin/env python3
"""Verify Phase 3R-2b 241-class original JVM shape/collision owner census.

Inputs: immutable original NeoForge 21.1.215 registry ZIP and the committed
PHASE3_R2B_ORIGINAL_EFFECTIVE_SHAPE_OWNERS_1_21_1.tsv. This is NOT a game,
Mixin, modded runtime or complete Phase 3 shape-API verification.
"""
import csv
import hashlib
import io
import sys
import zipfile
from pathlib import Path

ZIP_SHA = "7937deee9221c2032a634d8355b8118b47efd9d905b2bb64152148c0174d090e"
CANONICAL_SHA = "2998ea5f3eb04745aa12cdedd7a1ea2128987e8d7c6b43da56f47cc1b1e9068b"
CANONICAL_FNV64 = 0xC04F6D13ECD62939
REGISTRY = "phase2-neo1211-block-registry.tsv"
EXPECTED_PARAMS = (
    "net.minecraft.world.level.block.state.BlockState,"
    "net.minecraft.world.level.BlockGetter,"
    "net.minecraft.core.BlockPos,"
    "net.minecraft.world.phys.shapes.CollisionContext"
)


def effective_owner(record: dict[str, str], method: str) -> str:
    key = method + "="
    parts = [x.removeprefix(key) for x in record["effective_method_owners"].split("|")
             if x.startswith(key)]
    assert len(parts) == 1, (record["registry_id"], method, parts)
    signature = parts[0]
    assert signature.endswith("(" + EXPECTED_PARAMS + ")"), (method, signature)
    return signature.split("(", 1)[0].rsplit(".", 1)[-1]


def source_rows(zip_path: Path) -> dict[str, tuple[int, str, str, str]]:
    actual_sha = hashlib.sha256(zip_path.read_bytes()).hexdigest()
    assert actual_sha == ZIP_SHA, ("wrong immutable artifact", actual_sha)
    data: dict[str, list] = {}
    with zipfile.ZipFile(zip_path) as archive:
        original = list(csv.DictReader(
            io.StringIO(archive.read(REGISTRY).decode("utf-8")), delimiter="\t"))
    assert len(original) == 1060
    for record in original:
        name = record["java_class"]
        hierarchy = record["class_hierarchy"]
        shape = effective_owner(record, "getShape")
        collision = effective_owner(record, "getCollisionShape")
        if name not in data:
            data[name] = [0, hierarchy, shape, collision]
        else:
            assert data[name][1:] == [hierarchy, shape, collision], record["registry_id"]
        data[name][0] += 1
    assert len(data) == 241
    assert sum(r[0] for r in data.values()) == 1060
    for name, row in data.items():
        chain = row[1].split(">")
        assert chain[0] == name.rsplit(".", 1)[-1]
        assert row[2] in chain and row[3] in chain
        assert chain[-1] == "BlockBehaviour"
    return {name: tuple(values) for name, values in data.items()}


def canonical(original: dict[str, tuple[int, str, str, str]]) -> bytes:
    result = "".join("|".join((name, str(n), hierarchy, shape, collision)) + "\n"
                     for name, (n, hierarchy, shape, collision)
                     in sorted(original.items()))
    return result.encode("utf-8")


def fnv64(data: bytes) -> int:
    value = 0xCBF29CE484222325
    for byte in data:
        value = ((value ^ byte) * 0x100000001B3) & 0xFFFFFFFFFFFFFFFF
    return value


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit(
            "Usage: phase3_shape_effective_owner_verify.py original-registry.zip "
            "PHASE3_R2B_...tsv")
    original = source_rows(Path(sys.argv[1]))
    stream = canonical(original)
    assert hashlib.sha256(stream).hexdigest() == CANONICAL_SHA
    assert fnv64(stream) == CANONICAL_FNV64
    with Path(sys.argv[2]).open(encoding="utf-8", newline="") as reader:
        actual_rows = list(csv.DictReader(reader, delimiter="\t"))
    assert len(actual_rows) == 241
    assert len({r["java_class"] for r in actual_rows}) == 241
    for row in actual_rows:
        name = row["java_class"]
        assert name in original, name
        n, hierarchy, shape, collision = original[name]
        expected = (str(n), hierarchy, shape, collision)
        actual = (row["registered_block_ids"],
                  row["original_exact_class_hierarchy"],
                  row["getShape_effective_owner"],
                  row["getCollisionShape_effective_owner"])
        assert actual == expected, (name, actual, expected)
        assert row["original_exact_compiled_reflection_getShape_getCollision"] == (
            "ORIGINAL_21_1_215_JVM_REFLECTION_EXACT_DECLARING_OWNER")
    print("PASS original ZIP verified SHA-256, 1060 ID/241 class signatures + inheritance")
    print("PASS canonical SHA-256", CANONICAL_SHA, "FNV-64", hex(CANONICAL_FNV64))
    print("PASS TSV 241/241 nearest original JVM declaring owners (2 exact methods)")
    print("OPEN remaining shape APIs compiled owners, call sites, mods and gameplay")


if __name__ == "__main__":
    main()
