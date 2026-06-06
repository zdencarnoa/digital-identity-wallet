"""
JWK je JSON oblik za kriptografske kljuceve
Potrebno za SD-JWT VC jer ne koristi PEM format
"""

from jwcrypto.jwk import JWK
from .config import SIGNING_ALGORITHM

def generate_issuer_keypair() -> JWK:
    return JWK.generate(kty="EC", crv="P-256", alg=SIGNING_ALGORITHM)

# Generira holderov par kljuceva - samo za TESTIRANJE
def generate_holder_keypair() -> JWK:
    return JWK.generate(kty="EC", crv="P-256", alg=SIGNING_ALGORITHM)

def public_key_to_jwk(keypair: JWK) -> dict[str, str]:
    return keypair.export_public(as_dict=True)

def private_key_to_jwk(keypair: JWK) -> dict[str, str]:
    return keypair.export(as_dict=True)
