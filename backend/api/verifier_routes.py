from fastapi import APIRouter, HTTPException, Request

from pid_main.verifier import verify_presentation
from pid_main.exceptions import PidVerificationError
from tests.test_pid_main import VERIFIER_AUDIENCE

from . import nonce_storage
from .exceptions import UnknownSessionError
from .models import ChallengeResponse, VerifyRequest, VerifyResponse

VERIFIER_AUDIENCE = "https://demo-verifier.fer.hr"

router = APIRouter(
    prefix="/verifier",
    tags=["verifier"],
)

"""
Stvaranje nove sesije - priprema za prikaz prezentacije
"""
@router.post("/challenge", response_model=ChallengeResponse)
def create_challenge() -> ChallengeResponse:

    session_id, nonce = nonce_storage.create_session()
    return ChallengeResponse(
        session_id=session_id,
        nonce=nonce,
        audience=VERIFIER_AUDIENCE,
    )


"""
Provjera SD-JWT VC prezentacije, ocekuje se da je nonce vec dobiven iz challenge-a

Vraca samo otkrivene atribute
"""
@router.post("/verify", response_model=VerifyResponse)
def verify(payload: VerifyRequest, request: Request) -> VerifyResponse:

    try:
        nonce = nonce_storage.get_nonce(payload.session_id)
    except UnknownSessionError as e:
        raise HTTPException(status_code=400, detail=str(e))

    issuer_key = request.app.state.issuer_key

    try:
        verified_claims = verify_presentation(
            presentation=payload.presentation,
            issuer_public_key=issuer_key,
            expected_audience=VERIFIER_AUDIENCE,
            expected_nonce=nonce,
        )

    except PidVerificationError as e:
        raise HTTPException(status_code=400, detail=f"Prezentacija nije valjana {e}")

    nonce_storage.consume_session(payload.session_id)

    return VerifyResponse(verified_claims=verified_claims)