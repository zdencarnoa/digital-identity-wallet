import base64
import hashlib
import os

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.exceptions import InvalidKeyError

from backend.pid_main import serialization


def generate_ec_keypair() -> tuple[ec.EllipticCurvePrivateKey, ec.EllipticCurvePublicKey]:
    private_key = ec.generate_private_key(ec.SECP256R1())
    public_key = private_key.public_key()
    return private_key, public_key

def public_key_to_pem (public_key: ec.EllipticCurvePublicKey) -> str:
    pem_bytes = public_key.public_bytes(encoding=serialization.Encoding.PEM,
                                        format=serialization.PublicFormat.SubjectPublicKeyInfo)
    return pem_bytes.decode("utf-8")

def public_key_from_pem(pem_str: str) -> ec.EllipticCurvePublicKey:
    try:
        public_key = serialization.load_pem_public_key(pem_str.encode("utf-8"))
    except Exception:
        raise InvalidKeyError("Neispravan PEM javni kljuc")

    if not isinstance(public_key, ec.EllipticCurvePublicKey):
        raise InvalidKeyError("Kljuc nije EC javni kljuc.")
    return public_key