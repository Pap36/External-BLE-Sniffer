import binascii

def bytesToHex(bytes):
    return binascii.hexlify(bytes).decode('utf-8')

def hexToBytes(hex):
    return binascii.unhexlify(hex)

def intToBytes(intArray):
    return bytes([int(a) + 256 if a < 0 else int(a) for a in intArray])

def print_callback_rate_dict_to_latex(dict, columnNames):
    print("\\begin{table}[H]")
    print("\\centering")
    print("\\begin{tabular}" + "{|" + "|".join(["c" for _ in range(len(columnNames))]) + "|}")
    print("\\hline")
    print(" & ".join(columnNames) + "\\\\")
    print("\\hline")
    sorted_dict = sorted(dict.items(), key=lambda x: x[1][6], reverse=True)
    for key, value in sorted_dict:
        print(f"{key} & {' & '.join([str(v) for v in value])} \\\\")
        print("\\hline")
    print("\\end{tabular}")
    print("\\end{table}")