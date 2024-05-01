import os

from plotters import *
from utils import *

scenarios = [20, 100, 500, 1000, 5000, 10240]

scenario = 20

prefix = 'data/hyp3/' + str(scenario) + '/'

def handleFile(prefix):

    files = os.listdir(prefix)
    results = {}
    bursts = {}
    for file in files:
        if file.endswith('.json'):
            print("Processing file: ", prefix + file)
            
            usb, ble = split_data(read_data(prefix + file))
            usb, ble = filterOutPhone(usb, ble)
            # plot_callback_count(usb, ble, file.split('.')[0])
            scenario, timeout = file.split('_')[1], file.split('_')[2].split('.')[0]
            bursts[file] = len(set(extract_addresses(ble, True)))
            expectedCount = 1000 / int(scenario) * int(timeout)
            # print("Expected: ", expectedCount)
            res = print_address_count_comparison(usb, ble, file.split('.')[0], isPrint=False)
            results[file] = res

    erros = []
    output = {}
    for file, res in results.items():
        print(file)
        scenario, timeout = file.split('_')[1], file.split('_')[2].split('.')[0]
        all_usb_count = 0
        all_ble_count = 0
        for r in res[1:]:
            # calculated_adv_interval = round(int(timeout) * 1000 / int(r[1]), 2)
            # relative_error = round((calculated_adv_interval - int(scenario)) / int(scenario) * 100, 2)
            # print(f"{calculated_adv_interval} ms \t {relative_error} % \t {r[0]}")
            all_usb_count += r[1]
            all_ble_count += r[3]
        
        # average_adv_interval = round(int(timeout) * 1000 * len(res[1:]) / all_usb_count, 2)
        # print(f"Average: {average_adv_interval} ms")
        # average_relative_error = round((average_adv_interval - int(scenario)) / int(scenario) * 100, 2)
        # print(f"Average relative error: {average_relative_error} %")
        # erros.append(average_relative_error)
        output[file] = (all_usb_count, all_ble_count, bursts[file])

    return output

allOutputs = {
    20: {'con_20_60_p.json': (9542, 9114, 4), 'con_20_20.json': (3168, 2526, 4), 'con_20_5_p.json': (805, 772, 4), 'con_20_40.json': (6344, 5347, 4), 'con_20_40_p.json': (7919, 7333, 5), 'con_20_5.json': (787, 507, 4), 'con_20_60.json': (9506, 7723, 4), 'con_20_20_p.json': (3164, 3031, 4)}, 
    100: {'con_100_20.json': (757, 453, 4), 'con_100_60.json': (2264, 1477, 4), 'con_100_40.json': (1509, 891, 4), 'con_100_5.json': (192, 153, 4), 'con_100_20_p.json': (756, 696, 4), 'con_100_60_p.json': (2280, 2119, 4), 'con_100_5_p.json': (195, 184, 4), 'con_100_40_p.json': (1520, 1392, 4)}, 
    500: {'con_500_60_p.json': (472, 442, 4), 'con_500_5_p.json': (41, 38, 4), 'con_500_40_p.json': (318, 291, 4), 'con_500_60.json': (469, 332, 4), 'con_500_40.json': (317, 237, 4), 'con_500_5.json': (40, 30, 4), 'con_500_20.json': (161, 125, 4), 'con_500_20_p.json': (159, 150, 4)}, 
    1000: {'con_1000_40.json': (161, 148, 4), 'con_1000_60.json': (235, 154, 4), 'con_1000_20.json': (79, 61, 4), 'con_1000_20_p.json': (79, 80, 4), 'con_1000_60_p.json': (237, 221, 4), 'con_1000_5.json': (20, 16, 4), 'con_1000_5_p.json': (20, 19, 4), 'con_1000_40_p.json': (160, 145, 4)}, 
    5000: {'con_5000_5.json': (3, 3, 3), 'con_5000_40_p.json': (32, 28, 4), 'con_5000_5_p.json': (3, 3, 3), 'con_5000_60_p.json': (47, 46, 4), 'con_5000_20_p.json': (15, 15, 4), 'con_5000_60.json': (47, 43, 4), 'con_5000_40.json': (31, 23, 4), 'con_5000_20.json': (16, 9, 4)}, 
    10240: {'con_10240_20_p.json': (7, 8, 4), 'con_10240_20.json': (8, 7, 4), 'con_10240_60.json': (24, 21, 4), 'con_10240_5_p.json': (4, 4, 4), 'con_10240_40.json': (16, 14, 4), 'con_10240_40_p.json': (16, 15, 4), 'con_10240_60_p.json': (25, 23, 4), 'con_10240_5.json': (2, 3, 3)}
}

# for scenario in scenarios:
#     prefix = 'data/hyp3/' + str(scenario) + '/'
#     handleFile(prefix)
#     allOutputs[scenario] = handleFile(prefix)


