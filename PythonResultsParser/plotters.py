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

def extract_callbacks_timestamps(data):
    # extract the callbacks and timestamps from the data
    # remove the first values to account for errors from left-over recorded results
    data = data[10:]
    callbacks = [index for index, d in enumerate(data)]
    timeStamps = [d.timeStamp - data[0].timeStamp for d in data]
    
    # convert timeStamps to seconds
    timeStamps = [t / 1000 for t in timeStamps]
    return callbacks, timeStamps

def average_callbacks_timestamps(dataSources):
    average_callbacks = []
    average_timeStamps = []

    all_timestamps = []

    for data in dataSources:
        _, timeStamps = extract_callbacks_timestamps(data)
        all_timestamps += timeStamps

    average_timeStamps = list(set(all_timestamps))
    average_timeStamps.sort()
    average_callbacks = [0] * len(average_timeStamps)


    for data in dataSources:
        callbacks, timeStamps = extract_callbacks_timestamps(data)
        lastKnownTimestamp = 0
        for index, t in enumerate(average_timeStamps):
            if t in timeStamps:
                lastKnownTimestamp = t
                timeStampsIndex = timeStamps.index(t)
            else:
                timeStampsIndex = timeStamps.index(lastKnownTimestamp)
            average_callbacks[index] = average_callbacks[index] + callbacks[timeStampsIndex]

    average_callbacks = [c / len(dataSources) for c in average_callbacks]
    return average_callbacks, average_timeStamps
    
def plot_average(dataSources, source=""):
    # scatter plot the number of callbacks over the average time
    callbackAverage, timeStampAverage = average_callbacks_timestamps(dataSources)
    plt.plot(timeStampAverage, callbackAverage, label=source)
    plt.ylabel('Number of Callbacks')
    plt.xlabel('Time (s)')
    plt.title('Number of Callbacks vs Time')
    plt.legend()

def normalize_timestamps(usb, ble):
    # normalize the timestamps for each data source
    usb = usb[10:]
    ble = ble[10:]
    base = usb[0].timeStamp
    for u in usb:
        u.timeStamp = u.timeStamp - base
    base = ble[0].timeStamp
    for b in ble:
        b.timeStamp = b.timeStamp - base
    return usb, ble

def plot_callback_rate_scenario(dataSources, prefix=""):
    # plot the number of callbacks per second
    usbs = []
    bles = []
    for file in dataSources:
        usb, ble = split_data(read_data(prefix + '/' + file))
        usb, ble = normalize_timestamps(usb, ble)
        usbs += usb
        bles += ble

    usbs.sort(key=lambda x: x.timeStamp)
    bles.sort(key=lambda x: x.timeStamp)

    plot_callback_per_second_rate(usbs, bles, prefix, averageFactor=len(dataSources))


def plot_callback_per_second_rate(usb, ble, filename="", averageFactor=1):
    # plot the number of callbacks per second
    usb = usb[10:]
    ble = ble[10:]
    callbacks, timeStamps = extract_callbacks_timestamps(usb)
    callbacks = callbacks[100:]
    timeStamps = timeStamps[100:]
    callbacksPerSecond = [(callbacks[i] / timeStamps[i]) / averageFactor for i in range(len(callbacks))]
    plt.figure()
    plt.plot(timeStamps, callbacksPerSecond, label='USB')

    callbacks, timeStamps = extract_callbacks_timestamps(ble)
    callbacks = callbacks[100:]
    timeStamps = timeStamps[100:]
    callbacksPerSecond = [(callbacks[i] / timeStamps[i]) / averageFactor for i in range(len(callbacks))]
    plt.plot(timeStamps, callbacksPerSecond, label='BLE')
    plt.ylabel('Number of Callbacks per Second')
    plt.xlabel('Timestamp (s)')
    plt.title('USB vs BLE Callbacks per Second - ' + filename)
    plt.legend()


def plot_callback_count(usb_data, ble_data, filename=""):
    # plot the number of callbacks for each device
    usb_data = usb_data[10:]
    ble_data = ble_data[10:]
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
