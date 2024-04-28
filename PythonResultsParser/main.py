from plotters import *
import matplotlib.pyplot as plt

# open the folder data and get all json filenames from it in a list
import os

prefixes = [
    'data/scenario1/600',
    'data/scenario2/600',
    'data/scenario3/600',
]

prefix = 'data/scenario1/30'

all_usb = []
all_ble = []

for prefix in prefixes:

    data_files = os.listdir(prefix)

    usbs = []
    bles = []

    # iterate over the list of filenames
    for file in data_files:
        # check if the file is a json file
        if file.endswith('.json'):
            print("Processing file: ", file)
            # plot the number of callbacks for each device
            # usb, ble = split_data(read_data(prefix + '/' + file))
            # plot_callback_per_second_rate(usb, ble, file.split('.')[0])
            # usbs.append(usb)
            # all_usb.append(usb)
            # bles.append(ble)
            # all_ble.append(ble)
            
            # print_address_count_comparison(usb, ble, file.split('.')[0])
            # plot_callback_count(usb, ble, file.split('.')[0])

    plot_callback_rate_scenario(data_files, prefix)

    # plt.figure()
    # plot_average(usbs, "USB")
    # plot_average(bles, "BLE")

# plt.figure()
# plot_average(all_usb, "USB")
# plot_average(all_ble, "BLE")

# show the plots
plt.show()