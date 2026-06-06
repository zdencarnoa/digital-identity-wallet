"""Iznimke koje se mogu pojaviti u FastAPI sloju"""

"""Bazna iznimka za sve greske u API sloju."""
class ApiError(Exception):
    pass

"""OIB nije pronaden u mock korisnicima"""
class UnknownUserError(ApiError):
    pass

"""Verifier sesija s tim ID-om ne postoji ili je istekla"""
class UnknownSessionError(ApiError):
    pass