#!/usr/bin/env python3
"""
diff_native_api.py — diff a CleverTap native SDK between two tagged versions.

What it diffs (in a single invocation):
    1. Public API surface — methods on classes / protocols / listeners.
    2. Build manifest:
         Android — gradle/libs.versions.toml, module build.gradle SDK levels +
                   direct deps, src/main/AndroidManifest.xml uses-permission /
                   uses-feature.
         iOS    — module .podspec: platform, deployment targets, swift_version,
                   dependencies (bare and platform-prefixed).
    3. Changelog cross-validation — the matching entry from the native SDK's
       per-module changelog (docs/CTCORECHANGELOG.md / CHANGELOG.md), surfaced
       verbatim as a sanity panel.

Stdlib-only. No `pip install` needed. Requires Python ≥ 3.11 for tomllib.

Usage:
    diff_native_api.py \
      --platform {android|ios} \
      --module {core|pushtemplates|hms} \
      --old-version 8.0.0 --new-version 8.1.0 \
      [--local-path /path/to/local/clone] \
      [--out-dir ~/.cache/clevertap-sdk-diff] \
      [--no-cache]

Outputs (under <out-dir>/<platform>-<module>-<old>-to-<new>/):
    diff.json    — structured diff for programmatic consumption
    diff.md      — human-readable summary

Source acquisition order:
    1. --local-path with the requested tag available locally (git archive)
    2. ~/.cache/clevertap-sdk-versions/<repo>-<tag>/ if present
    3. GitHub tarball download to the cache

Design notes:
    - Regex-based parsing (not AST). Catches ~80% of public-surface changes
      cleanly; the remaining noise is for the engineer to triage.
    - @RestrictTo / @Hide annotated methods on Android are filtered out.
    - All extracted symbols are sorted by name for stable diffs.
"""

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tarfile
import tempfile
import tomllib
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field, asdict
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple


# ─────────────────────────── Configuration ─────────────────────────────

REPOS = {
    ("android", "core"):          ("CleverTap/clevertap-android-sdk", "corev{ver}"),
    ("android", "pushtemplates"): ("CleverTap/clevertap-android-sdk", "ptv{ver}"),
    ("android", "hms"):           ("CleverTap/clevertap-android-sdk", "hmsv{ver}"),
    ("ios",     "core"):          ("CleverTap/clevertap-ios-sdk",     "{ver}"),
    ("ios",     "pushtemplates"): ("CleverTap/clevertap-ios-sdk",     "{ver}"),  # TBD on first real run
}

# Source globs per (platform, module). Paths are relative to the extracted source root.
SOURCE_GLOBS = {
    ("android", "core"): [
        "clevertap-core/src/main/java/com/clevertap/android/sdk/CleverTapAPI.java",
        "clevertap-core/src/main/java/com/clevertap/android/sdk/**/*Listener.java",
        "clevertap-core/src/main/java/com/clevertap/android/sdk/**/*Callback.java",
        "clevertap-core/src/main/java/com/clevertap/android/sdk/**/*Config.java",
    ],
    ("android", "pushtemplates"): [
        "clevertap-pushtemplates/src/main/java/com/clevertap/android/pushtemplates/**/*.java",
        "clevertap-pushtemplates/src/main/java/com/clevertap/android/pushtemplates/**/*.kt",
    ],
    ("android", "hms"): [
        "clevertap-hms/src/main/java/com/clevertap/android/hms/**/*.java",
        "clevertap-hms/src/main/java/com/clevertap/android/hms/**/*.kt",
    ],
    ("ios", "core"): [
        "CleverTapSDK/**/*.h",
    ],
    ("ios", "pushtemplates"): [
        # TBD on first real run — confirm where iOS push template headers live
        # in the clevertap-ios-sdk repo (or whether they're in a separate repo).
        "**/CTNotificationService*.h",
    ],
}

DEFAULT_OUT_DIR = Path.home() / ".cache" / "clevertap-sdk-diff"
DEFAULT_CACHE_DIR = Path.home() / ".cache" / "clevertap-sdk-versions"

# Per-module Android build-manifest locations (relative to source root).
ANDROID_MANIFEST_PATHS = {
    "core":          {"build_gradle": "clevertap-core/build.gradle",
                      "manifest":     "clevertap-core/src/main/AndroidManifest.xml"},
    "pushtemplates": {"build_gradle": "clevertap-pushtemplates/build.gradle",
                      "manifest":     "clevertap-pushtemplates/src/main/AndroidManifest.xml"},
    "hms":           {"build_gradle": "clevertap-hms/build.gradle",
                      "manifest":     "clevertap-hms/src/main/AndroidManifest.xml"},
}

# Per-module iOS podspec locations (relative to source root).
IOS_PODSPEC_PATHS = {
    "core":          "CleverTap-iOS-SDK.podspec",
    # iOS push templates: confirm on first real run.
    "pushtemplates": "CTNotificationService.podspec",
}

# Per-module changelog locations (relative to source root).
CHANGELOG_PATHS = {
    ("android", "core"):          "docs/CTCORECHANGELOG.md",
    ("android", "pushtemplates"): "docs/CTPUSHTEMPLATESCHANGELOG.md",
    ("android", "hms"):           "docs/CTHMSCHANGELOG.md",
    ("ios",     "core"):          "CHANGELOG.md",
    ("ios",     "pushtemplates"): "CHANGELOG.md",
}


