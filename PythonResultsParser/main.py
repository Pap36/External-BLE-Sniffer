from plotters import *
import matplotlib.pyplot as plt

# open the folder data and get all json filenames from it in a list
import os

from utils import print_callback_rate_dict_to_latex

scenarioIndexes = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
scenarioIndexes = [9]
times = [30, 60, 300, 600]

scenarioIndexes = [9]
times = [600]


prefixes = []
for i in scenarioIndexes:
    for t in times:
        prefixes.append(f'data/scenario{i}/{t}/')

all_usb = []
all_ble = []

callback_rates = {}


# fig, axs = plt.subplots(2, 2)

# axs[0,0].set(ylabel='Callback Rate')
# axs[1,0].set(ylabel='Callback Rate', xlabel='Timestamp (s)')
# axs[1,1].set(xlabel='Timestamp (s)')
# # ax.label_outer()

# fig.suptitle('nRF 52840 DK vs Phone Callback Rate - Scenario 4')

for index, prefix in enumerate(prefixes):

    data_files = os.listdir(prefix)

    usbs = []
    bles = []

    # # iterate over the list of filenames
    for file in data_files:
        # check if the file is a json file
        if file.endswith('.json'):
            print("Processing file: ", file)
            # plot the number of callbacks for each device
            usb, ble = split_data(read_data(prefix + '/' + file))
            # plot_callback_per_second_rate(usb, ble, file.split('.')[0])
            # usbs.append(usb)
            # # all_usb.append(usb)
            # bles.append(ble)
            # all_ble.append(ble)
            
            # print_address_count_comparison(usb, ble, file.split('.')[0])
            plot_callback_count(usb, ble, file.split('.')[0])
        
    # col, row = index % 2, index // 2
    # plot_callback_rate_scenario(data_files, prefix, True, axs[col, row])


    # print("Processing folder: ", prefix)
    # data_files = [prefix + file for file in data_files]
    # try: 
    #     usb, ble = calculate_callback_rate_scenario(data_files)
    # except Exception as e:
    #     print(e)
    #     usb, ble = (0, 0)
    
    # usb_callback_rate = round(usb, 2)
    # ble_callback_rate = round(ble, 2)
    
    # scenarioIndex = int(prefix.split('/')[1].split('scenario')[1])
    # time = int(prefix.split('/')[2])

    # if scenarioIndex not in callback_rates:
    #     callback_rates[scenarioIndex] = {}

    # callback_rates[scenarioIndex][time] = (usb_callback_rate, ble_callback_rate)


    # plot_callback_rate_scenario(data_files, prefix)

    # plt.figure()
    # plot_average(usbs, "USB")
    # plot_average(bles, "BLE")

# plt.figure()
# plot_average(all_usb, "USB")
# plot_average(all_ble, "BLE")

# show the plots
plt.show()

# print(callback_rates)

print("Scenario\tTime\tUSB\tAverage\tUSB Error %\tBLE\tAverage\tPredicted\tBLE Error %")


callback_rates = {
    1: {30: (66.78, 36.11), 60: (65.08, 34.64), 300: (59.59, 29.57), 600: (59.82, 31.64)},
    2: {30: (61.15, 37.34), 60: (62.66, 36.55), 300: (64.25, 35.12), 600: (65.64, 34.27)}, 
    3: {30: (64.84, 40.93), 60: (67.49, 36.24), 300: (69.74, 35.56), 600: (67.4, 35.03)}, 
    4: {30: (64.43, 37.96), 60: (64.65, 33.95), 300: (66.67, 34.87), 600: (68.77, 33.1)}, 
    5: {30: (61.83, 8.01), 60: (61.06, 8.98), 300: (60.49, 9.94), 600: (68.6, 8.92)}, 
    6: {30: (83.63, 11.77), 60: (83.68, 11.95), 300: (88.79, 11.86), 600: (85.54, 11.96)}, 
    7: {30: (81.1, 10.85), 60: (81.96, 11.54), 300: (81.4, 10.55), 600: (79.66, 10.61)}, 
    8: {30: (70.29, 9.44), 60: (71.72, 9.19), 300: (66.72, 9.03), 600: (62.0, 7.93)}, 
    9: {30: (55.85, 31.2), 60: (52.7, 32.44), 300: (50.49, 32.03), 600: (52.0, 30.74)}, 
    10: {30: (51.39, 32.03), 60: (52.15, 32.46), 300: (54.55, 30.88), 600: (59.29, 30.37)}, 
    11: {30: (60.35, 7.94), 60: (57.85, 7.41), 300: (60.67, 7.24), 600: (57.51, 7.0)}, 
    12: {30: (60.27, 7.19), 60: (59.17, 7.0), 300: (62.44, 7.09), 600: (59.69, 7.42)}
}

