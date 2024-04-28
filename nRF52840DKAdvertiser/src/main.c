/* main.c - Application main entry point */

/*
 * Copyright (c) 2015-2016 Intel Corporation
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <stdio.h>
#include <string.h>
#include <zephyr/types.h>
#include <stddef.h>
#include <zephyr/sys/printk.h>
#include <zephyr/sys/util.h>
#include <zephyr/device.h>
#include <zephyr/kernel.h>
#include <zephyr/sys/ring_buffer.h>
#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/hci.h>
#include <zephyr/device.h>
#include <zephyr/drivers/gpio.h>
#include <zephyr/usb/usb_device.h>
#include <zephyr/usb/usbd.h>
#include <zephyr/drivers/uart.h>

#define RING_BUF_SIZE 1024
#define SLEEP_TIME_MS	1

/*
 * Get button configuration from the devicetree sw0 alias. This is mandatory.
 */
#define SW0_NODE	DT_ALIAS(sw0)
#define SW1_NODE	DT_ALIAS(sw1)
#if !DT_NODE_HAS_STATUS(SW0_NODE, okay)
#error "Unsupported board: sw0 devicetree alias is not defined"
#endif
static const struct gpio_dt_spec button1 = GPIO_DT_SPEC_GET_OR(SW0_NODE, gpios,
							      {0});

static const struct gpio_dt_spec button2 = GPIO_DT_SPEC_GET_OR(SW1_NODE, gpios,
							      {0});
								  
static struct gpio_callback button1_cb_data;
static struct gpio_callback button2_cb_data;

uint8_t ring_buffer[RING_BUF_SIZE];
// uint8_t lenghts_buffer[RING_BUF_SIZE];

struct ring_buf ringbuf;
// struct ring_buf lengthsbuf;
struct device *curr_dev;
bool advertising = false;
int timeout = 5000;
long startTime = 0;

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

/*
 * The led0 devicetree alias is optional. If present, we'll use it
 * to turn on the LED whenever the button is pressed.
 */
static struct gpio_dt_spec led = GPIO_DT_SPEC_GET_OR(DT_ALIAS(led0), gpios,
						     {0});


static struct bt_le_adv_param *adv_param = BT_LE_ADV_PARAM(0, 0x0020, 0x0020, NULL);

static uint8_t name_data[] = {0x6e, 0x52, 0x46, 0x20, 0x41, 0x64, 0x76,
				0x65, 0x72, 0x74, 0x69, 0x73, 0x65, 0x72};

static uint8_t mfg_data[] = {0x00, 0x00, 0x33, 0x3f};

static const struct bt_data ad[] = {
	BT_DATA(BT_DATA_MANUFACTURER_DATA, mfg_data, 4),
};

static struct bt_data sd[] = {
	BT_DATA(BT_DATA_NAME_COMPLETE, name_data, 14),
};

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
				return;
			};

			curr_dev = dev;

			// check received data 0x00 stop 0x01 start 0x02 active scanning 0x03 passive scanning
			if (buffer[0] == 0x00) {
				// stop advertising
				bt_le_adv_stop();
				// set the led to 0
				gpio_pin_set_dt(&led, 0);
				advertising = false;
				// send 0x00 through uart
				uint8_t toSend[1] = {0x00};
				ring_buf_put(&ringbuf, toSend, 1);
				uart_irq_tx_enable(curr_dev);
			} else if (buffer[0] == 0x01) {
				// start advertising
				int err = bt_le_adv_start(adv_param, ad, ARRAY_SIZE(ad), sd, ARRAY_SIZE(sd));
				startTime = k_uptime_get();
				advertising = true;
				if (err) {
					printk("Advertising failed to start (err %d)\n", err);
					return;
				}
				// set the led to 1
				gpio_pin_set_dt(&led, 1);
				// send 0x01 through uart
				uint8_t toSend[1] = {0x01};
				ring_buf_put(&ringbuf, toSend, 1);
				uart_irq_tx_enable(curr_dev);
			} else if (buffer[0] == 0x02) {
				// send param data
				uint8_t toSend[6];
				toSend[0] = 0x02;
				toSend[1] = adv_param->interval_min >> 8;
				toSend[2] = adv_param->interval_min & 0xFF;
				toSend[3] = adv_param->interval_max >> 8;
				toSend[4] = adv_param->interval_max & 0xFF;
				toSend[5] = timeout / 1000;
				ring_buf_put(&ringbuf, toSend, 6);
				uart_irq_tx_enable(curr_dev);
			} else if (buffer[0] == 0x03) {
				// set param data (advMin advMax timeout)
				adv_param->interval_min = (buffer[1] << 8) | buffer[2];
				adv_param->interval_max = (buffer[3] << 8) | buffer[4];
				timeout = buffer[5] * 1000;
			} else {
				printk("Unknown command");
			}
		}

		if (uart_irq_tx_ready(dev)) {
			int rb_len, send_len;
			uint8_t buffer[73];
			rb_len = ring_buf_get(&ringbuf, buffer, sizeof(buffer));

			if (!rb_len) {
				printk("Ring buffer empty, disable TX IRQ\n");
				uart_irq_tx_disable(dev);
				continue;
			}

			send_len = uart_fifo_fill(dev, buffer, rb_len);
			// print the bytes contents sent to the uart
			printk("Sending: ");
			for (int i = 0; i < send_len; i++) {
				printk("%x ", buffer[i]);
			}
			printk("\n");

			if (send_len < rb_len) {
				printk("Drop %d bytes\n", rb_len - send_len);
			}

			printk("ringbuf -> tty fifo %d bytes\n", send_len);
		}
	}
}

