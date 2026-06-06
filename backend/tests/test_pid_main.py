import pytest

from pid_main.config import PID_VCT
from pid_main.exceptions import PidIssuanceError, PidVerificationError
from pid_main.keys import (
    generate_issuer_keypair,
    generate_holder_keypair,
    public_key_to_jwk,
)
from pid_main.issuer import create_pid
from pid_main.holder import create_presentation
from pid_main.verifier import verify_presentation


@pytest.fixture
def issuer_key():
    return generate_issuer_keypair()


@pytest.fixture
def holder_key():
    return generate_holder_keypair()


@pytest.fixture
def dummy_attributes():
    return {
        "given_name": "Jane",
        "family_name": "Doe",
        "birth_date": "1967-06-09",
        "nationality": "HR",
        "personal_administrative_number": "12345678901",
    }


@pytest.fixture
def issued_pid(issuer_key, holder_key, dummy_attributes):
    holder_public_jwk = public_key_to_jwk(holder_key)
    return create_pid(
        attributes=dummy_attributes,
        holder_public_jwk=holder_public_jwk,
        issuer_private_key=issuer_key,
    )


########################################
# Issuer testovi - provjeravaju kreira li se pid na ocekivan nacin
########################################

class TestIssuance:

    def test_create_pid_returns_string(self, issued_pid):
        assert isinstance(issued_pid, str)
        assert len(issued_pid) > 0

    def test_issued_pid_is_compact(self, issued_pid):
        assert "~" in issued_pid

    def test_unknown_attribute_raises_exception(self, issuer_key, holder_key):
        holder_public_jwk = public_key_to_jwk(holder_key)
        with pytest.raises(PidIssuanceError):
            create_pid(
                attributes={"krivo": "neispravan atribut"},
                holder_public_jwk=holder_public_jwk,
                issuer_private_key=issuer_key,
            )


########################################
# Verifikacija prezentacije - provjerava se prikazuju li se samo ocekivani podatci
########################################

VERIFIER_AUDIENCE = "https://demo-verifier.fer.hr"
TEST_NONCE = "nonce-1234567890"


class TestVerification:

    def test_disclose_all(self, issued_pid, holder_key, issuer_key, dummy_attributes):
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=dummy_attributes.keys(),
            holder_private_key=holder_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        verified = verify_presentation(
            presentation=presentation,
            issuer_public_key=issuer_key,
            expected_audience=VERIFIER_AUDIENCE,
            expected_nonce=TEST_NONCE,
        )

        for name, value in dummy_attributes.items():
            assert verified[name] == value

        assert verified["vct"] == PID_VCT
        assert "iss" in verified
        assert "iat" in verified
        assert "exp" in verified
        assert "cnf" in verified

    def test_selective_disclosure_age(self, issued_pid, holder_key, issuer_key, dummy_attributes):
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=["birth_date"],
            holder_private_key=holder_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        verified = verify_presentation(
            presentation=presentation,
            issuer_public_key=issuer_key,
            expected_audience=VERIFIER_AUDIENCE,
            expected_nonce=TEST_NONCE,
        )

        assert verified["birth_date"] == "1967-06-09"
        assert "given_name" not in verified
        assert "family_name" not in verified
        assert "personal_administrative_number" not in verified


########################################
# Fail slucajevi
########################################

class TestVerificationFailures:

    def test_wrong_nonce_fails(self, issued_pid, holder_key, issuer_key):
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=["given_name"],
            holder_private_key=holder_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        with pytest.raises(PidVerificationError):
            verify_presentation(
                presentation=presentation,
                issuer_public_key=issuer_key,
                expected_audience=VERIFIER_AUDIENCE,
                expected_nonce="krivi-nonce",
            )

    def test_wrong_audience_fails(self, issued_pid, holder_key, issuer_key):
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=["given_name"],
            holder_private_key=holder_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        with pytest.raises(PidVerificationError):
            verify_presentation(
                presentation=presentation,
                issuer_public_key=issuer_key,
                expected_audience="https://krivi-verifier.primjer",
                expected_nonce=TEST_NONCE,
            )

    def test_wrong_issuer_fails(self, issued_pid, holder_key, issuer_key):
        wrong_issuer_key = generate_issuer_keypair()
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=["given_name"],
            holder_private_key=holder_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        with pytest.raises(PidVerificationError):
            verify_presentation(
                presentation=presentation,
                issuer_public_key=wrong_issuer_key,
                expected_audience=VERIFIER_AUDIENCE,
                expected_nonce=TEST_NONCE,
            )

    # Ovaj test provjerava sto se dogodi kada napadac ima ukradeni sd-jwt
    def test_kb_jwt_not_by_holder(self, issued_pid, holder_key, issuer_key):
        attacker_key = generate_holder_keypair()
        presentation = create_presentation(
            sd_jwt_issuance=issued_pid,
            attributes_to_disclose=["given_name"],
            holder_private_key=attacker_key,
            verifier_audience=VERIFIER_AUDIENCE,
            nonce=TEST_NONCE,
        )

        with pytest.raises(PidVerificationError):
            verify_presentation(
                presentation=presentation,
                issuer_public_key=issuer_key,
                expected_audience=VERIFIER_AUDIENCE,
                expected_nonce=TEST_NONCE,
            )
