"""Verify the exact audited images in a personal APK, or their absence in a public APK."""
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
    expected = {"assets/" + p["path"] for p in photos}
    actual = {name for name in apk.namelist() if name.startswith("assets/photos/") and not name.endswith("/")}
    if args.personal:
        assert actual == expected, f"Asset mismatch: missing {expected - actual}, unexpected {actual - expected}"
        for photo in photos:
            assert hashlib.sha256(apk.read("assets/" + photo["path"])).hexdigest() == photo["sha256"], photo["path"]
    else:
        assert not actual, "Public APK contains personal images"
    print(f"PASS: {'personal' if args.personal else 'public'} APK; {len(actual)} photos; source manifest {len(photos)} entries")
