# Pokretanje: pytest tests/test_api.py -v

import pytest
from fastapi.testclient import TestClient

from api.main import app
from pid_main.keys import generate_holder_keypair, public_key_to_jwk
from pid_main.holder import create_presentation


@pytest.fixture
def client():
    with TestClient(app) as client:
        yield client


@pytest.fixture
def holder_key():
    return generate_holder_keypair()


def test_root_returns_ok(client):
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


########################################
# Issuer testovi
########################################

class TestIssuer:

    def test_issue_pid_for_known_user(self, client, holder_key):
        holder_jwk = public_key_to_jwk(holder_key)

        response = client.post(
            "/issuer/issue",
            json={
                "oib": "12345678901",
                "holder_public_jwk": holder_jwk,
            }
        )

        assert response.status_code == 200
        data = response.json()

        # Provjera prezentacije i njenog formata
        assert "sd_jwt_vc" in data
        assert "~" in data["sd_jwt_vc"]

    def test_issue_pid_for_unknown_user(self, client, holder_key):
        holder_jwk = public_key_to_jwk(holder_key)

        response = client.post(
            "/issuer/issue",
            json={
                "oib": "00000000000",
                "holder_public_jwk": holder_jwk,
            }
        )

        assert response.status_code == 404


########################################
# Verifier testovi
########################################

class TestEndToEndVerifier:
    """
    Sveobuhvatni test koji ispituje cijeli flow - od izdavanja PID-a do potvrde prezentacije
    """

    def test_issue_present_verify(self, client, holder_key):
        holder_jwk = public_key_to_jwk(holder_key)

        issue_response = client.post(
            "/issuer/issue",
            json={
                "oib": "12345678901",
                "holder_public_jwk": holder_jwk,
            }
        )

        assert issue_response.status_code == 200
        sd_jwt = issue_response.json()["sd_jwt_vc"]

        challenge_response = client.post("/verifier/challenge")
        assert challenge_response.status_code == 200
        challenge = challenge_response.json()

        presentation = create_presentation(
            sd_jwt_issuance=sd_jwt,
            attributes_to_disclose=["birth_date"],
            holder_private_key=holder_key,
            verifier_audience=challenge["audience"],
            nonce=challenge["nonce"],
        )

        verify_response = client.post(
            "/verifier/verify",
            json={
                "session_id": challenge["session_id"],
                "presentation": presentation,
            }
        )

        assert verify_response.status_code == 200

        verified = verify_response.json()["verified_claims"]
        assert verified["birth_date"] == "1967-06-09"
        assert "given_name" not in verified

    """
    Ispituje moze li se ista sesija koristiti vise puta - ocekivano ponasanje je da NE!
    """

    def test_session_cannot_be_reused(self, client, holder_key):
        holder_jwk = public_key_to_jwk(holder_key)

        sd_jwt = client.post(
            "/issuer/issue",
            json={
                "oib": "12345678901",
                "holder_public_jwk": holder_jwk,
            }
        ).json()["sd_jwt_vc"]

        challenge = client.post("/verifier/challenge").json()

        presentation = create_presentation(
            sd_jwt_issuance=sd_jwt,
            attributes_to_disclose=["given_name"],
            holder_private_key=holder_key,
            verifier_audience=challenge["audience"],
            nonce=challenge["nonce"],
        )

        first_use = client.post(
            "/verifier/verify",
            json={
                "session_id": challenge["session_id"],
                "presentation": presentation,
            }
        )

        assert first_use.status_code == 200

        second_use = client.post(
            "/verifier/verify",
            json={
                "session_id": challenge["session_id"],
                "presentation": presentation,
            }
        )

        assert second_use.status_code == 400
