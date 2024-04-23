import json
import matplotlib.pyplot as plt

from BLEScanResult import BLEScanResult


def read_data(filename):
    # open a json file and parse the data using BLESanResult class
    with open(filename, 'r') as f:
        data = json.load(f)

    # return an array of BLEScanResult objects
    return [BLEScanResult(**d) for d in data]

def split_data(data):
    # split data by source (usb, ble)
    return [d for d in data if d.source == 'USB'], [d for d in data if d.source == 'BLE']

def plot_callback_count(usb_data, ble_data, filename=""):
    # plot the number of callbacks for each device
    callbacks = [index for index, d in enumerate(usb_data)]
    timeStamps = [d.timeStamp - usb_data[0].timeStamp for d in usb_data]
    # convert timeStamps to seconds
    timeStamps = [t / 1000 for t in timeStamps]
    plt.figure()
    plt.plot(timeStamps, callbacks, label='USB')
    

    callbacks = [index for index, d in enumerate(ble_data)]
    timeStamps = [d.timeStamp - ble_data[0].timeStamp for d in ble_data]
    # convert timeStamps to seconds
    timeStamps = [t / 1000 for t in timeStamps]
    plt.plot(timeStamps, callbacks, label = 'BLE')
    plt.ylabel('Callback Index')
    plt.xlabel('Timestamp (s)')
    plt.title('USB vs BLE Callbacks - ' + filename)
    plt.legend()

def populateLocalNameData(data):
    # populate the local name data for each device
    nameAddressDict = {}
    for d in data:
        if d.address not in nameAddressDict and d.localName != "":
            nameAddressDict[d.address] = d.localName
    print(nameAddressDict)
    
    for d in data:
        if d.address in nameAddressDict:
            d.localName = nameAddressDict[d.address]

    return data

def extract_addresses(data):
    # extract the addresses from the data and the number of callbacks for each address
    return [d.address if d.localName == "" else d.localName for d in data]

def centraliseAdvTypeUSB(data, addresses):
    typeDict = {}
    for addr in addresses:
        typeDict[addr] = {}
        types = [d.adv_type for d in data if d.address == addr or d.localName == addr]
        for t in types:
            if t not in typeDict[addr]:
                typeDict[addr][t] = 1
            else:
                typeDict[addr][t] += 1
        # sort the dictionary by the number of callbacks
        typeDict[addr] = dict(sorted(typeDict[addr].items(), key=lambda x: x[1], reverse=True))
    return typeDict

def print_address_count_comparison(usb_data, ble_data, filename=""):
    # plot the number of callbacks for each device
    usb_data = populateLocalNameData(usb_data)
    ble_data = populateLocalNameData(ble_data)
    usb_addresses = extract_addresses(usb_data)
    ble_addresses = extract_addresses(ble_data)
    all_addresses = list(set(usb_addresses + ble_addresses))
    usb_address_count = {a: usb_addresses.count(a) for a in all_addresses}
    ble_address_count = {a: ble_addresses.count(a) for a in all_addresses}
    average_rssi_address_usb = {a: round(sum([d.rssi for d in usb_data if d.address == a or d.localName == a]) / 
        (usb_address_count[a] if usb_address_count[a] != 0 else 1), 2) for a in all_addresses}
    average_rssi_address_ble = {a: round(sum([d.rssi for d in ble_data if d.address == a or d.localName == a]) / 
        (ble_address_count[a] if ble_address_count[a] != 0 else 1), 2) for a in all_addresses}
    usb_typeDict = centraliseAdvTypeUSB(usb_data, all_addresses)
    
    # zip together the addresses, number of callbacks and average rssi's and order by rssi_usb
    zipped = sorted(list(zip(all_addresses, usb_address_count.values(), average_rssi_address_usb.values(), 
        ble_address_count.values(), average_rssi_address_ble.values(), usb_typeDict.values())), key=lambda x: x[2], reverse=True)
    print(f'Address count comparison for {filename}:')
    zipped.insert(0, ('Address', 'USB Count', 'Average RSSI USB', 'BLE Count', 'Average RSSI BLE', 'USB Adv Types'))
    for a in zipped:
        print(f'{a[0]:<30} {a[1]:>15} {a[2]:>20} {a[3]:>15} {a[4]:>20} \t{a[5]}')

def print_address_count(data, filename=""):
    # plot the number of callbacks for each device
    addresses = list(set(extract_addresses(data)))
    address_count = {a: addresses.count(a) for a in addresses}
    print(f'Address count for {filename}:')
    for a in address_count:
        print(f'{a}\t:\t{address_count[a]}')
