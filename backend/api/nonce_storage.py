import secrets
import time

from .exceptions import UnknownSessionError

# Trajanje sesije u sekundama
SESSION_DURATION = 5 * 60

_sessions: dict[str, tuple[str, float]] = {}


# Kreiranje nove sesije - nonce, session id i istek trajanja
def create_session() -> tuple[str, str]:

    session_id = secrets.token_urlsafe(16)
    nonce = secrets.token_urlsafe(32)
    expires_at = time.time() + SESSION_DURATION

    _sessions[session_id] = (nonce, expires_at)
    return session_id, nonce


# Dohvacanje noncea za danu sesiju, provjera valjanosti sesije
def get_nonce(session_id: str) -> str:

    if session_id not in _sessions:
        raise UnknownSessionError(f"Sesija {session_id} ne postoji")

    nonce, expires_at = _sessions[session_id]

    if time.time() >= expires_at:

        del _sessions[session_id]
        raise UnknownSessionError(f"Sesija {session_id} je istekla")

    return nonce

# Brisanje sesije nakon provjere prezentacije, obrana od replay napada
def consume_session(session_id: str) -> None:

    _sessions.pop(session_id, None)


# Povremeno ciscenje isteklih sesija, vraca broj isteklih sesija od posljednjeg brisanja
def cleanup_expired() -> int:

    now = time.time()
    expired = [sid for sid, (nonce, expires_at) in _sessions.items() if now >= expires_at]

    for sid in expired:
        del _sessions[sid]

    return len(expired)