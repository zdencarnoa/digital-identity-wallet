"""
Testni korisnici.

Simulira podatke dobivene putem eOsobne
"""

from .exceptions import UnknownUserError

MOCK_USERS: dict[str, dict] = {
    "12345678901": {
        "given_name": "Jane",
        "family_name": "Doe",
        "birth_date": "1967-06-09",
        "nationality": "HR",
        "personal_administrative_number": "12345678901",
    },
    "98765432109": {
        "given_name": "John",
        "family_name": "Doe",
        "birth_date": "2003-11-22",
        "nationality": "HR",
        "personal_administrative_number": "98765432109",
    },
    "11122233344": {
        "given_name": "Messmer",
        "family_name": "Impaler",
        "birth_date": "2002-10-12",
        "nationality": "HR",
        "personal_administrative_number": "11122233344",
    },
}


def get_user_by_oib(oib: str) -> dict:
    if oib not in MOCK_USERS:
        raise UnknownUserError(f"Ne postoji korisnik s OIB-om {oib}")
    return dict(MOCK_USERS[oib])
