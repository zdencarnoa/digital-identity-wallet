from fastapi import APIRouter, HTTPException, Request

from pid_main.issuer import create_pid
from pid_main.exceptions import PidIssuanceError

from .exceptions import UnknownUserError
from .mock_users import get_user_by_oib
from .models import IssueRequest, IssueResponse

"""
HTTP routes za issuer endpoint-e.

Definira /issuer/issue (izdavanje PID-a) i /issuer/public-key (dohvat javnog kljuca
za verifikaciju).
Issuer kljuc se ucitava jednom pri pokretanju aplikacije i drzi u
app.state - ovdje samo cita referencu.
"""

router = APIRouter(
    prefix="/issuer",
    tags=["issuer"]
)


# Izdavanje PID-a za korisnika identificiranog s OIB-om
@router.post("/issue", response_model=IssueResponse)
def issue_pid(payload: IssueRequest, request: Request) -> IssueResponse:
    try:
        attributes = get_user_by_oib(payload.oib)
    except UnknownUserError as e:
        raise HTTPException(status_code=404, detail=str(e))

    issuer_key = request.app.state.issuer_key

    try:
        sd_jwt_vc = create_pid(
            attributes=attributes,
            holder_public_jwk=payload.holder_public_jwk,
            issuer_private_key=issuer_key,
        )
    except PidIssuanceError as e:
        raise HTTPException(status_code=400, detail=str(e))

    return IssueResponse(sd_jwt_vc=sd_jwt_vc)


@router.get("/public-key")
def get_issuer_public_key(request: Request):
    issuer_key = request.app.state.issuer_key
    return issuer_key.export_public(as_dict=True)