refactored = {
    (25, 60, "Passive"): (2385.5, 99.4, 2278.5, 94.94),
    (25, 20, "Active"): (792.0, 99.0, 631.5, 78.94),
    (25, 5, "Passive"): (200.0, 100.0, 193.0, 96.5),
    (25, 40, "Active"): (1586.0, 99.12, 1336.75, 83.55),
    (25, 40, "Passive"): (1583.8, 98.99, 1466.6, 91.66),
    (25, 5, "Active"): (196.75, 98.38, 126.75, 63.38),
    (25, 60, "Active"): (2376.5, 99.02, 1930.75, 80.45),
    (25, 20, "Passive"): (791.0, 98.88, 757.75, 94.72),
    (100, 20, "Active"): (189.25, 94.62, 113.25, 56.62),
    (100, 60, "Active"): (566.0, 94.33, 369.25, 61.54),
    (100, 40, "Active"): (377.25, 94.31, 222.75, 55.69),
    (100, 5, "Active"): (48.0, 96.0, 38.25, 76.5),
    (100, 20, "Passive"): (189.0, 94.5, 174.0, 87.0),
    (100, 60, "Passive"): (570.0, 95.0, 529.75, 88.29),
    (100, 5, "Passive"): (48.75, 97.5, 46.0, 92.0),
    (100, 40, "Passive"): (380.0, 95.0, 348.0, 87.0),
    (500, 60, "Passive"): (118.0, 98.33, 110.5, 92.08),
    (500, 5, "Passive"): (10.0, 100.0, 9.5, 95.0),
    (500, 40, "Passive"): (79.5, 99.38, 72.75, 90.94),
    (500, 60, "Active"): (117.25, 97.71, 83.0, 69.17),
    (500, 40, "Active"): (79.25, 99.06, 59.25, 74.06),
    (500, 5, "Active"): (10.0, 100.0, 7.5, 75.0),
    (500, 20, "Active"): (40.0, 100.0, 31.25, 78.12),
    (500, 20, "Passive"): (39.75, 99.38, 37.5, 93.75),
    (1000, 40, "Active"): (40.0, 100.0, 37.0, 92.5),
    (1000, 60, "Active"): (58.75, 97.92, 38.5, 64.17),
    (1000, 20, "Active"): (19.75, 98.75, 15.25, 76.25),
    (1000, 20, "Passive"): (19.75, 98.75, 20.0, 100.0),
    (1000, 60, "Passive"): (59.25, 98.75, 55.25, 92.08),
    (1000, 5, "Active"): (5.0, 100.0, 4.0, 80.0),
    (1000, 5, "Passive"): (5.0, 100.0, 4.75, 95.0),
    (1000, 40, "Passive"): (40.0, 100.0, 36.25, 90.62),
    (5000, 5, "Active"): (1.0, 100.0, 1.0, 100.0),
    (5000, 40, "Passive"): (8.0, 100.0, 7.0, 87.5),
    (5000, 5, "Passive"): (1.0, 100.0, 1.0, 100.0),
    (5000, 60, "Passive"): (11.75, 97.92, 11.5, 95.83),
    (5000, 20, "Passive"): (3.75, 93.75, 3.75, 93.75),
    (5000, 60, "Active"): (11.75, 97.92, 10.75, 89.58),
    (5000, 40, "Active"): (7.75, 96.88, 5.75, 71.88),
    (5000, 20, "Active"): (4.0, 100.0, 2.25, 56.25),
    (10240, 20, "Passive"): (1.75, 89.6, 1.95, 100.0),
    (10240, 20, "Active"): (1.95, 100.0, 1.75, 89.6),
    (10240, 60, "Active"): (5.86, 100.0, 5.25, 89.6),
    (10240, 5, "Passive"): (0.49, 100.0, 0.49, 100.0),
    (10240, 40, "Active"): (3.91, 100.0, 3.5, 89.6),
    (10240, 40, "Passive"): (3.91, 100.0, 3.75, 96.0),
    (10240, 60, "Passive"): (5.86, 100.0, 5.75, 98.13),
    (10240, 5, "Active"): (0.49, 100.0, 0.49, 100.0),
}

# for output in allOutputs:
#     for key, value in allOutputs[output].items():
#         adv, timeout = key.split('_')[1], key.split('_')[2].split('.')[0]
#         if adv == '20':
#             adv = '25'
#         scanMode = 'Passive' if key.endswith('_p.json') else 'Active'
#         expected = 1000 / int(adv) * int(timeout)
#         value = (min(expected * value[2], value[0]), min(expected * value[2], value[1]), value[2])
#         usb_accuracy = round(value[0] / value[2] / expected * 100, 2)
#         ble_accuracy = round(value[1] / value[2] / expected * 100, 2)
#         refactored[(adv, timeout, scanMode)] = (round(value[0] / value[2], 2), usb_accuracy, round(value[1] / value[2], 2), ble_accuracy)

# for key, value in refactored.items():
#     print(f"{key[0]}, \t{key[1]}, \t{key[2]}, \t{value[0]}, \t{value[1]}, \t{value[2]}, \t{value[3]}")

# ordered refactored by key
refactored = dict(sorted(refactored.items(), key=lambda item: item[0]))

dict_to_latex(refactored, ["Adv Interval, Timeout, Scan Mode", "USB Count", "USB Accuracy", "BLE Count", "BLE Accuracy"])
# plt.show()    