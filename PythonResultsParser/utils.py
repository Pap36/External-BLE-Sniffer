import binascii

def bytesToHex(bytes):
    return binascii.hexlify(bytes).decode('utf-8')

def hexToBytes(hex):
    return binascii.unhexlify(hex)

def intToBytes(intArray):
    return bytes([int(a) + 256 if a < 0 else int(a) for a in intArray])