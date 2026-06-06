"""
Konfiguracija PID formata na osnovi SD-JWT VC formata

"""

# VCT nije standardan, oznacava da se radi o demo verziji sustava
PID_VCT = "urn:hr.fer.demo:pid:1"

# Issuer identifier, iss claim
DEFAULT_ISSUER = "https://demo-issuer.fer.hr"

# Algoritam za potpisivanje issuera i KB-JWT-a
SIGNING_ALGORITHM = "ES256"

# Hash algoritam za izradu sazetaka za selective disclosure
HASH_ALGORITHM = "sha-256"


# Atributi PID-a koje je moguce selektivno otkrivati
SD_DISCLOSABLE_ATTRIBUTES = {
    "given_name",
    "family_name",
    "birth_date",
    "nationality",
    "personal_administrative_number",   #OIB
}

# Podrazumijevano trajanje PID-a u danima
DEFAULT_VALIDITY_DAYS = 365

