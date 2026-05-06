import time
from typing import Any

from sd_jwt.common import SDObj
from sd_jwt.issuer import SDJWTIssuer
from jwcrypto.jwk import JWK

from .config import PID_VCT, SD_DISCLOSABLE_ATTRIBUTES, SIGNING_ALGORITHM, DEFAULT_VALIDITY_DAYS, DEFAULT_ISSUER

from .exceptions import PidIssuanceError

def _build_pid_claims(attributes: dict[str, Any]
                      , holder_public_jwk: dict
                      , issuer_id: str
                      , validity_days: int) -> dict[str, Any]:
    now = int(time.time())
    expiration = now + validity_days * 24 * 60 * 60

    unknown = set(attributes.keys()) - SD_DISCLOSABLE_ATTRIBUTES
    if unknown:
        raise PidIssuanceError(
            f"Postoje nepoznati atributi za PID: {unknown}"
        )

    claims: dict[Any, Any] = {
        "iss": issuer_id,
        "vct": PID_VCT,
        "iat": now,
        "exp": expiration,
        "cnf": {"jwk": holder_public_jwk},
    }

    for name, value in attributes.items():
        claims[SDObj(name)] = value

    return claims

def create_pid(
        attributes: dict[str, Any],
        holder_public_jwk: dict,
        issuer_private_key: JWK,
        issuer_id: str,
        validity_days: int
):
    claims = _build_pid_claims(attributes, holder_public_jwk, issuer_id, validity_days)

    issuer = SDJWTIssuer(
        user_claims=claims,
        issuer_keys=issuer_private_key,
        sign_alg=SIGNING_ALGORITHM,
        add_decoy_claims=False,
        serialization_format="compact"
    )

    return issuer.sd_jwt_issuance
