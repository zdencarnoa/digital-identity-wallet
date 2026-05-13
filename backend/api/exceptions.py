class ApiError(Exception):
    pass


class UnknownUserError(ApiError):
    #OIB nije pronaden u mock korisnicima
    pass


class UnknownSessionError(ApiError):
    #Verifier sesija s tim ID-om ne postoji ili je istekla.
    pass