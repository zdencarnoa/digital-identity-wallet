"""
Definicija PID formata na osnovi SD-JWT VC formata
VCT nije standardan, oznacava da se radi o demo verziji sustava
"""
PID_VCT = "urn:hr.fer.demo:pid:1"

#Algoritam za potpisivanje
SIGNING_ALGORITHM = "ES256"

#Hash algoritam za selective disclosure
HASH_ALGORITHM = "sha-256"

SD_DISCLOSABLE_ATTRIBUTES = {
    "given_name",
    "family_name",
    "birth_date",
    "nationality",
    "personal_administrative_number", #OIB
    "issuance_date",
    "expiry_date",
}

DEFAULT_VALIDITY_DAYS = 365

DEFAULT_ISSUER = "https://demo-issuer.fer.hr"