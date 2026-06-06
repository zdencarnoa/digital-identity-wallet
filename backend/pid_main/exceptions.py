"""Iznimke koje se mogu pojaviti u pid_main modulu."""

# Bazna iznimka za sve greske vezane uz izdavanje i verifikaciju PID-a
class PidError(Exception):
    pass


# Baca se kad SD-JWT prezentacija ne prodje verifikaciju
class PidVerificationError(PidError):
    pass

# Baca se kada issuer odbije izdati PID
class PidIssuanceError(PidError):
    pass
