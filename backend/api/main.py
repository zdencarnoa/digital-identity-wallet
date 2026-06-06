from contextlib import asynccontextmanager

from fastapi import FastAPI

from .issuer_routes import router as issuer_router
from .verifier_routes import router as verifier_router
from .key_store import load_or_generate_issuer_key

"""
Glavni modul FastAPI aplikacije.

Konfigurira aplikaciju, ucitava issuer kljuc pri startu, i registrira
issuer/verifier rute.
Pokretanje: uvicorn api.main:app --reload
"""

"""
Ovo se izvodi samo pri pokretanju i gasenju servera
"""


@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Pokretanje servera, ucitavam issuerov kljuc..")
    app.state.issuer_key = load_or_generate_issuer_key()

    print("Issuerov kljuc spreman.")

    yield

    print("Gasenje servera.")


app = FastAPI(
    title="Demo PID wallet API",
    description="Issuer i verifier za SD_JWT VC PID",
    version="0.1.0",
    lifespan=lifespan,
)

app.include_router(issuer_router)
app.include_router(verifier_router)

"""
Provjera rada servera
"""


@app.get("/", tags=["health"])
def root():
    return {"status": "ok", "service": "demo pid wallet"}
