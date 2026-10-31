#!/usr/bin/env python3
"""
ZeltrixWare kaynak çıkarıcı.

original/ZeltrixWare.jar içinden:
  1. thunder/** sınıflarını Vineflower ile decompile eder,
  2. intermediary isimleri (class_/method_/field_) yarn isimlerine çevirir,
  3. accesswidener'ı intermediary -> named'e dönüştürür,
  4. resource'ları (assets, fabric.mod.json, mixins json, icon) çıkarır,
  5. thunder/** dışındaki her şeyi (kotlin, jnr, netty, orbit, mediaplayerinfo...)
     libs/zeltrixware-bundled-libs.jar olarak ayırır,
  6. DECOMPILE_REPORT.md yazar.

Kullanım:
  python3 tools/decompile.py --jar original/ZeltrixWare.jar [--root .] [--force]
"""
import argparse
import logging
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request
import zipfile
from pathlib import Path

LOG = logging.getLogger("decompile")

VINEFLOWER_VERSION = "1.10.1"
MAVEN = "https://maven.fabricmc.net"
PROJECT_PREFIX = "thunder/"
RESOURCE_FILES = {"fabric.mod.json", "icon.png", "thunderhack.mixins.json"}
RESOURCE_PREFIXES = ("assets/",)
NOT_LIB_FILES = RESOURCE_FILES | {
    "thunderhack.accesswidener",
    "zeltrixwarev5-refmap.json",
    "LICENSE_zeltrixwarev5",
    "META-INF/MANIFEST.MF",
    "META-INF/INDEX.LIST",
}
LIBS_JAR_NAME = "zeltrixware-bundled-libs.jar"

DOT_CLASS = re.compile(r"net\.minecraft\.class_\d+(?:[.$]class_\d+)*")
SLASH_CLASS = re.compile(r"net/minecraft/class_\d+(?:\$class_\d+)*")
SIMPLE_CLASS = re.compile(r"\bclass_\d+\b")
MEMBER = re.compile(r"\b(?:method|field|comp)_\d+\b")
IMPORT_LINE = re.compile(
    r"^import (net\.minecraft\.class_\d+(?:[.$]class_\d+)*);[ \t]*$", re.M
)
FAIL_MARKERS = ("Couldn't be decompiled", "$VF: Unable to", "Method decompilation error")


def download(url: str, dest: Path) -> None:
    LOG.info("indiriliyor: %s", url)
    req = urllib.request.Request(url, headers={"User-Agent": "zeltrixware-bootstrap"})
    with urllib.request.urlopen(req, timeout=120) as resp, open(dest, "wb") as fh:
        shutil.copyfileobj(resp, fh)


def yarn_url(version: str) -> str:
    return f"{MAVEN}/net/fabricmc/yarn/{version}/yarn-{version}-mergedv2.jar"


