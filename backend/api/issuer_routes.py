from fastapi import APIRouter, HTTPException, Request

from pid_main.issuer import create_pid
from pid_main.exceptions import PidIssuanceError
from tests.test_pid_main import issuer_key

from .exceptions import UnknownUserError
from .mock_users import get_user_by_oib
from .models import IssueRequest, IssueResponse


router = APIRouter(
    prefix="/issuer",
    tags=["issuer"]
)

#Izdavanje PID-a za korisnika identificiranog s OIB-om
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