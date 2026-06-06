from pydantic import BaseModel, Field

"""
Pydantic modeli za FastAPI request i response sheme.

Modeli definiraju strukturu JSON-a koji prolazi izmedu wallet-a i backend-a

"""


# Zahtjev za izdavanjem PID-a
class IssueRequest(BaseModel):
    oib: str = Field(
        description="OIB korisnika koji predaje zahtjev za PID",
        examples=["12345678901"],
    )
    holder_public_jwk: dict = Field(
        description="Javni kljuc korisnika u JWK formatu, ugraduje se u cnf PID-a"
    )


# Odgovor s izdanim PID-om
class IssueResponse(BaseModel):
    sd_jwt_vc: str = Field(
        description="SD-JWT VC u compact obliku"
    )


# Odgovor na zahtjev za novi challenge
class ChallengeResponse(BaseModel):
    session_id: str = Field(
        description="Session ID."
    )

    nonce: str = Field(
        description="Jednokratni broj koji je dio KB-JWT-a"
    )

    audience: str = Field(
        description="Identifier verifier-a, aud polje u KB-JWT"
    )


# Zahtjev za provjeru SD-JWT prezentacije
class VerifyRequest(BaseModel):
    session_id: str = Field(
        description="Session ID iz challenge endpoint-a."
    )

    presentation: str = Field(
        description="SD-JWT VC presentacija, sadrzi KB_JWT"
    )


# Odgovor s verificiranim atributima
class VerifyResponse(BaseModel):
    verified_claims: dict = Field(
        description="Provjereni atributi PID-a, samo otkriveni - selective disclosure."
    )
