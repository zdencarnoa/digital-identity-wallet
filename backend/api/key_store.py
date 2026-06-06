"""
Pohrana i dohvacanje issuer kljuca.

Kljuc mora biti stalan -> generira se pri prvom pokretanju

SIGURNOSNO: Za demo, privatni kljuc se sprema u nezasticeni JSON file
U stvarnom sustavu bi se koristio HSM (Hardware Security Module) ili slicni KMS sustavi.
Issuer kljuc je posebno osjetljiv jer u slucaju da napadac dode do njega moze izdavati lazne PID-ove
"""

import json
from pathlib import Path
from jwcrypto.jwk import JWK
from pid_main.keys import generate_issuer_keypair

DEFAULT_KEY_PATH = Path(__file__).resolve().parent.parent / "keys" / "issuer.jwk.json"


def load_or_generate_issuer_key(path: Path = DEFAULT_KEY_PATH) -> JWK:
    path.parent.mkdir(parents=True, exist_ok=True)

    if path.exists():
        return _load_from_file(path)

    print(f"[key_store] Issuer kljuc ne postoji. Generiram novi i spremam u {path}")
    key = generate_issuer_keypair()
    _save_to_file(key, path)
    return key


def _load_from_file(path: Path) -> JWK:
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    key = JWK()
    key.import_key(**data)
    return key


def _save_to_file(key: JWK, path: Path) -> None:
    data = key.export(as_dict=True)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
