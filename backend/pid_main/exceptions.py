class PidCoreError(Exception):
    pass

class PidVerificationError(PidCoreError):
    pass

class PidIssuanceError(PidCoreError):
    pass

class InvalidKeyError(PidCoreError):
    pass