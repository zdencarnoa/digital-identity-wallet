from typing import Iterable
from sd_jwt.holder import SDJWTHolder
from jwcrypto.jwk import JWK

from .config import SIGNING_ALGORITHM

"""
Simulacija holder strane namijenjena za testove

Ovo inace radi Android wallet kroz PresentationBuilder klasu.
Ovaj modul postoji samo radi unit testova koji testiraju
issuer/verifier kombinaciju bez stvarnog mobilnog uredaja.
"""


def create_presentation(
        sd_jwt_issuance: str,
        attributes_to_disclose: Iterable[str],
        holder_private_key: JWK,
        verifier_audience: str,
        nonce: str, ) -> str:
    holder = SDJWTHolder(sd_jwt_issuance, serialization_format="compact")
    disclosure_map = {name: True for name in attributes_to_disclose}

    holder.create_presentation(
        claims_to_disclose=disclosure_map,
        nonce=nonce,
        aud=verifier_audience,  # zastita od replay napada -> postoji tocno jedan primatelj
        holder_key=holder_private_key,
        sign_alg=SIGNING_ALGORITHM,
    )

    return holder.sd_jwt_presentation