scenariosForAverage1 = [1, 2, 3, 4, 9, 10]
scenariosForAverage2 = [5, 6, 7, 8, 11, 12]

# scenariosForAverage1 = scenarioIndexes
# scenariosForAverage2 = [1]

processedDataBLEOff = {}
processedDataBLEOOn = {}

averages1 = {}
for time in times:
    usb_sum = 0
    ble_sum = 0
    for scenarioIndex in scenariosForAverage1:
        usb, ble = callback_rates[scenarioIndex][time]
        usb_sum += usb
        ble_sum += ble
    averages1[time] = (round(usb_sum / len(scenariosForAverage1), 2), round(ble_sum / len(scenariosForAverage1), 2))

averages2 = {}
for time in times:
    usb_sum = 0
    ble_sum = 0
    for scenarioIndex in scenariosForAverage2:
        usb, ble = callback_rates[scenarioIndex][time]
        usb_sum += usb
        ble_sum += ble
    averages2[time] = (round(usb_sum / len(scenariosForAverage2), 2), round(ble_sum / len(scenariosForAverage2), 2))

for time in times:
    for scenarioIndex in scenariosForAverage1:
        usb, ble = callback_rates[scenarioIndex][time]
        usbError = round((usb - averages1[time][0]) / averages1[time][0] * 100, 2)
        blePredicted = round(averages1[time][1] + averages1[time][1] * usbError / 100, 2)
        bleError = round((ble - blePredicted) / averages1[time][1] * 100, 2)
        print(f"{scenarioIndex}\t\t{time}\t{usb}\t{averages1[time][0]}\t{usbError}%\t\t{ble}\t{averages1[time][1]}\t{blePredicted}\t\t{bleError}%")
        processedDataBLEOff[(scenarioIndex, time)] = [usb, averages1[time][0], usbError, ble, averages1[time][1], blePredicted, bleError]

print("\n\n")

for time in times:
    for scenarioIndex in scenariosForAverage2:
        usb, ble = callback_rates[scenarioIndex][time]
        usbError = round((usb - averages2[time][0]) / averages2[time][0] * 100, 2)
        blePredicted = round(averages2[time][1] + averages2[time][1] * usbError / 100, 2)
        bleError = round((ble - blePredicted) / averages2[time][1] * 100, 2)
        print(f"{scenarioIndex}\t\t{time}\t{usb}\t{averages2[time][0]}\t{usbError}%\t\t{ble}\t{averages2[time][1]}\t{blePredicted}\t\t{bleError}%")
        processedDataBLEOOn[(scenarioIndex, time)] = [usb, averages2[time][0], usbError, ble, averages2[time][1], blePredicted, bleError]

# Keep only times 300 and 600
processedDataBLEOff = {k: v for k, v in processedDataBLEOff.items() if k[1] in [30, 60, 300, 600]}

ordered = sorted(processedDataBLEOff.items(), key=lambda x: x[1][5])
ordered.reverse()

print("\n\n")

for o in ordered:
    print(o)

print_callback_rate_dict_to_latex(
    processedDataBLEOff, ['Scenario, Time', 'Board', 'Board Avg', 'Board Error \%', 'Phone', 'Phone Avg', 'Phone Predicted', 'Phone Error \%']
)


ordered = sorted(processedDataBLEOOn.items(), key=lambda x: x[1][5])
ordered.reverse()

print_callback_rate_dict_to_latex(
    processedDataBLEOOn, ['Scenario, Time', 'Board', 'Board Avg', 'Board Error \%', 'Phone', 'Phone Avg', 'Phone Predicted', 'Phone Error \%']
)