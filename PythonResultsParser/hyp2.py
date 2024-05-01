import os
from typing import List
from BLEScanResult import BLEScanResult
from plotters import read_data, split_data
from utils import dict_to_latex, print_callback_rate_dict_to_latex

def are_equal(usb_res: BLEScanResult, ble_res: BLEScanResult):
    return usb_res.getAddress() == ble_res.getAddress() and \
        usb_res.scanData == ble_res.scanData and abs(usb_res.timeStamp - ble_res.timeStamp) <= 20

def group_by_address(data: List[BLEScanResult]):
    res = {}
    for d in data:
        if d.getAddress() not in res:
            res[d.getAddress()] = []
        res[d.getAddress()].append(d)
    return res

def process_results(file, isPrint=True):
    usb, ble = split_data(read_data(file))

    usb = usb[10:]
    ble = ble[10:]

    usb_grouped = group_by_address(usb)
    ble_grouped = group_by_address(ble)

    results = {}

    for address in usb_grouped.keys():
        if address in ble_grouped:
            results[address] = (len(usb_grouped[address]), 0, len(ble_grouped[address]), 0)
        else:
            results[address] = (len(usb_grouped[address]), 0, 0, 0)
            ble_grouped[address] = []

    for address in ble_grouped.keys():
        if address not in results:
            results[address] = (0, 0, len(ble_grouped[address]), 0)
            usb_grouped[address] = []

    # count for each address how many were scanned by usb and not by ble
    for index, address in enumerate(results.keys()):
        if isPrint:
            print("Processing ", round(index / len(results.keys()) * 100, 2), "%" )
        usb_count_only = 0
        usb_average_rssi = round(sum([d.rssi for d in usb_grouped[address]]) / len(usb_grouped[address]) \
            if len(usb_grouped[address]) != 0 else 0.00, 2)
        ble_average_rssi = round(sum([d.rssi for d in ble_grouped[address]]) / len(ble_grouped[address]) \
            if len(ble_grouped[address]) != 0 else 0.00, 2)
        for result in usb_grouped[address]:
            found = False
            for ble_res in ble_grouped[address]:
                if are_equal(result, ble_res):
                    found = True
                    break
            if not found:
                usb_count_only += 1
        ble_count_only = 0
        for result in ble_grouped[address]:
            found = False
            for usb_res in usb_grouped[address]:
                if are_equal(result, usb_res):
                    found = True
                    break
            if not found:
                ble_count_only += 1
        results[address] = (len(usb_grouped[address]), usb_count_only, usb_average_rssi,
            len(ble_grouped[address]), ble_count_only,  ble_average_rssi)

    return results

def get_errors_for_file(file, isPrint=True):
    results = process_results(file, isPrint=isPrint)
    if isPrint:
        print("Address \t\t USB Count \t USB no BLE Count \t Avg USB RSSI \t BLE Count \t BLE no USB Count \t Avg BLE RSSI \t Error \t Performance")
    
    file_ble_count = 0
    file_ble_only_count = 0

    flie_usb_count = 0
    file_usb_only_count = 0
    
    for address, counts in results.items():
        error = round(counts[4] / counts[3] * 100, 2) if counts[3] != 0 else 0
        performance = round(counts[1] / counts[0] * 100, 2) if counts[0] != 0 else 0
        file_ble_count += counts[3]
        file_ble_only_count += counts[4]

        flie_usb_count += counts[0]
        file_usb_only_count += counts[1]

        rssi_str = str(counts[5])
        # padd rssi_str at end with 0 up to 6 characters
        rssi_str = rssi_str + "0" * (6 - len(rssi_str))
        if isPrint: 
            print(f"{address} \t\t {counts[0]} \t\t {counts[1]} \t\t\t {counts[2]}\t\t {counts[3]} \t\t {counts[4]} \t\t\t {rssi_str} \t {error} \t {performance}")

    if isPrint:
        print(f"Total BLE count: {file_ble_count}")
        print(f"Total BLE only count: {file_ble_only_count}")
        print(f"Error: {round(file_ble_only_count / file_ble_count * 100, 2)} %")
        print(f"Total USB count: {flie_usb_count}")
        print(f"Total USB only count: {file_usb_only_count}")
        print(f"Error: {round(file_usb_only_count / flie_usb_count * 100, 2)} %")
    
    return round(file_ble_only_count / file_ble_count * 100, 2), round(file_usb_only_count / flie_usb_count * 100, 2)

    
file = "data/hyp2/1000/hyp2_1000_1.json"

scenarios = [100]

errors = {}
performances = {}
for scenario in scenarios:
    prefix = f"data/hyp2/{scenario}"
    files = os.listdir(prefix)

    for file in files:
        if file.endswith('.json'):
            print("Processing file: ", prefix + '/' + file)
            error, performance = get_errors_for_file(prefix + '/' + file, True)
            parts = file.split('_')
            errors[parts[1] + '_' + parts[2]] = error
            performances[parts[1] + '_' + parts[2]] = performance
            print("Error: ", str(error))
            print("Performance: ", str(performance) + '\n')

results = {}
for file, error in errors.items():
    results[file] = (error, performances[file])

print(results)

results = {
    'min_2.json': (57.58, 64.91), 
    'min_3.json': (57.74, 65.19), 
    'min_1.json': (58.59, 68.58), 
    '30_1.json': (23.31, 60.41), 
    '30_3.json': (21.72, 55.43), 
    '30_2.json': (21.52, 53.33), 
    '100_1.json': (19.62, 58.04), 
    '100_3.json': (19.97, 59.25), 
    '100_2.json': (19.94, 57.94), 
    '1000_1.json': (19.4, 60.03), 
    '1000_2.json': (21.74, 62.14), 
    '1000_3.json': (20.48, 57.4), 
    '5000_3.json': (23.86, 68.16), 
    '5000_2.json': (21.58, 63.85), 
    '5000_1.json': (21.66, 60.39), 
    '10240_2.json': (23.98, 67.88), 
    '10240_3.json': (23.31, 66.82), 
    '10240_1.json': (24.03, 68.79),
}

averaged_results = {}
for file, error in results.items():
    parts = file.split('_')
    if parts[0] not in averaged_results:
        averaged_results[parts[0]] = (0, 0)
    averaged_results[parts[0]] = (averaged_results[parts[0]][0] + error[0], averaged_results[parts[0]][1] + error[1])

for key, value in averaged_results.items():
    averaged_results[key] = (round(value[0] / 3, 2), round(value[1] / 3, 2))

dict_to_latex(averaged_results, ["Scenario", "BLE Error %", "USB Error %"])