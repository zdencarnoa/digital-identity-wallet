"""
JWK je JSON oblik za kriptografske kljuceve
Potrebno za SD-JWT VC jer ne koristi PEM format
"""

from jwcrypto.jwk import JWK
from .config import SIGNING_ALGORITHM
from .exceptions import InvalidKeyError

def generate_issuer_keypair() -> JWK:
    return JWK.generate(kty="EC", crv="P-256", alg=SIGNING_ALGORITHM)

#Samo za testiranje, kasnije Android Keystore
def generate_holder_keypair() -> JWK:
    return JWK.generate(kty="EC", crv="P-256", alg=SIGNING_ALGORITHM)

def public_key_to_jwk(keypair: JWK) -> dict:
    public_only = JWK()
    public_only.import_key(**keypair.export_public(as_dict=True))

    return public_only.export_public(as_dict=True)

def private_key_to_jwk(keypair: JWK) -> dict:
    return keypair.export(as_dict=True)

def jwk_from_dict(jwk_dict: dict) -> JWK:
    try:
        key = JWK()
        key.import_jwk(**jwk_dict)
        return key
    except InvalidKeyError as e:
        raise InvalidKeyError(f"Neispravan kljuc: {e}") from e