# ─────────────────────────── Data classes ──────────────────────────────

@dataclass(frozen=True)
class Symbol:
    """A single public-API symbol — method, protocol member, etc."""
    file: str            # relative path from source root
    kind: str            # "method", "static_method"
    name: str            # method or selector name
    signature: str       # normalized signature (for CHANGED detection)

    def as_dict(self) -> dict:
        return asdict(self)


@dataclass
class Surface:
    """All public symbols extracted from a version of the SDK."""
    by_name: Dict[str, List[Symbol]] = field(default_factory=dict)

    def add(self, sym: Symbol) -> None:
        self.by_name.setdefault(sym.name, []).append(sym)

    def names(self) -> Set[str]:
        return set(self.by_name.keys())


# ─────────────────────────── Source acquisition ────────────────────────

def acquire_sources(
    platform: str,
    module: str,
    version: str,
    local_path: Optional[Path],
    cache_dir: Path,
    no_cache: bool,
) -> Path:
    """Return a directory holding the SDK source tree at the requested tag."""
    repo, tag_fmt = REPOS[(platform, module)]
    tag = tag_fmt.format(ver=version)
    repo_slug = repo.split("/")[-1]

    # 1. Local clone with the tag available
    if local_path and local_path.exists():
        if _git_has_tag(local_path, tag):
            extract_dir = cache_dir / f"{repo_slug}-{tag}-fromlocal"
            extract_dir.mkdir(parents=True, exist_ok=True)
            _git_archive_to_dir(local_path, tag, extract_dir)
            return extract_dir
        else:
            print(
                f"[diff] local clone at {local_path} does not have tag {tag}; "
                f"falling through to cache/tarball",
                file=sys.stderr,
            )

    # 2. Cache hit
    cache_target = cache_dir / f"{repo_slug}-{tag}"
    if cache_target.exists() and not no_cache:
        return cache_target

    # 3. Tarball download
    cache_dir.mkdir(parents=True, exist_ok=True)
    return _download_and_extract(repo, tag, cache_dir, cache_target)


def _git_has_tag(repo_path: Path, tag: str) -> bool:
    try:
        subprocess.check_output(
            ["git", "-C", str(repo_path), "show-ref", "--tags", "--verify", f"refs/tags/{tag}"],
            stderr=subprocess.DEVNULL,
        )
        return True
    except subprocess.CalledProcessError:
        return False


def _git_archive_to_dir(repo_path: Path, tag: str, dest: Path) -> None:
    proc = subprocess.run(
        ["git", "-C", str(repo_path), "archive", "--format=tar", tag],
        capture_output=True,
        check=True,
    )
    with tarfile.open(fileobj=__import__("io").BytesIO(proc.stdout)) as tf:
        tf.extractall(dest)


def _download_and_extract(repo: str, tag: str, cache_dir: Path, dest: Path) -> Path:
    url = f"https://github.com/{repo}/archive/refs/tags/{tag}.tar.gz"
    print(f"[diff] downloading {url}", file=sys.stderr)
    tmp_tar = cache_dir / f".download-{tag}-{os.getpid()}.tar.gz"
    try:
        with urllib.request.urlopen(url) as resp, open(tmp_tar, "wb") as f:
            shutil.copyfileobj(resp, f)
    except urllib.error.HTTPError as e:
        raise SystemExit(
            f"[diff] failed to download tag {tag} from {repo}: HTTP {e.code}. "
            f"Confirm the tag exists at https://github.com/{repo}/tags ."
        )
    # GitHub tarballs extract into a single top-level directory of an unpredictable
    # name; strip one level so dest/ is the source root.
    staging = cache_dir / f".staging-{tag}-{os.getpid()}"
    staging.mkdir(parents=True, exist_ok=True)
    with tarfile.open(tmp_tar) as tf:
        tf.extractall(staging)
    children = list(staging.iterdir())
    if len(children) != 1 or not children[0].is_dir():
        raise SystemExit(
            f"[diff] unexpected tarball layout for {tag}: {[c.name for c in children]}"
        )
    if dest.exists():
        shutil.rmtree(dest)
    children[0].rename(dest)
    shutil.rmtree(staging, ignore_errors=True)
    tmp_tar.unlink(missing_ok=True)
    return dest


# ─────────────────────────── Public-surface extraction ─────────────────

# Java/Kotlin: `public ReturnType methodName(...)` on a single line. Skip class/interface/enum
# declarations, and skip if the previous line(s) contain a restriction annotation.
_JAVA_PUBLIC_METHOD = re.compile(
    r"^\s*public\s+"
    r"(?!class\b|interface\b|enum\b|@interface\b|static\s+final\b)"
    r"(?:static\s+)?"
    r"(?:final\s+)?"
    r"(?:synchronized\s+)?"
    r"(?:<[^>]+>\s+)?"             # generic type params
    r"([\w\<\>\[\],\s\?\.]+?)\s+"  # return type (lazy)
    r"(\w+)\s*"                    # method name
    r"\(([^)]*)\)"                 # parameter list (no nested parens supported — fine for our surface)
)

_KOTLIN_PUBLIC_FUN = re.compile(
    r"^\s*(?:public\s+)?"
    r"(?:open\s+|abstract\s+|final\s+)?"
    r"fun\s+"
    r"(?:<[^>]+>\s+)?"
    r"(\w+)\s*"                    # function name
    r"\(([^)]*)\)"                 # parameter list
    r"(?:\s*:\s*([\w\<\>\[\],\s\?\.]+))?"  # optional return type
)