static void button1_pressed(const struct device *dev, struct gpio_callback *cb,
				unsigned int pins)
{
	gpio_pin_set_dt(&led, 1);
}

static void button2_pressed(const struct device *dev, struct gpio_callback *cb,
				unsigned int pins)
{
	gpio_pin_set_dt(&led, 0);
}


int main(void)
{
	int err;
	int ret;

	/* Initialize the Bluetooth Subsystem */
	err = bt_enable(NULL);
	if (err) {
		printk("Bluetooth init failed (err %d)\n", err);
		return 0;
	}

	printk("Bluetooth initialized\n");

	if (!gpio_is_ready_dt(&button1)) {
		printk("Error: button 1 device %s is not ready\n",
		       button1.port->name);
		return 0;
	}

	if (!gpio_is_ready_dt(&button2)) {
		printk("Error: button 2 device %s is not ready\n",
		       button2.port->name);
		return 0;
	}

	ret = gpio_pin_configure_dt(&button1, GPIO_INPUT);
	if (ret != 0) {
		printk("Error %d: failed to configure %s pin %d\n",
		       ret, button1.port->name, button1.pin);
		return 0;
	}

	ret = gpio_pin_configure_dt(&button2, GPIO_INPUT);
	if (ret != 0) {
		printk("Error %d: failed to configure %s pin %d\n",
		       ret, button2.port->name, button2.pin);
		return 0;
	}

	ret = gpio_pin_interrupt_configure_dt(&button1,
					      GPIO_INT_EDGE_TO_ACTIVE);
	if (ret != 0) {
		printk("Error %d: failed to configure interrupt on %s pin %d\n",
			ret, button1.port->name, button1.pin);
		return 0;
	}

	ret = gpio_pin_interrupt_configure_dt(&button2,
					      GPIO_INT_EDGE_TO_ACTIVE);
	if (ret != 0) {
		printk("Error %d: failed to configure interrupt on %s pin %d\n",
			ret, button2.port->name, button2.pin);
		return 0;
	}

	gpio_init_callback(&button1_cb_data, button1_pressed, BIT(button1.pin));
	gpio_init_callback(&button2_cb_data, button2_pressed, BIT(button2.pin));
	gpio_add_callback(button1.port, &button1_cb_data);
	gpio_add_callback(button2.port, &button2_cb_data);
	printk("Set up button at %s pin %d\n", button1.port->name, button1.pin);

	if (led.port && !gpio_is_ready_dt(&led)) {
		printk("Error %d: LED device %s is not ready; ignoring it\n",
		       ret, led.port->name);
		led.port = NULL;
	}
	if (led.port) {
		ret = gpio_pin_configure_dt(&led, GPIO_OUTPUT);
		if (ret != 0) {
			printk("Error %d: failed to configure LED device %s pin %d\n",
			       ret, led.port->name, led.pin);
			led.port = NULL;
		} else {
			printk("Set up LED at %s pin %d\n", led.port->name, led.pin);
		}
		gpio_pin_set_dt(&led, 0);
	}

	const struct device *dev;
	uint32_t baudrate, dtr = 0U;

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

	// /* They are optional, we use them to test the interrupt endpoint */
	// ret = uart_line_ctrl_set(dev, UART_LINE_CTRL_DCD, 1);
	// if (ret) {
	// 	printk("Failed to set DCD, ret code %d", ret);
	// }

	// ret = uart_line_ctrl_set(dev, UART_LINE_CTRL_DSR, 1);
	// if (ret) {
	// 	printk("Failed to set DSR, ret code %d", ret);
	// }

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

	/* Give CPU resources to low priority threads. */
	k_sleep(K_MSEC(100));
	// uint8_t toSend[1] = {0x01};
	// int rblen = ring_buf_put(&ringbuf, toSend, 1);
	// if(rblen) uart_irq_tx_enable(curr_dev);
	// /* Give CPU resources to low priority threads. */
	// k_sleep(K_MSEC(100));

	while(1) {
		int start = gpio_pin_get_dt(&button1);
		int stop = gpio_pin_get_dt(&button2);
		if (k_uptime_get() - startTime > timeout && advertising) {
			bt_le_adv_stop();
			// set the led to 0
			gpio_pin_set_dt(&led, 0);
			advertising = false;
			// send 0x00 through uart
			if (curr_dev) {
				uint8_t toSend[1] = {0x00};
				ring_buf_put(&ringbuf, toSend, 1);
				uart_irq_tx_enable(curr_dev);
			}
		}
		if (start > 0 && !advertising) {
			int err = bt_le_adv_start(adv_param, ad, ARRAY_SIZE(ad), sd, ARRAY_SIZE(sd));
			if (err) {
				printk("Advertising failed to start (err %d)\n", err);
				return 0;
			}
			startTime = k_uptime_get();
			advertising = true;
			if(curr_dev) {
				uint8_t toSend[1] = {0x01};
				int rblen = ring_buf_put(&ringbuf, toSend, 1);
				if(rblen) uart_irq_tx_enable(curr_dev);
			}
		} else if (stop > 0 && advertising) {
			int err = bt_le_adv_stop();
			if (err) {
				printk("Advertising failed to stop (err %d)\n", err);
				return 0;
			}
			advertising = false;
			// send 0x00 through uart
			if(curr_dev) {
				uint8_t toSend[1] = {0x00};
				ring_buf_put(&ringbuf, toSend, 1);
				uart_irq_tx_enable(curr_dev);
			} 
		}
		k_msleep(SLEEP_TIME_MS);
	}

	return 0;
}
