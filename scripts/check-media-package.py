"""Verify packaged image hashes; public builds include generated assets only."""
import argparse
import hashlib
import json
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument("apk")
parser.add_argument("--personal", action="store_true")
args = parser.parse_args()
with zipfile.ZipFile(args.apk) as apk:
    photos = json.loads(apk.read("assets/teaching-media.json"))
    included = [p for p in photos if args.personal or p['path'].startswith('generated/')]
    expected = {"assets/" + p["path"] for p in included}
    actual = {name for name in apk.namelist() if name.startswith(("assets/photos/", "assets/generated/")) and not name.endswith("/")}
    assert actual == expected, f"Asset mismatch: missing {expected - actual}, unexpected {actual - expected}"
    for photo in included:
        assert hashlib.sha256(apk.read("assets/" + photo["path"])).hexdigest() == photo["sha256"], photo["path"]
    print(f"PASS: {'personal' if args.personal else 'public'} APK; {len(actual)} photos; source manifest {len(photos)} entries")