_RESTRICTION_ANNOTATION = re.compile(
    r"@(RestrictTo|Hide|Internal|VisibleForTesting)\b"
)

# Objective-C method declaration: `- (returnType)selector:(paramType)param ...` ending in `;`.
_OBJC_METHOD = re.compile(
    r"^\s*([+\-])\s*"              # kind: + class method, - instance method
    r"\(([^)]+)\)\s*"              # return type
    r"([\w:]+)"                    # selector (simplified — only the first part; full selector handled below)
)


def extract_surface(source_root: Path, platform: str, module: str) -> Surface:
    surface = Surface()
    globs = SOURCE_GLOBS.get((platform, module), [])
    for g in globs:
        for path in source_root.glob(g):
            if not path.is_file():
                continue
            rel = path.relative_to(source_root).as_posix()
            try:
                text = path.read_text(encoding="utf-8", errors="replace")
            except OSError:
                continue
            if platform == "android":
                if path.suffix == ".java":
                    _extract_java(text, rel, surface)
                elif path.suffix == ".kt":
                    _extract_kotlin(text, rel, surface)
            elif platform == "ios":
                if path.suffix == ".h":
                    _extract_objc_header(text, rel, surface)
    return surface


def _is_restricted(prev_lines: List[str]) -> bool:
    """Look up to 3 previous non-blank lines for a restriction annotation."""
    count = 0
    for line in reversed(prev_lines):
        stripped = line.strip()
        if not stripped:
            continue
        count += 1
        if _RESTRICTION_ANNOTATION.search(stripped):
            return True
        if count >= 3:
            break
        # Stop scanning if we hit something that clearly isn't an annotation
        # or modifier (a closing brace, a different method, etc.).
        if not (stripped.startswith("@") or stripped.startswith("//") or stripped.startswith("*")):
            break
    return False


def _extract_java(text: str, rel: str, surface: Surface) -> None:
    lines = text.splitlines()
    for i, line in enumerate(lines):
        # Quick reject before regex
        if "public" not in line:
            continue
        m = _JAVA_PUBLIC_METHOD.match(line)
        if not m:
            continue
        return_type, name, params = m.group(1), m.group(2), m.group(3)
        # Skip ctors (return type == class name pattern) — best-effort
        if return_type.strip() in ("", name):
            continue
        if _is_restricted(lines[max(0, i - 5):i]):
            continue
        sig = _normalize_signature(params, return_type)
        surface.add(Symbol(file=rel, kind="method", name=name, signature=sig))


def _extract_kotlin(text: str, rel: str, surface: Surface) -> None:
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "fun " not in line:
            continue
        # Skip if the line is a private/internal declaration
        if re.search(r"\b(private|internal)\s+fun\b", line):
            continue
        m = _KOTLIN_PUBLIC_FUN.match(line)
        if not m:
            continue
        name, params, ret = m.group(1), m.group(2), m.group(3) or "Unit"
        if _is_restricted(lines[max(0, i - 5):i]):
            continue
        sig = _normalize_signature(params, ret)
        surface.add(Symbol(file=rel, kind="method", name=name, signature=sig))


def _extract_objc_header(text: str, rel: str, surface: Surface) -> None:
    # Strip block and line comments for simplicity
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"//.*?$", "", text, flags=re.MULTILINE)

    # Combine multi-line method declarations: an ObjC declaration starts with `- (` or `+ (`
    # and continues until the first `;` outside of parentheses. Collapse to single lines.
    decls = _collect_objc_method_declarations(text)
    for decl in decls:
        m = re.match(r"^\s*([+\-])\s*\(([^)]+)\)\s*(.+);\s*$", decl, flags=re.DOTALL)
        if not m:
            continue
        kind_sign, return_type, rest = m.group(1), m.group(2), m.group(3)
        selector = _objc_extract_selector(rest)
        if not selector:
            continue
        kind = "static_method" if kind_sign == "+" else "method"
        sig = _normalize_signature(rest, return_type)
        surface.add(Symbol(file=rel, kind=kind, name=selector, signature=sig))


_OBJC_DECL_RE = re.compile(
    # Line-anchored: declaration starts with + or - at start of line (possibly
    # indented). Capture from there up to the first semicolon, allowing
    # multi-line declarations via DOTALL on the lazy `.*?`.
    r"^[ \t]*([+\-])[ \t]*\([^)]+\)[^;{]*?;",
    flags=re.MULTILINE | re.DOTALL,
)


def _collect_objc_method_declarations(text: str) -> List[str]:
    """Find all ObjC method declarations in a header.

    Matches lines starting with `+ (...)` or `- (...)` and captures up to the
    first `;`. Skips anything that contains `{` before the `;` (would be a
    method body, but those don't appear in headers anyway).
    """
    return [m.group(0) for m in _OBJC_DECL_RE.finditer(text)]


