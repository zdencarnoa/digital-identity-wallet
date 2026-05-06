import time
from typing import Any

from sd_jwt.verifier import SDJWTVerifier
from jwcrypto.jwk import JWK

from .config import SIGNING_ALGORITHM, PID_VCT
from .exceptions import PidVerificationError

def verify_presentation(presentation:str,
                        issuer_public_key: JWK,
                        expected_audience: str,
                        expected_nonce: str,
                        ) -> dict[str, Any]:

    #Biblioteka je napravljena za vise issuera pa ocekuje funkciju
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

    return verified_claims

def _check_vct(claims: dict[str, Any]):
    vct = claims.get("vct")

    if vct != PID_VCT:
        raise PidVerificationError(
            f"Nepodrzan vct: {vct}"
        )

def _check_expiration(claims: dict[str, Any]):
    expiration = claims.get("exp")
    if expiration is None:
        raise PidVerificationError("PID nema istek")

    if int(time.time()) >= expiration:
        raise PidVerificationError("PID je istekao.")
