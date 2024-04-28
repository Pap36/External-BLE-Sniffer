/* main.c - Application main entry point */

/*
 * Copyright (c) 2015-2016 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 */
#include <stdio.h>
#include <string.h>
#include <zephyr/device.h>
#include <zephyr/drivers/uart.h>
#include <zephyr/kernel.h>
#include <zephyr/sys/ring_buffer.h>

#include <zephyr/usb/usb_device.h>
#include <zephyr/usb/usbd.h>

#include <zephyr/types.h>
#include <stddef.h>
#include <zephyr/sys/printk.h>
#include <zephyr/sys/util.h>

#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/hci.h>

#include <zephyr/usb/usb_device.h>

#define RING_BUF_SIZE 1024
uint8_t ring_buffer[RING_BUF_SIZE];
// uint8_t lenghts_buffer[RING_BUF_SIZE];

struct ring_buf ringbuf;
// struct ring_buf lengthsbuf;
struct device *curr_dev;

struct bt_le_scan_param scan_param = {
	.type = BT_HCI_LE_SCAN_ACTIVE,
	.options = BT_LE_SCAN_OPT_NONE,
	.interval = 0x0004,
	.window = 0x0004,
};

#if defined(CONFIG_USB_DEVICE_STACK_NEXT)
USBD_CONFIGURATION_DEFINE(config_1, USB_SCD_SELF_POWERED, 200);

USBD_DESC_LANG_DEFINE(sample_lang);
USBD_DESC_STRING_DEFINE(sample_mfr, "ZEPHYR", 1);
USBD_DESC_STRING_DEFINE(sample_product, "Zephyr USBD CDC ACM", 2);
USBD_DESC_STRING_DEFINE(sample_sn, "0123456789ABCDEF", 3);

USBD_DEVICE_DEFINE(sample_usbd, DEVICE_DT_GET(DT_NODELABEL(zephyr_udc0)), 0x2fe3, 0x0001);

static int enable_usb_device_next(void)
{
	int err;

	err = usbd_add_descriptor(&sample_usbd, &sample_lang);
	if (err) {
		printk("Failed to initialize language descriptor (%d)", err);
		return err;
	}

	err = usbd_add_descriptor(&sample_usbd, &sample_mfr);
	if (err) {
		printk("Failed to initialize manufacturer descriptor (%d)", err);
		return err;
	}

	err = usbd_add_descriptor(&sample_usbd, &sample_product);
	if (err) {
		printk("Failed to initialize product descriptor (%d)", err);
		return err;
	}

	err = usbd_add_descriptor(&sample_usbd, &sample_sn);
	if (err) {
		printk("Failed to initialize SN descriptor (%d)", err);
		return err;
	}

	err = usbd_add_configuration(&sample_usbd, &config_1);
	if (err) {
		printk("Failed to add configuration (%d)", err);
		return err;
	}

	err = usbd_register_class(&sample_usbd, "cdc_acm_0", 1);
	if (err) {
		printk("Failed to register CDC ACM class (%d)", err);
		return err;
	}

	err = usbd_init(&sample_usbd);
	if (err) {
		printk("Failed to initialize device support");
		return err;
	}

	err = usbd_enable(&sample_usbd);
	if (err) {
		printk("Failed to enable device support");
		return err;
	}

	printk("USB device support enabled");

	return 0;
}
#endif /* IS_ENABLED(CONFIG_USB_DEVICE_STACK_NEXT) */

int callbacksRegistered = 0;

static void scan_cb(const bt_addr_le_t *addr, int8_t rssi, uint8_t adv_type,
		    struct net_buf_simple *buf)
{
	int rb_len;
	// send the manufacturer data over USB
	int len_to_send = buf->len + 10;
	// create an array to send which contains the buf data, rssi, adv type and addr
	uint8_t send_array[len_to_send];
	send_array[0] = len_to_send - 1;
	// copy the rssi
	send_array[1] = rssi;
	// copy the adv type
	send_array[2] = adv_type;
	// copy the addr type
	send_array[3] = addr->type;
	// copy the addr
	for (int i = 0; i < 6; i++) {
		send_array[4 + i] = addr->a.val[i];
	}
	// copy the manufacturer data
	for (int i = 0; i < buf->len; i++) {
		send_array[10 + i] = buf->data[i];
	}
	callbacksRegistered++;

	rb_len = ring_buf_put(&ringbuf, send_array, len_to_send);

	// if (rb_len < len_to_send) {
	// 	printk("Drop %u bytes", len_to_send - rb_len);
	// }

	// printk("tty fifo -> ringbuf %d bytes", rb_len);
	if (rb_len) {
		uart_irq_tx_enable(curr_dev);
	}
}