def _objc_extract_selector(rest: str) -> str:
    """Given the body of a method declaration after the return type, extract the selector.

    Examples:
        "registerForPush"                                 → "registerForPush"
        "recordEvent:(NSString *)event withProps:(NSDict *)p" → "recordEvent:withProps:"
        "profileGetProperty:(NSString *)key callback:(RCTResponseSenderBlock)cb"
                                                          → "profileGetProperty:callback:"
    """
    # Selector parts are identifiers possibly followed by ':' then a parameter clause.
    parts: List[str] = []
    i = 0
    n = len(rest)
    while i < n:
        # Skip whitespace
        while i < n and rest[i].isspace():
            i += 1
        if i >= n:
            break
        # Identifier
        ident_match = re.match(r"\w+", rest[i:])
        if not ident_match:
            break
        ident = ident_match.group(0)
        i += len(ident)
        # Check for trailing ':'
        if i < n and rest[i] == ":":
            parts.append(ident + ":")
            i += 1
            # Skip parameter clause: '(' ... ')' then identifier
            if i < n and rest[i] == "(":
                depth = 1
                i += 1
                while i < n and depth > 0:
                    if rest[i] == "(":
                        depth += 1
                    elif rest[i] == ")":
                        depth -= 1
                    i += 1
            # Skip parameter name
            while i < n and rest[i].isspace():
                i += 1
            param_name = re.match(r"\w+", rest[i:])
            if param_name:
                i += len(param_name.group(0))
        else:
            # No-arg selector (or a trailing modifier like NS_AVAILABLE)
            if not parts:
                parts.append(ident)
            break
    return "".join(parts) if parts else ""


def _normalize_signature(params: str, return_type: str) -> str:
    """Collapse whitespace and strip parameter names to make signatures stable.

    Best-effort — only used to detect CHANGED. False positives (CHANGED that's
    really stylistic) get flagged for the engineer to triage.
    """
    p = re.sub(r"\s+", " ", params).strip()
    r = re.sub(r"\s+", " ", return_type).strip()
    return f"({p}) -> {r}"


# ─────────────────────────── Diffing ───────────────────────────────────

def compute_diff(old: Surface, new: Surface) -> dict:
    old_names = old.names()
    new_names = new.names()

    added_names = sorted(new_names - old_names)
    removed_names = sorted(old_names - new_names)
    common_names = sorted(old_names & new_names)

    added: List[dict] = []
    for n in added_names:
        for sym in new.by_name[n]:
            added.append(sym.as_dict())

    removed: List[dict] = []
    for n in removed_names:
        for sym in old.by_name[n]:
            removed.append(sym.as_dict())

    changed: List[dict] = []
    for n in common_names:
        old_sigs = {s.signature for s in old.by_name[n]}
        new_sigs = {s.signature for s in new.by_name[n]}
        if old_sigs != new_sigs:
            changed.append({
                "name": n,
                "old": sorted(old_sigs),
                "new": sorted(new_sigs),
                "files": sorted({s.file for s in new.by_name[n]}),
            })

    return {"added": added, "removed": removed, "changed": changed}


# ─────────────────────────── Build manifest extraction ────────────────

def extract_android_build_manifest(source_root: Path, module: str) -> dict:
    """Extract SDK levels, version catalog, direct deps, permissions, features."""
    paths = ANDROID_MANIFEST_PATHS.get(module)
    if not paths:
        return {}

    # Parse the root version catalog
    toml_path = source_root / "gradle" / "libs.versions.toml"
    catalog: dict = {}
    if toml_path.exists():
        try:
            with toml_path.open("rb") as f:
                catalog = tomllib.load(f)
        except Exception as e:
            print(f"[diff] failed to parse {toml_path}: {e}", file=sys.stderr)

    versions = catalog.get("versions", {})  # flat {snake_key: value-string}

    # SDK levels — read from the module's build.gradle
    sdk_levels = _extract_android_sdk_levels(source_root / paths["build_gradle"], versions)

    # Direct dependency declarations (literal strings, not catalog refs)
    direct_deps = _extract_android_direct_deps(source_root / paths["build_gradle"])

    # Manifest permissions / features
    perms, features = _extract_android_manifest_entries(source_root / paths["manifest"])

    return {
        "sdk_levels": sdk_levels,
        "versions_catalog": {
            "versions": versions,
            "libraries": catalog.get("libraries", {}),
            "bundles": catalog.get("bundles", {}),
            "plugins": catalog.get("plugins", {}),
        },
        "direct_deps": sorted(direct_deps),
        "permissions": sorted(perms),
        "uses_features": sorted(features),
    }


_GRADLE_SDK_LITERAL = re.compile(
    r"^\s*(minSdk(?:Version)?|targetSdk(?:Version)?|compileSdk(?:Version)?)\s+(\d+)\b",
    re.MULTILINE,
)
_GRADLE_SDK_CATALOG = re.compile(
    r"^\s*(minSdk(?:Version)?Val|targetSdk(?:Version)?Val|compileSdk(?:Version)?Val)\s*=\s*"
    r"libs\.versions\.([\w.]+)\.get\(\)\.toInteger\(\)",
    re.MULTILINE,
)
_GRADLE_DIRECT_DEP = re.compile(
    r"^\s*(api|implementation|compileOnly|testImplementation|androidTestImplementation)\s+"
    r"['\"]([^'\"]+:[^'\"]+:[^'\"]+)['\"]",
    re.MULTILINE,
)