def resolve_yarn(requested: str, dest: Path) -> str:
    try:
        download(yarn_url(requested), dest)
        return requested
    except Exception as exc:  # noqa: BLE001
        LOG.warning("yarn %s indirilemedi (%s), en günceli aranıyor", requested, exc)
    mc = requested.split("+")[0]
    req = urllib.request.Request(
        f"{MAVEN}/net/fabricmc/yarn/maven-metadata.xml",
        headers={"User-Agent": "zeltrixware-bootstrap"},
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        meta = resp.read().decode("utf-8")
    builds = sorted(
        int(b) for b in re.findall(rf"<version>{re.escape(mc)}\+build\.(\d+)</version>", meta)
    )
    if not builds:
        raise SystemExit(f"HATA: {mc} için yarn sürümü bulunamadı")
    version = f"{mc}+build.{builds[-1]}"
    download(yarn_url(version), dest)
    return version


class Renamer:
    def __init__(self, tiny_text: str) -> None:
        lines = tiny_text.splitlines()
        hdr = lines[0].split("\t")
        if hdr[:2] != ["tiny", "2"] or hdr[3:5] != ["intermediary", "named"]:
            raise SystemExit(f"HATA: beklenmeyen mapping başlığı: {hdr}")
        self.classes = {}
        self.members = {}
        for line in lines[1:]:
            if line.startswith("c\t"):
                p = line.split("\t")
                if len(p) >= 3 and p[2]:
                    self.classes[p[1]] = p[2]
            elif line.startswith("\t") and not line.startswith("\t\t"):
                p = line[1:].split("\t")
                if p[0] in ("m", "f") and len(p) >= 4 and p[3] and p[2] != p[3]:
                    self.members[p[2]] = p[3]
        self.simple = {}
        self.fqn = {}
        for inter, named in self.classes.items():
            tok = re.split(r"[/$]", inter)[-1]
            if not tok.startswith("class_"):
                continue
            self.simple[tok] = re.split(r"[/$]", named)[-1]
            self.fqn[tok] = named.replace("/", ".").replace("$", ".")
        LOG.info("mapping: %d sınıf, %d üye", len(self.classes), len(self.members))

    def _dot(self, raw: str) -> str:
        parts = raw.split(".")
        key = "net/minecraft/" + "$".join(parts[2:])
        named = self.classes.get(key)
        return named.replace("/", ".").replace("$", ".") if named else raw

    def apply(self, text: str) -> str:
        kept = {}
        conflicts = {}
        for m in IMPORT_LINE.finditer(text):
            raw = m.group(1)
            named_fqn = self._dot(raw)
            if named_fqn == raw:
                continue
            simple_named = named_fqn.rsplit(".", 1)[-1]
            tok = re.split(r"[.$]", raw)[-1]
            prev = kept.get(simple_named)
            if prev is None:
                kept[simple_named] = named_fqn
            elif prev != named_fqn:
                conflicts[tok] = named_fqn
        if conflicts:
            text = IMPORT_LINE.sub(
                lambda m: "" if re.split(r"[.$]", m.group(1))[-1] in conflicts else m.group(0),
                text,
            )
        text = DOT_CLASS.sub(lambda m: self._dot(m.group(0)), text)
        text = SLASH_CLASS.sub(lambda m: self.classes.get(m.group(0), m.group(0)), text)
        text = SIMPLE_CLASS.sub(
            lambda m: conflicts.get(m.group(0)) or self.simple.get(m.group(0), m.group(0)),
            text,
        )
        text = MEMBER.sub(lambda m: self.members.get(m.group(0), m.group(0)), text)
        return text

    def convert_access_widener(self, text: str) -> str:
        out = []
        for i, line in enumerate(text.splitlines()):
            if i == 0:
                out.append(re.sub(r"\bintermediary\b", "named", line))
                continue
            line = SLASH_CLASS.sub(lambda m: self.classes.get(m.group(0), m.group(0)), line)
            line = MEMBER.sub(lambda m: self.members.get(m.group(0), m.group(0)), line)
            out.append(line)
        return "\n".join(out) + "\n"


def split_jar(jar: Path, work: Path, root: Path) -> Path:
    classes_dir = work / "classes"
    classes_dir.mkdir(parents=True, exist_ok=True)
    libs_dir = root / "libs"
    libs_dir.mkdir(parents=True, exist_ok=True)
    libs_jar = libs_dir / LIBS_JAR_NAME
    res_dir = root / "src" / "main" / "resources"
    res_dir.mkdir(parents=True, exist_ok=True)

    n_classes = n_libs = n_res = 0
    with zipfile.ZipFile(jar) as zin, zipfile.ZipFile(libs_jar, "w", zipfile.ZIP_DEFLATED) as zlib:
        for info in zin.infolist():
            name = info.filename
            is_project = name.startswith(PROJECT_PREFIX)
            if is_project and name.endswith(".class"):
                zin.extract(info, classes_dir)
                n_classes += 1
                continue
            if is_project:
                continue
            if name in RESOURCE_FILES or name.startswith(RESOURCE_PREFIXES):
                if not info.is_dir():
                    target = res_dir / name
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(zin.read(info))
                    n_res += 1
                continue
            if name == "LICENSE_zeltrixwarev5":
                (root / "LICENSE").write_bytes(zin.read(info))
                continue
            if name == "thunderhack.accesswidener" or name in NOT_LIB_FILES:
                continue
            if info.is_dir():
                continue
            zlib.writestr(info.filename, zin.read(info))
            n_libs += 1
    LOG.info("sınıf: %d, resource: %d, lib girdisi: %d", n_classes, n_res, n_libs)
    if n_classes == 0:
        raise SystemExit("HATA: jar içinde thunder/** sınıfı yok")
    return classes_dir


def run_vineflower(vf: Path, classes_dir: Path, original: Path, out_dir: Path) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    cmd = [
        "java", "-Xmx4g", "-jar", str(vf),
        "-dgs=1", "-asc=1", "-rsy=1", "-log=WARN",
        f"-e={original}",
        str(classes_dir), str(out_dir),
    ]
    LOG.info("Vineflower çalışıyor")
    LOG.debug("komut: %s", " ".join(cmd))
    proc = subprocess.run(cmd, check=False)
    if proc.returncode != 0:
        LOG.warning("Vineflower çıkış kodu %d (kısmi çıktı olabilir)", proc.returncode)


def update_gradle_yarn(root: Path, version: str) -> None:
    props = root / "gradle.properties"
    if not props.exists():
        return
    text = props.read_text(encoding="utf-8")
    new = re.sub(r"(?m)^yarn_mappings=.*$", f"yarn_mappings={version}", text)
    if new != text:
        props.write_text(new, encoding="utf-8")
        LOG.info("gradle.properties yarn_mappings=%s", version)


def main() -> int:
    ap = argparse.ArgumentParser(description="ZeltrixWare jar -> Gradle projesi")
    ap.add_argument("--jar", required=True, type=Path)
    ap.add_argument("--root", default=".", type=Path)
    ap.add_argument("--yarn", default="1.21+build.9")
    ap.add_argument("--vineflower", default=VINEFLOWER_VERSION)
    ap.add_argument("--force", action="store_true", help="mevcut src/main/java üzerine yaz")
    ap.add_argument("-v", "--verbose", action="store_true")
    args = ap.parse_args()
    logging.basicConfig(
        level=logging.DEBUG if args.verbose else logging.INFO,
        format="[%(levelname)s] %(message)s",
    )

    jar = args.jar.resolve()
    root = args.root.resolve()
    if not jar.is_file():
        LOG.error("jar bulunamadı: %s", jar)
        return 2
    if shutil.which("java") is None:
        LOG.error("java PATH'te yok")
        return 2

    java_dir = root / "src" / "main" / "java"
    if java_dir.exists() and any(java_dir.rglob("*.java")) and not args.force:
        LOG.error("%s dolu; --force ile tekrar çalıştır", java_dir)
        return 3

    with tempfile.TemporaryDirectory() as tmp:
        work = Path(tmp)
        yarn_jar = work / "yarn.jar"
        yarn_version = resolve_yarn(args.yarn, yarn_jar)
        update_gradle_yarn(root, yarn_version)
        with zipfile.ZipFile(yarn_jar) as z:
            renamer = Renamer(z.read("mappings/mappings.tiny").decode("utf-8"))

        vf = work / "vineflower.jar"
        download(
            f"https://github.com/Vineflower/vineflower/releases/download/"
            f"{args.vineflower}/vineflower-{args.vineflower}.jar",
            vf,
        )

        classes_dir = split_jar(jar, work, root)

        with zipfile.ZipFile(jar) as zin:
            aw_raw = zin.read("thunderhack.accesswidener").decode("utf-8")
        aw_path = root / "src" / "main" / "resources" / "thunderhack.accesswidener"
        aw_path.write_text(renamer.convert_access_widener(aw_raw), encoding="utf-8")
        LOG.info("accesswidener yazıldı: %s", aw_path)

        raw_out = work / "decompiled"
        run_vineflower(vf, classes_dir, jar, raw_out)

        if java_dir.exists():
            shutil.rmtree(java_dir)
        java_dir.mkdir(parents=True)

        failed = []
        total = 0
        for src in sorted(raw_out.rglob("*.java")):
            rel = src.relative_to(raw_out)
            text = src.read_text(encoding="utf-8", errors="replace")
            if any(marker in text for marker in FAIL_MARKERS):
                failed.append(str(rel))
            dest = java_dir / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_text(renamer.apply(text), encoding="utf-8")
            total += 1

    report = [
        "# Decompile raporu",
        "",
        f"- Kaynak jar: `{jar.name}`",
        f"- Yarn: `{yarn_version}`",
        f"- Vineflower: `{args.vineflower}`",
        f"- Yazılan .java dosyası: {total}",
        f"- Sorunlu dosya: {len(failed)}",
        "",
        "Sorunlu dosyalar elle düzeltilmeli (decompiler tam çıkaramadı):",
        "",
    ]
    report += [f"- `{f}`" for f in failed] or ["- yok"]
    (root / "DECOMPILE_REPORT.md").write_text("\n".join(report) + "\n", encoding="utf-8")
    LOG.info("bitti: %d dosya, %d sorunlu", total, len(failed))
    return 0 if total > 0 else 1


if __name__ == "__main__":
    sys.exit(main())
