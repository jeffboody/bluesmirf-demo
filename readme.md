About
=====

This project shows how connect an Android phone to an Arduino with the
Bluesmirf Bluetooth module. The demo can control an led and read a
potentiometer. It should work with Android 2.0 (ecliar) and above.

The Bluesmirf Bluetooth module is available from Sparkfun.

* <a href="https://www.sparkfun.com/products/10268">Bluesmirf Gold</a>
* <a href="https://www.sparkfun.com/products/10269">Bluesmirf Silver</a>

The Mdfly Bluetooth module is also works (see the notes below).

* <a href="http://www.mdfly.com/index.php?main_page=product_info&cPath=8_47&products_id=769">Mdfly Bluetooth</a>

Send questions or comments to Jeff Boody - jeffboody@gmail.com

Installing Android SDK
======================

	# download the SDK to $ANDROID
	# i.e. ~/android
	http://developer.android.com/sdk/index.html

	# unzip the packages
	cd $ANDROID
	tar -xzf android-sdk_<verison>.tgz

	# install ant (if necessary)
	sudo apt-get install ant

	# configuring the Android SDK
	# add "SDK Platform Android 2.1, API 7"
	android &
	# "Available Packages"
	# select check box for "SDK Platform 2.1, API 7"
	# install selected

	# Eclipse is not required

Installing Arduino Dev Kit
==========================

	# install Arduino
	sudo apt-get install arduino
	
	# install ino command line toolkit
	# https://github.com/amperka/ino
	sudo apt-get install python-setuptools
	sudo apt-get install python-configobj
	sudo apt-get install python-jinja2
	sudo apt-get install python-serial
	cd $SRC
	git clone git://github.com/amperka/ino.git
	cd ino
	sudo make install

Clone Project
=============

	# download the source with git
	cd $SRC
	git clone git://github.com/jeffboody/bluesmirf-demo.git
	cd bluesmirf-demo
	git submodule update --init

	# or download the source(s) as a zip or tarball
	https://github.com/jeffboody/bluesmirf-demo/archives/master
	https://github.com/jeffboody/bluesmirf/archives/master

	# configure the profile
	cd bluesmirf-demo
	vim profile
	# edit SDK to point to $ANDROID

Building and installing the bluesmirf-demo
==========================================

	cd $SRC/bluesmirf-demo
	source profile

	# start the adb server as root
	sudo adb devices

	./build-java.sh
	./install.sh

Building and uploading firmware with Arduino GUI
===========================================

	# launch the Arduino IDE
	cd $ARDUINO/arduino-0021
	./arduino &

	# verify board settings under Tools->Board
	Arduino Duemilanove or Nano w/ATmega328

	# verify serial port under Tools->Serial Port
	# when Arduino is connected
	/dev/ttyUSB0

	# open firmware/src/sketch.ino
	# compile using Sketch->Verify/Compile

	# use the upload icon to program the Arduino firmware
	# if bluesmirf is connected then unplug one of the tx/rx wires first

Building and uploading firmware with command prompt
==========================================

	# building
	cd firmware
	./build.sh
	
	# upload over USB
	./upload.sh
	
	# upload over Bluetooth with SPPMirror
	# see https://github.com/jeffboody/spp-mirror
	# 1) start the spp mirror app
	# 2) connect to the Bluetooth spp device and ensure net is listening
	# 3) press Arduino reset button
	# 4) wait ~250ms then start upload
	./upload-spp-mirror.sh

Building the circuit
====================

View circuit.fzz schematic with <a href="http://www.fritzing.org">Fritzing</a> software.

Mdfly Bluetooth Notes
=====================

The default baud rate is 9600 and must be changed to 115200.

1. connect Mdfly and Arduino for programming (i.e. rx -> rx and tx -> tx)
2. connect Arduino reset to ground to enable FTDI mode
3. start Arduino serial monitor and set baud rate to 9600
4. program the Mdfly module
5. reconnect rx/tx for serial (i.e. rx -> tx and tx -> rx)
6. disconnect Arduino reset from ground

Commands
--------

	AT               # OK
	AT+NAMEmdfly01   # OKname
	AT+PIN1234       # OKpin
	AT+BAUD8         # OKbaud

References
----------

* http://mdfly.com/Download/Wireless/BT0417C_ATcommand.pdf
* http://arduino.cc/forum/index.php?topic=59338.0

License
=======

	Copyright (c) 2011-2012 Jeff Boody

	Permission is hereby granted, free of charge, to any person obtaining a
	copy of this software and associated documentation files (the "Software"),
	to deal in the Software without restriction, including without limitation
	the rights to use, copy, modify, merge, publish, distribute, sublicense,
	and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included
	in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
