#!/usr/bin/env python3
"""3R-7F-2: official exact Flywheel 1.0.6 source/binary static probe; NOT gameplay."""
import hashlib
import io
import json
import re
import subprocess
import time
import urllib.request
import zipfile
from pathlib import Path

BASE = "https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/1.0.6/"
NAME = "flywheel-neoforge-1.21.1-1.0.6"
OUT = Path("build/reports/planetary/phase3-r7f2")
OWNERS = ("VisualizationManagerImpl", "VisualManagerImpl", "InstancedDrawManager",
          "IndirectDrawManager", "LightStorage", "InstanceTypes",
          "TransformedInstance", "PosedInstance", "OrientedInstance",
          "MatrixBuffer", "StagingBuffer")
SHADERS = ("instance/transformed.vert", "instance/posed.vert", "instance/oriented.vert")
WORDS = re.compile(r"normal|pose|matrix|transform|light|renderOrigin|delete|reset|upload|flush|recreate|queue|invalidate|fallback|GPU|instance", re.I)

def fetch(path):
    for attempt in range(3):
        try:
            req = urllib.request.Request(BASE + path, headers={"User-Agent": "Planetary-source-audit/1.0"})
            with urllib.request.urlopen(req, timeout=60) as response:
                return response.read()
        except Exception:
            if attempt == 2:
                raise
            time.sleep(3 * (attempt + 1))

def checksum(data):
    return hashlib.sha256(data).hexdigest()

def release(suffix):
    file = NAME + suffix + ".jar"
    data = fetch(file)
    expected = fetch(file + ".sha256").decode("ascii").strip().split()[0].lower()
    assert re.fullmatch("[0-9a-f]{64}", expected), "Invalid official SHA256 sidecar"
    assert checksum(data) == expected, "Published checksum mismatch: " + file
    return data, expected

def select(files, suffix):
    matches = [p for p in files if p.endswith(suffix)]
    assert len(matches) == 1, "Expected one entry ending " + suffix + " got " + str(matches)
    return matches[0]

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    binary, binary_hash = release("")
    source, source_hash = release("-sources")
    jb, js = zipfile.ZipFile(io.BytesIO(binary)), zipfile.ZipFile(io.BytesIO(source))
    entry_rows = ["kind\tname\tentry\tsha256\tbytes"]
    source_rows = ["owner\tentry\tline\ttext"]
    shader_rows = ["resource\tline\ttext"]
    chosen = []
    for owner in OWNERS:
        compiled = select(jb.namelist(), "/" + owner + ".class")
        java = select(js.namelist(), "/" + owner + ".java")
        chosen.append(compiled)
        for kind, jar, path in (("compiled", jb, compiled), ("source", js, java)):
            data = jar.read(path)
            entry_rows.append("\t".join([kind, owner, path, checksum(data), str(len(data))]))
        for i, line in enumerate(js.read(java).decode("utf-8").splitlines(), 1):
            if WORDS.search(line):
                source_rows.append("\t".join([owner, java, str(i), line.strip().replace("\t", " ")[:300]]))
    for resource in SHADERS:
        path = select(jb.namelist(), "/" + resource)
        data = jb.read(path)
        entry_rows.append("\t".join(["shader", resource, path, checksum(data), str(len(data))]))
        for i, line in enumerate(data.decode("utf-8").splitlines(), 1):
            shader_rows.append("\t".join([path, str(i), line.strip().replace("\t", " ")]))
    (OUT / "selected-entries.tsv").write_text("\n".join(entry_rows) + "\n")
    (OUT / "source-owner-traces.tsv").write_text("\n".join(source_rows) + "\n")
    (OUT / "shader-traces.tsv").write_text("\n".join(shader_rows) + "\n")
    jar_path = OUT / (NAME + ".jar")
    jar_path.write_bytes(binary)
    try:
        traces = []
        for path in chosen:
            klass = path[:-6].replace("/", ".")
            p = subprocess.run(["javap", "-private", "-c", "-s", "-classpath", str(jar_path), klass],
                               check=True, capture_output=True, text=True, timeout=90)
            traces.append("===== " + klass + " =====\n" + p.stdout)
        (OUT / "compiled-javap.txt").write_text("\n".join(traces))
    finally:
        jar_path.unlink(missing_ok=True)
    summary = {
        "stage": "3R-7F-2", "mode": "exact-published-jar-static-source-abi-NOT-game",
        "maven": BASE, "binary_sha256_verified": binary_hash, "source_sha256_verified": source_hash,
        "binary_entries": len(jb.namelist()), "source_entries": len(js.namelist()),
        "compiled_and_source_owners": len(chosen), "shader_resources": len(SHADERS),
        "target_neoforge": "21.1.215", "create": "6.0.10",
        "remaining": "live Create with NeoForge 21.1.215, GPU shader runtime, world reload, 6/12/8 visual gate"
    }
    (OUT / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps(summary, indent=2))
    print("Static official release verified; live mod and graphics remain OPEN")

if __name__ == "__main__":
    main()