def _extract_android_sdk_levels(gradle_path: Path, versions: dict) -> dict:
    """Return {minSdk: int, targetSdk: int, compileSdk: int} or partial."""
    if not gradle_path.exists():
        return {}
    text = gradle_path.read_text(encoding="utf-8", errors="replace")
    result: Dict[str, int] = {}

    for m in _GRADLE_SDK_LITERAL.finditer(text):
        key = _normalize_sdk_key(m.group(1))
        try:
            result[key] = int(m.group(2))
        except ValueError:
            pass

    for m in _GRADLE_SDK_CATALOG.finditer(text):
        key_raw = m.group(1).replace("Val", "")
        key = _normalize_sdk_key(key_raw)
        if key in result:
            continue
        catalog_path = m.group(2)  # e.g. "android.minSdk"
        toml_key = catalog_path.replace(".", "_")
        val = versions.get(toml_key)
        if val is not None:
            try:
                result[key] = int(val)
            except (ValueError, TypeError):
                pass

    return result


def _normalize_sdk_key(raw: str) -> str:
    raw = raw.replace("Version", "")
    if raw.startswith("minSdk"):
        return "minSdk"
    if raw.startswith("targetSdk"):
        return "targetSdk"
    if raw.startswith("compileSdk"):
        return "compileSdk"
    return raw


def _extract_android_direct_deps(gradle_path: Path) -> Set[str]:
    if not gradle_path.exists():
        return set()
    text = gradle_path.read_text(encoding="utf-8", errors="replace")
    out: Set[str] = set()
    for m in _GRADLE_DIRECT_DEP.finditer(text):
        scope = m.group(1)
        coord = m.group(2)
        out.add(f"{scope} {coord}")
    return out


def _extract_android_manifest_entries(manifest_path: Path) -> Tuple[Set[str], Set[str]]:
    if not manifest_path.exists():
        return set(), set()
    try:
        tree = ET.parse(manifest_path)
    except ET.ParseError as e:
        print(f"[diff] failed to parse {manifest_path}: {e}", file=sys.stderr)
        return set(), set()
    root = tree.getroot()
    ns = "{http://schemas.android.com/apk/res/android}"
    perms = {el.get(f"{ns}name", "") for el in root.iter("uses-permission")} - {""}
    feats = {el.get(f"{ns}name", "") for el in root.iter("uses-feature")} - {""}
    return perms, feats


# ─── iOS podspec extraction ───

_POD_FIELDS = {
    "platform_ios":        re.compile(r"^\s*s\.platform\s*=\s*:ios\s*,\s*['\"]([\d.]+)['\"]", re.MULTILINE),
    "platform_ios_target": re.compile(r"^\s*s\.ios\.deployment_target\s*=\s*['\"]([\d.]+)['\"]", re.MULTILINE),
    "platform_tvos":       re.compile(r"^\s*s\.tvos\.deployment_target\s*=\s*['\"]([\d.]+)['\"]", re.MULTILINE),
    "swift_version":       re.compile(r"^\s*s\.swift_version\s*=\s*['\"]([\d.]+)['\"]", re.MULTILINE),
}

_POD_DEP_RE = re.compile(
    r"^\s*s(?:\.(ios|tvos|osx|watchos))?\.dependency\s+['\"]([^'\"]+)['\"]"
    r"(?:\s*,\s*['\"]([^'\"]+)['\"])?",
    re.MULTILINE,
)


def extract_ios_build_manifest(source_root: Path, module: str) -> dict:
    podspec_rel = IOS_PODSPEC_PATHS.get(module)
    if not podspec_rel:
        return {}
    path = source_root / podspec_rel
    if not path.exists():
        return {"_warning": f"podspec not found at {podspec_rel}"}

    text = path.read_text(encoding="utf-8", errors="replace")

    platform: Dict[str, str] = {}
    for key, rx in _POD_FIELDS.items():
        m = rx.search(text)
        if not m:
            continue
        if key == "platform_ios":
            platform["ios"] = m.group(1)
        elif key == "platform_ios_target":
            # s.ios.deployment_target overrides s.platform if both present
            platform["ios"] = m.group(1)
        elif key == "platform_tvos":
            platform["tvos"] = m.group(1)

    swift_version_match = _POD_FIELDS["swift_version"].search(text)
    swift_version = swift_version_match.group(1) if swift_version_match else None

    deps: Set[str] = set()
    for m in _POD_DEP_RE.finditer(text):
        scope = m.group(1) or "all"
        name = m.group(2)
        version = m.group(3) or ""
        deps.add(f"{scope}:{name}:{version}".rstrip(":"))

    return {
        "platform": platform,
        "swift_version": swift_version,
        "dependencies": sorted(deps),
    }


# ─── Changelog cross-validation ───

def extract_changelog_entry(source_root: Path, platform: str, module: str, version: str) -> Optional[dict]:
    rel = CHANGELOG_PATHS.get((platform, module))
    if not rel:
        return None
    path = source_root / rel
    if not path.exists():
        return {"version": version, "entry": None,
                "_warning": f"changelog not found at {rel}"}
    text = path.read_text(encoding="utf-8", errors="replace")
    # Match `### Version X.Y.Z` or `### [Version X.Y.Z](link)` (case-insensitive);
    # capture through the next version heading or EOF.
    pattern = re.compile(
        rf"^###\s+\[?Version\s+{re.escape(version)}\b.*?(?=^###\s+\[?Version\b|\Z)",
        re.MULTILINE | re.DOTALL | re.IGNORECASE,
    )
    m = pattern.search(text)
    if not m:
        return {"version": version, "entry": None,
                "_warning": f"no '### Version {version}' section found in {rel}"}
    return {"version": version, "entry": m.group(0).strip()}


