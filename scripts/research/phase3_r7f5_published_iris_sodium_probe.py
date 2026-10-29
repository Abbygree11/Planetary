#!/usr/bin/env python3
"""Planetary 3R-7F-5: original published Iris/Sodium NeoForge1.21.1 binary
source-ownership and shader resource metadata. NO installed client / GPU test."""
from __future__ import annotations
import csv
import hashlib
import json
import re
import subprocess
import sys
import time
import urllib.request
from pathlib import Path
from zipfile import ZipFile

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/reports/planetary/phase3-r7f5")
CACHE = Path("build/phase3-r7f5-published-jars")
SPECS = [
    ("iris", "t3ruzodq", "YL57xq9U", "iris-neoforge-1.8.12+mc1.21.1.jar",
     "a3e6355915c7d3b2bc392724795113e51d289378"),
    ("sodium", "Pb3OXVqC", "AANobbMI", "sodium-neoforge-0.6.13+mc1.21.1.jar",
     "38af70fa4dc4b2aaac636e92fdba3bedd5a025e1"),
]
KEYWORDS = {
    "iris": ("PipelineManager", "WorldRenderingPipeline", "ShadowRenderer",
             "SodiumShader", "SodiumPrograms", "ShaderPack", "RenderTargets",
             "MixinSodiumWorldRenderer", "IrisRenderingPipeline", "CompositeRenderer"),
    "sodium": ("RenderSectionManager", "ChunkBuilder", "ChunkRenderer", "BlockRenderer",
               "FluidRenderer", "ShaderChunkRenderer", "TerrainRenderPass",
               "GlProgram", "ChunkRender", "VertexSerializer", "ShaderProgram",
               "RenderDevice", "ChunkMesh"),
}

def fetch(url: str) -> bytes:
    for attempt in range(4):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Planetary-source-audit/3R-7F-5 (+GitHub Actions)"})
            with urllib.request.urlopen(req, timeout=85) as response:
                return response.read()
        except Exception as ex:
            if attempt == 3:
                raise RuntimeError(f"Unable to fetch published release {url}: {ex}") from ex
            time.sleep((attempt + 1) * 3)

def digest(data: bytes, alg: str = "sha256") -> str:
    return hashlib.new(alg, data).hexdigest()

def rows(path: Path, header: tuple[str, ...], entries: list[tuple]):
    with path.open("w", newline="", encoding="utf-8") as out:
        writer = csv.writer(out, delimiter="\t")
        writer.writerow(header)
        writer.writerows(entries)