static void interrupt_handler(const struct device *dev, void *user_data)
{
	ARG_UNUSED(user_data);

	while (uart_irq_update(dev) && uart_irq_is_pending(dev)) {
		if (uart_irq_rx_ready(dev)) {
			int recv_len, rb_len;
			uint8_t buffer[64];
			size_t len = MIN(ring_buf_space_get(&ringbuf), sizeof(buffer));

			recv_len = uart_fifo_read(dev, buffer, len);
			if (recv_len < 0) {
				printk("Failed to read UART FIFO");
				recv_len = 0;
			};

			// check received data 0x00 stop 0x01 start 0x02 active scanning 0x03 passive scanning
			if (buffer[0] == 0x00) {
				// stop scanning
				bt_le_scan_stop();
			} else if (buffer[0] == 0x01) {
				// start scanning
				curr_dev = dev;
				bt_le_scan_start(&scan_param, scan_cb);
			} else if (buffer[0] == 0x02) {
				// send params
				uint8_t send_array[6];
				send_array[0] = 0x02;
				send_array[1] = scan_param.window >> 8;
				send_array[2] = scan_param.window & 0xFF;
				send_array[3] = scan_param.interval >> 8;
				send_array[4] = scan_param.interval & 0xFF;
				send_array[5] = scan_param.type;
				rb_len = ring_buf_put(&ringbuf, send_array, sizeof(send_array));
				if (rb_len) {
					uart_irq_tx_enable(dev);
				}
			} else if (buffer[0] == 0x03) {
				// set params
				scan_param.window = buffer[1] << 8 | buffer[2];
				scan_param.interval = buffer[3] << 8 | buffer[4];
				scan_param.type = buffer[5];
			} else {
				printk("Unknown command");
			}
		}

		if (uart_irq_tx_ready(dev)) {
			// uint8_t lengthToRead[1];
			int rb_len, send_len;
			// lb_len = ring_buf_get(&lengthsbuf, lengthToRead, 1);
			uint8_t buffer[73];
			rb_len = ring_buf_get(&ringbuf, buffer, sizeof(buffer));

			if (!rb_len) {
				printk("Ring buffer empty, disable TX IRQ");
				uart_irq_tx_disable(dev);
				continue;
			}

			send_len = uart_fifo_fill(dev, buffer, rb_len);
			if (send_len < rb_len) {
				printk("Drop %d bytes", rb_len - send_len);
			}

			printk("ringbuf -> tty fifo %d bytes", send_len);
		}
	}
}

int main(void)
{
	int err, err2;

	/* Initialize the Bluetooth Subsystem */
	err = bt_enable(NULL);
	if (err) {
		printk("Bluetooth init failed (err %d)\n", err);
		return 0;
	}

	printk("Bluetooth initialized\n");

	printk("Start USB CDC ACM device sample");
	const struct device *dev;
	uint32_t baudrate, dtr = 0U;
	int ret;

	dev = DEVICE_DT_GET_ONE(zephyr_cdc_acm_uart);
	printk("CDC ACM device is %p, name is %s", dev, dev->name);
	if (!device_is_ready(dev)) {
		printk("CDC ACM device not ready");
		return 0;
	}

#if defined(CONFIG_USB_DEVICE_STACK_NEXT)
	ret = enable_usb_device_next();
#else
	ret = usb_enable(NULL);
#endif

	if (ret != 0) {
		printk("Failed to enable USB");
		return 0;
	}

	ring_buf_init(&ringbuf, sizeof(ring_buffer), ring_buffer);
	// ring_buf_init(&lengthsbuf, sizeof(lenghts_buffer), lenghts_buffer);

	printk("Wait for DTR");

	while (true) {
		uart_line_ctrl_get(dev, UART_LINE_CTRL_DTR, &dtr);
		if (dtr) {
			break;
		} else {
			/* Give CPU resources to low priority threads. */
			k_sleep(K_MSEC(100));
		}
	}

	printk("DTR set");

	/* They are optional, we use them to test the interrupt endpoint */
	ret = uart_line_ctrl_set(dev, UART_LINE_CTRL_DCD, 1);
	if (ret) {
		printk("Failed to set DCD, ret code %d", ret);
	}

	ret = uart_line_ctrl_set(dev, UART_LINE_CTRL_DSR, 1);
	if (ret) {
		printk("Failed to set DSR, ret code %d", ret);
	}

	/* Wait 100ms for the host to do all settings */
	k_msleep(100);

	ret = uart_line_ctrl_get(dev, UART_LINE_CTRL_BAUD_RATE, &baudrate);
	if (ret) {
		printk("Failed to get baudrate, ret code %d", ret);
	} else {
		printk("Baudrate detected: %d", baudrate);
	}

	uart_irq_callback_set(dev, interrupt_handler);

	/* Enable rx interrupts */
	uart_irq_rx_enable(dev);
	return 0;
}