# ─── Build-manifest diff ───

def compute_build_diff(platform: str, old: dict, new: dict) -> dict:
    """Return a structured diff of two build manifest dicts."""
    if not old and not new:
        return {}

    if platform == "android":
        return _diff_android_build(old or {}, new or {})
    elif platform == "ios":
        return _diff_ios_build(old or {}, new or {})
    return {}


def _diff_android_build(old: dict, new: dict) -> dict:
    # SDK levels: scalar diffs only
    sdk_levels: Dict[str, dict] = {}
    for key in ("minSdk", "targetSdk", "compileSdk"):
        o = old.get("sdk_levels", {}).get(key)
        n = new.get("sdk_levels", {}).get(key)
        if o != n:
            sdk_levels[key] = {"old": o, "new": n}

    # Versions catalog: flat-key diff per section
    o_cat = old.get("versions_catalog", {})
    n_cat = new.get("versions_catalog", {})
    cat_diff: Dict[str, dict] = {}
    for section in ("versions", "libraries", "bundles", "plugins"):
        sec_old = o_cat.get(section, {}) or {}
        sec_new = n_cat.get(section, {}) or {}
        added = {k: sec_new[k] for k in sorted(set(sec_new) - set(sec_old))}
        removed = {k: sec_old[k] for k in sorted(set(sec_old) - set(sec_new))}
        changed = {
            k: {"old": sec_old[k], "new": sec_new[k]}
            for k in sorted(set(sec_old) & set(sec_new))
            if sec_old[k] != sec_new[k]
        }
        if added or removed or changed:
            cat_diff[section] = {"added": added, "removed": removed, "changed": changed}

    # Direct deps: list diff
    o_deps = set(old.get("direct_deps", []))
    n_deps = set(new.get("direct_deps", []))
    direct_deps = {
        "added": sorted(n_deps - o_deps),
        "removed": sorted(o_deps - n_deps),
    }
    if not direct_deps["added"] and not direct_deps["removed"]:
        direct_deps = {}

    # Permissions
    o_perms = set(old.get("permissions", []))
    n_perms = set(new.get("permissions", []))
    perm_diff = {
        "added": sorted(n_perms - o_perms),
        "removed": sorted(o_perms - n_perms),
    }
    if not perm_diff["added"] and not perm_diff["removed"]:
        perm_diff = {}

    # uses-features
    o_feats = set(old.get("uses_features", []))
    n_feats = set(new.get("uses_features", []))
    feat_diff = {
        "added": sorted(n_feats - o_feats),
        "removed": sorted(o_feats - n_feats),
    }
    if not feat_diff["added"] and not feat_diff["removed"]:
        feat_diff = {}

    return {
        "sdk_levels": sdk_levels,
        "versions_catalog": cat_diff,
        "direct_deps": direct_deps,
        "permissions": perm_diff,
        "uses_features": feat_diff,
    }


def _diff_ios_build(old: dict, new: dict) -> dict:
    platform_diff: Dict[str, dict] = {}
    o_plat = old.get("platform", {}) or {}
    n_plat = new.get("platform", {}) or {}
    for key in set(o_plat) | set(n_plat):
        if o_plat.get(key) != n_plat.get(key):
            platform_diff[key] = {"old": o_plat.get(key), "new": n_plat.get(key)}

    swift_diff = None
    if old.get("swift_version") != new.get("swift_version"):
        swift_diff = {"old": old.get("swift_version"), "new": new.get("swift_version")}

    o_deps = set(old.get("dependencies", []))
    n_deps = set(new.get("dependencies", []))
    deps_diff = {
        "added": sorted(n_deps - o_deps),
        "removed": sorted(o_deps - n_deps),
    }
    if not deps_diff["added"] and not deps_diff["removed"]:
        deps_diff = {}

    return {
        "platform": platform_diff,
        "swift_version": swift_diff,
        "dependencies": deps_diff,
    }


# ─────────────────────────── Output ────────────────────────────────────

def write_outputs(diff: dict, meta: dict, out_dir: Path) -> Tuple[Path, Path]:
    out_dir.mkdir(parents=True, exist_ok=True)
    json_path = out_dir / "diff.json"
    md_path = out_dir / "diff.md"

    payload = {"meta": meta, **diff}
    json_path.write_text(json.dumps(payload, indent=2, sort_keys=True))

    md = _render_markdown(payload)
    md_path.write_text(md)

    return json_path, md_path


