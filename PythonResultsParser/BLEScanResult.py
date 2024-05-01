from utils import *

class BLEScanResult:
    def __init__(self, rssi, adv_type, addr_type, addr, data, source, timeStamp):
        self.rssi = rssi
        self.adv_type = adv_type
        self.addr_type = addr_type
        # addr is a list of signed integers corresponding to bytes
        addr = intToBytes(addr)
        self.address = bytesToHex(addr)
        self.scanData = data
        self.localName = ""
        self.source = source
        self.timeStamp = timeStamp
        self.checkLocalName()

    def __str__(self):
        return f"BLEScanResult({self.address}, {self.rssi}, {self.scanData})"

    def __repr__(self):
        return str(self)
    
    def checkLocalName(self):
        # check if the scanData contains a local name:
        # 0x09 is the data type for local name
        toProcess = self.scanData
        while len(toProcess) > 0:
            length, dataType = toProcess[:2]
            data = toProcess[2:1+length]
            if dataType == 0x09 or dataType == 0x08:
                localNameBytes = intToBytes(data)
                self.localName = localNameBytes.decode("utf-8")
                return
            toProcess = toProcess[1+length:]

    def getAddress(self):

        return self.address

        # if self.localName == "":
        #     return self.address
        # else:
        #     return self.localName 