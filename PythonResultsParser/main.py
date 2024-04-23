from plotters import *
import matplotlib.pyplot as plt

# open the folder data and get all json filenames from it in a list
import os
data_files = os.listdir('data')

# iterate over the list of filenames
for file in data_files:
    # check if the file is a json file
    if file.endswith('.json'):
        # plot the number of callbacks for each device
        usb, ble = split_data(read_data('data/' + file))
        print_address_count_comparison(usb, ble, file.split('.')[0])
        plot_callback_count(usb, ble, file.split('.')[0])

# show the plots
plt.show()