def _render_markdown(payload: dict) -> str:
    meta = payload["meta"]
    added = payload["added"]
    removed = payload["removed"]
    changed = payload["changed"]

    lines: List[str] = []
    lines.append(f"# Native API diff: {meta['platform']}/{meta['module']} "
                 f"{meta['old_version']} → {meta['new_version']}")
    lines.append("")
    lines.append(f"- Source acquisition: {meta['source_strategy']}")
    lines.append(f"- Symbols old: {meta['symbols_old']}")
    lines.append(f"- Symbols new: {meta['symbols_new']}")
    lines.append(f"- Added: **{len(added)}**, Removed: **{len(removed)}**, Changed: **{len(changed)}**")
    lines.append("")

    if added:
        lines.append("## Added")
        lines.append("")
        lines.append("| Name | Kind | File |")
        lines.append("|---|---|---|")
        for s in added:
            lines.append(f"| `{s['name']}` | {s['kind']} | `{s['file']}` |")
        lines.append("")

    if removed:
        lines.append("## Removed")
        lines.append("")
        lines.append("| Name | Kind | File |")
        lines.append("|---|---|---|")
        for s in removed:
            lines.append(f"| `{s['name']}` | {s['kind']} | `{s['file']}` |")
        lines.append("")

    if changed:
        lines.append("## Changed signatures")
        lines.append("")
        for c in changed:
            lines.append(f"### `{c['name']}`")
            lines.append("")
            lines.append("Old:")
            for sig in c["old"]:
                lines.append(f"- `{sig}`")
            lines.append("")
            lines.append("New:")
            for sig in c["new"]:
                lines.append(f"- `{sig}`")
            lines.append("")
            lines.append(f"Files: {', '.join('`' + f + '`' for f in c['files'])}")
            lines.append("")

    if not (added or removed or changed):
        lines.append("_No public-surface changes detected._")
        lines.append("")
        lines.append("This usually means the release is bug-fix only. If you expected changes, "
                     "double-check the versions and module, and consider whether the additions "
                     "are in files outside the configured globs.")
        lines.append("")

    # Build manifest section
    build = payload.get("build") or {}
    if build:
        lines.append("## Build manifest changes")
        lines.append("")
        _render_build_section(lines, build, meta["platform"])

    # Changelog cross-validation panel
    changelog = payload.get("changelog")
    if changelog:
        lines.append(f"## Changelog entry for {changelog.get('version', meta['new_version'])}")
        lines.append("")
        entry = changelog.get("entry")
        warning = changelog.get("_warning")
        if warning:
            lines.append(f"_Warning: {warning}_")
            lines.append("")
        if entry:
            lines.append("```markdown")
            lines.append(entry)
            lines.append("```")
            lines.append("")

    return "\n".join(lines)


def _render_build_section(lines: List[str], build: dict, platform: str) -> None:
    # `build` is the full {android: {...}, ios: {...}} envelope; dig into the
    # active platform's nested dict.
    build = build.get(platform) or {}
    if platform == "android":
        sdk = build.get("sdk_levels") or {}
        if sdk:
            lines.append("### SDK levels")
            lines.append("")
            lines.append("| Key | Old | New |")
            lines.append("|---|---|---|")
            for key in ("minSdk", "targetSdk", "compileSdk"):
                if key in sdk:
                    lines.append(f"| `{key}` | {sdk[key]['old']} | {sdk[key]['new']} |")
            lines.append("")

        cat = build.get("versions_catalog") or {}
        for section_name in ("versions", "libraries", "bundles", "plugins"):
            sec = cat.get(section_name)
            if not sec:
                continue
            lines.append(f"### Version catalog — `{section_name}`")
            lines.append("")
            if sec.get("added"):
                lines.append("**Added:**")
                lines.append("")
                for k, v in sec["added"].items():
                    lines.append(f"- `{k}` = `{v}`")
                lines.append("")
            if sec.get("removed"):
                lines.append("**Removed:**")
                lines.append("")
                for k, v in sec["removed"].items():
                    lines.append(f"- `{k}` (was `{v}`)")
                lines.append("")
            if sec.get("changed"):
                lines.append("**Changed:**")
                lines.append("")
                for k, v in sec["changed"].items():
                    lines.append(f"- `{k}`: `{v['old']}` → `{v['new']}`")
                lines.append("")

        dd = build.get("direct_deps") or {}
        if dd:
            lines.append("### Direct dependency declarations (literal, non-catalog)")
            lines.append("")
            if dd.get("added"):
                lines.append("**Added:**")
                for d in dd["added"]:
                    lines.append(f"- `{d}`")
                lines.append("")
            if dd.get("removed"):
                lines.append("**Removed:**")
                for d in dd["removed"]:
                    lines.append(f"- `{d}`")
                lines.append("")

        perms = build.get("permissions") or {}
        if perms:
            lines.append("### AndroidManifest — uses-permission")
            lines.append("")
            for kind in ("added", "removed"):
                if perms.get(kind):
                    lines.append(f"**{kind.capitalize()}:**")
                    for p in perms[kind]:
                        lines.append(f"- `{p}`")
                    lines.append("")

        feats = build.get("uses_features") or {}
        if feats:
            lines.append("### AndroidManifest — uses-feature")
            lines.append("")
            for kind in ("added", "removed"):
                if feats.get(kind):
                    lines.append(f"**{kind.capitalize()}:**")
                    for f in feats[kind]:
                        lines.append(f"- `{f}`")
                    lines.append("")

        if not any([sdk, cat, dd, perms, feats]):
            lines.append("_No Android build-manifest changes detected._")
            lines.append("")

    elif platform == "ios":
        plat = build.get("platform") or {}
        if plat:
            lines.append("### Platform / deployment targets")
            lines.append("")
            lines.append("| Key | Old | New |")
            lines.append("|---|---|---|")
            for key, val in plat.items():
                lines.append(f"| `{key}` | {val.get('old')} | {val.get('new')} |")
            lines.append("")

        swift = build.get("swift_version")
        if swift:
            lines.append(f"### Swift version: `{swift.get('old')}` → `{swift.get('new')}`")
            lines.append("")

        deps = build.get("dependencies") or {}
        if deps:
            lines.append("### Pod dependencies")
            lines.append("")
            for kind in ("added", "removed"):
                if deps.get(kind):
                    lines.append(f"**{kind.capitalize()}:**")
                    for d in deps[kind]:
                        lines.append(f"- `{d}`")
                    lines.append("")

        if not any([plat, swift, deps]):
            lines.append("_No iOS build-manifest changes detected._")
            lines.append("")


