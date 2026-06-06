import time
from typing import Any

from sd_jwt.verifier import SDJWTVerifier
from jwcrypto.jwk import JWK

from .config import PID_VCT
from .exceptions import PidVerificationError

"""
Verifikacija SD-JWT VC prezentacija koje wallet salje verifier-u
Provjerava:
 - Issuer-ov potpis SD-JWT-a 
 - Key Binding JWT potpis holder-a
 - sd_hash (KB-JWT-a uz konkretnu prezentaciju)
 - Audience claim (sprjecava krivu uporabu prema drugom verifier-u)
 - Nonce claim (sprjecava replay napad)
 - vct - osigurava pravi format vjerodajnice
 - exp - osigurava da PID nije istekao
"""

def verify_presentation(
        presentation: str,
        issuer_public_key: JWK,
        expected_audience: str,
        expected_nonce: str,
) -> dict[str, Any]:
    # Biblioteka je napravljena za vise issuera pa ocekuje funkciju
    # demo ima samo jednog issuera pa se parametri ignoriraju i vraca se specifican kljuc
    def _cb_get_issuer_key(issuer: str, header: dict) -> JWK:
        return issuer_public_key

    try:
        verifier = SDJWTVerifier(
            sd_jwt_presentation=presentation,
            cb_get_issuer_key=_cb_get_issuer_key,
            expected_aud=expected_audience,
            expected_nonce=expected_nonce,
            serialization_format="compact",
        )
        verified_claims = verifier.get_verified_payload()
    except Exception as e:
        raise PidVerificationError(f"Neuspjesna provjera SD-JWT-a: {e}") from e

    _check_vct(verified_claims)
    _check_expiration(verified_claims)
    _check_iat(verified_claims)

    return verified_claims


def _check_vct(claims: dict[str, Any]) -> None:
    vct = claims.get("vct")

    if vct != PID_VCT:
        raise PidVerificationError(
            f"Nepodrzan vct: {vct}"
        )


def _check_expiration(claims: dict[str, Any]) -> None:
    expiration = claims.get("exp")
    if expiration is None:
        raise PidVerificationError("PID nema istek")

    leeway_seconds = 60
    now = int(time.time())
    if now - leeway_seconds >= expiration:
        raise PidVerificationError("PID je istekao.")

def _check_iat(claims: dict[str, Any]) -> None:
    iat = claims.get("iat")
    if iat is None:
        raise PidVerificationError("PID nema datum izdavanja")

    # PID ne moze biti izdan u buducnosti (mala tolerancija za clock skew)
    leeway_seconds = 60
    now = int(time.time())
    if iat > now + leeway_seconds:
        raise PidVerificationError("PID datum izdavanja je u buducnosti")
