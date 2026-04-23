from dataclasses import dataclass, field
from typing import Optional


@dataclass
class AttributeDigest:
    name: str
    digest: str         #hex SHA-256
    value: Optional[str] = None
    salt: Optional[str] = None

@dataclass
class PID:
    issuer_id: str
    publiv_key_pem: str                   #javni kljuc korisnika
    attributes: list[AttributeDigest]
    issued_at: str
    expires_at: str
    signature: str                        #b64 enkodirani potpis issuer-a

@dataclass
class DisclosedPID:

    issuer_id: str
    publiv_key_pem: str
    attributes: list[AttributeDigest]     #samo neki atributi sadrze salt - oni koje treba prikazati
    issued_at: str
    expires_at: str
    signature: str