# ─────────────────────────── Main ──────────────────────────────────────

def main() -> int:
    p = argparse.ArgumentParser(
        description=(
            "Diff CleverTap native SDK between two tagged versions. "
            "Covers public API surface, build manifest (SDK levels, dependencies, "
            "permissions, podspec), and surfaces the matching changelog entry."
        )
    )
    p.add_argument("--platform", choices=["android", "ios"], required=True)
    p.add_argument("--module", choices=["core", "pushtemplates", "hms"], required=True)
    p.add_argument("--old-version", required=True)
    p.add_argument("--new-version", required=True)
    p.add_argument("--local-path", type=Path, default=None,
                   help="Path to a local clone of the native SDK repo. If the requested tag is present, sources are taken from here.")
    p.add_argument("--out-dir", type=Path, default=DEFAULT_OUT_DIR,
                   help="Where to write diff.json and diff.md.")
    p.add_argument("--cache-dir", type=Path, default=DEFAULT_CACHE_DIR,
                   help="Where to cache downloaded/extracted source trees.")
    p.add_argument("--no-cache", action="store_true",
                   help="Force re-download even if the cache has the tag.")
    args = p.parse_args()

    if (args.platform, args.module) not in REPOS:
        print(f"[diff] platform/module combination not supported: {args.platform}/{args.module}", file=sys.stderr)
        return 2

    if args.platform == "ios" and args.module == "hms":
        print("[diff] iOS has no HMS module (Huawei is Android-only); aborting.", file=sys.stderr)
        return 2

    # Acquire both versions
    print(f"[diff] acquiring {args.platform}/{args.module} @ {args.old_version}", file=sys.stderr)
    old_src = acquire_sources(args.platform, args.module, args.old_version,
                              args.local_path, args.cache_dir, args.no_cache)
    print(f"[diff] acquiring {args.platform}/{args.module} @ {args.new_version}", file=sys.stderr)
    new_src = acquire_sources(args.platform, args.module, args.new_version,
                              args.local_path, args.cache_dir, args.no_cache)

    # Extract surfaces
    print(f"[diff] extracting old surface from {old_src}", file=sys.stderr)
    old_surface = extract_surface(old_src, args.platform, args.module)
    print(f"[diff] extracting new surface from {new_src}", file=sys.stderr)
    new_surface = extract_surface(new_src, args.platform, args.module)

    print(f"[diff] old symbols: {sum(len(v) for v in old_surface.by_name.values())} unique names: {len(old_surface.names())}",
          file=sys.stderr)
    print(f"[diff] new symbols: {sum(len(v) for v in new_surface.by_name.values())} unique names: {len(new_surface.names())}",
          file=sys.stderr)

    diff = compute_diff(old_surface, new_surface)

    # Build-manifest extraction + diff
    print(f"[diff] extracting build manifests", file=sys.stderr)
    if args.platform == "android":
        old_build = extract_android_build_manifest(old_src, args.module)
        new_build = extract_android_build_manifest(new_src, args.module)
        build_diff = {"android": compute_build_diff("android", old_build, new_build), "ios": None}
    else:
        old_build = extract_ios_build_manifest(old_src, args.module)
        new_build = extract_ios_build_manifest(new_src, args.module)
        build_diff = {"android": None, "ios": compute_build_diff("ios", old_build, new_build)}

    # Changelog cross-validation panel (read from the new version's source tree)
    print(f"[diff] reading changelog entry for {args.new_version}", file=sys.stderr)
    changelog = extract_changelog_entry(new_src, args.platform, args.module, args.new_version)

    meta = {
        "platform": args.platform,
        "module": args.module,
        "old_version": args.old_version,
        "new_version": args.new_version,
        "source_strategy": "local" if args.local_path else "cache_or_tarball",
        "symbols_old": sum(len(v) for v in old_surface.by_name.values()),
        "symbols_new": sum(len(v) for v in new_surface.by_name.values()),
        "old_source_path": str(old_src),
        "new_source_path": str(new_src),
    }

    payload_extras = {"build": build_diff, "changelog": changelog}

    pair = f"{args.platform}-{args.module}-{args.old_version}-to-{args.new_version}"
    target_dir = args.out_dir / pair
    json_path, md_path = write_outputs({**diff, **payload_extras}, meta, target_dir)

    print(f"[diff] wrote {json_path}")
    print(f"[diff] wrote {md_path}")
    api_summary = f"+{len(diff['added'])} added, -{len(diff['removed'])} removed, ~{len(diff['changed'])} changed"
    build_present = build_diff.get(args.platform) or {}
    build_changes = sum(
        1 for v in build_present.values()
        if v and (not isinstance(v, dict) or v)
    )
    print(f"[diff] api: {api_summary}; build: {build_changes} categories with changes; "
          f"changelog: {'present' if (changelog and changelog.get('entry')) else 'missing'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