def source_candidates(component: str, classes: list[str]) -> list[str]:
    ranked = []
    for key in KEYWORDS[component]:
        matches = [x for x in classes if x.rsplit("/", 1)[-1] == key + ".class"]
        # Prefer the exact top-level class and avoid synthetic nested-only names.
        if matches:
            ranked.append(matches[0])
    return ranked

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    CACHE.mkdir(parents=True, exist_ok=True)
    manifests = []
    selected = []
    javap_parts = []
    metadata = []
    shaders = []
    mixins = []
    details = {}
    for component, version, project, filename, fixed_sha1 in SPECS:
        version_data = json.loads(fetch("https://api.modrinth.com/v2/version/" + version))
        assert version_data["id"] == version, (component, version_data.get("id"))
        assert version_data["project_id"] == project, (component, version_data.get("project_id"))
        assert "1.21.1" in version_data["game_versions"], (component,version_data["game_versions"])
        assert "neoforge" in version_data["loaders"], (component,version_data["loaders"])
        candidates = [f for f in version_data["files"] if f["filename"] == filename]
        assert len(candidates) == 1, (component, [f["filename"] for f in version_data["files"]])
        f = candidates[0]
        assert f["hashes"]["sha1"].lower() == fixed_sha1, (component, "published SHA1 changed")
        url = f["url"]
        assert url.startswith("https://cdn.modrinth.com/data/" + project + "/versions/" + version + "/"), url
        data = fetch(url)
        assert digest(data, "sha1") == fixed_sha1, component + " binary sha1 mismatch"
        assert digest(data, "sha512") == f["hashes"]["sha512"].lower(), component + " binary sha512 mismatch"
        jar = CACHE / filename
        jar.write_bytes(data)
        manifests.append((component, version, project, filename, str(len(data)), fixed_sha1,
                          digest(data), f["hashes"]["sha512"], "OFFICIAL_MODRINTH_HASH_MATCH"))
        with ZipFile(jar) as z:
            all_paths = z.namelist()
            class_paths = sorted(x for x in all_paths if x.endswith(".class"))
            assert len(class_paths) > 100, (component, len(class_paths))
            mods_toml = [p for p in all_paths if p.endswith("neoforge.mods.toml")]
            assert mods_toml, component + " missing NeoForge mod metadata"
            for p in mods_toml:
                raw = z.read(p).decode("utf-8", "replace")
                metadata.append((component, p, digest(z.read(p)), raw.replace("\n", "\\n")[:12000]))
            json_configs = [p for p in all_paths if p.endswith(".json") and "mixin" in p.lower()]
            for p in sorted(json_configs):
                content = z.read(p)
                try:
                    obj = json.loads(content)
                except Exception:
                    continue
                pk = obj.get("package","") if isinstance(obj,dict) else ""
                entries = []
                if isinstance(obj,dict):
                    for field in ("mixins","client","server"):
                        entries.extend(obj.get(field,[]) if isinstance(obj.get(field,[]),list) else [])
                mixins.append((component,p,pk,len(entries),digest(content),
                               ";".join(str(x) for x in entries if any(s in str(x).lower()
                               for s in ("shader","shadow","pipeline","render","chunk","sodium")))[:2500]))
            for p in sorted(x for x in all_paths if x.endswith((".vsh",".fsh",".glsl",".vert",".frag"))):
                blob = z.read(p)
                shaders.append((component,p,digest(blob),len(blob),
                                ",".join(s for s in ("normal","tangent","shadow","gbuffer","projection","modelview")
                                         if s.encode() in blob.lower())))
            owners = source_candidates(component,class_paths)
            assert len(owners) >= 4, (component, "too few actual original JVM owner names matched", owners)
            for p in owners[:12]:
                javaclass = p[:-6].replace("/",".")
                cp = subprocess.run(["javap","-private","-s","-c","-classpath",str(jar),javaclass],
                                    capture_output=True,text=True,check=False,timeout=35)
                assert cp.returncode == 0, (component, javaclass, cp.stderr)
                assert "Compiled from" in cp.stdout or "class " in cp.stdout or "interface " in cp.stdout, javaclass
                original= z.read(p)
                selected.append((component,p,digest(original),len(original),"ORIGINAL_RELEASE_CLASSFILE_JAVAP"))
                javap_parts.append("===== "+component+" "+javaclass+" =====\n"+cp.stdout)
            details[component] = {
                "jar": filename,
                "version_id":version,
                "sha256":digest(data),
                "total_classes":len(class_paths),
                "selected_original_classes":len(owners[:12]),
                "neoforge_metadata_count":len(mods_toml),
                "mixin_config_count":len(json_configs),
                "shader_files":sum(1 for s in shaders if s[0]==component),
                "declared_dependencies":version_data.get("dependencies",[]),
            }
    iris_deps = details["iris"]["declared_dependencies"]
    sodium_project_dependency = any(
        d.get("project_id") == "AANobbMI" or d.get("version_id") == "Pb3OXVqC"
        for d in iris_deps if isinstance(d, dict)
    )
    details["iris"]["sodium_modrinth_api_direct_dependency"] = sodium_project_dependency
    details["iris"]["dependency_disposition"] = (
        "MODRINTH_API_PROJECT_LINK_CONFIRMED" if sodium_project_dependency
        else "MODRINTH_API_LIST_INCOMPLETE_OR_NO_DIRECT_LINK_CHECK_JAR_TOML_AND_PUBLISHED_VERSION_PAGE"
    )
    # Modrinth dependencies, version page and the JAR's NeoForge loader
    # metadata are different sources. Avoid treating a missing API dependency
    # row as incompatible JAR evidence.
    rows(OUT/"release-hash-abi.tsv",
         ("component","modrinth_version_id","modrinth_project_id","jar",
          "bytes","sha1","sha256","sha512","verification"),
         manifests)
    rows(OUT/"selected-original-jvm-classfiles.tsv",
         ("component","classfile","class_sha256","class_size","scope"),selected)
    rows(OUT/"published-neoforge-mod-metadata.tsv",
         ("component","jar_path","sha256","escaped_mod_toml"),metadata)
    rows(OUT/"published-mixin-configs.tsv",
         ("component","config_path","package","entries","sha256","render_related_mixins"),mixins)
    rows(OUT/"published-shader-resources.tsv",
         ("component","jar_path","sha256","bytes","keyword_presence_only"),shaders)
    (OUT/"selected-original-javap.txt").write_text("\n".join(javap_parts),encoding="utf-8")
    details["scope"]="original_published_NeoForge_mod_JAR_binary_and_resources_only"
    details["exclusions"]=["no Minecraft client started","no shaderpack loaded",
                            "no Mixin transformation", "no GPU/GLSL execution",
                            "no guarantee all modded renderers use selected source",
                            "no official sources.jar provided as part of this audit"]
    (OUT/"summary.json").write_text(json.dumps(details,indent=2,sort_keys=True)+"\n",encoding="utf-8")
    print(json.dumps(details,indent=2,sort_keys=True))
    print("SELECTED_ORIGINAL_JAVAP_CLASSES",len(selected),
          "MIXIN_CONFIGS",len(mixins),"SHADERS",len(shaders))
if __name__=="__main__":
    main()
