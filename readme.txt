[About]

	This project shows how connect an Android phone to an Arduino with the
	Bluesmirf Bluetooth module. The demo can control an led and read a
	potentiometer. It should work with Android 2.0 (ecliar) and above.

	The Bluesmirf Bluetooth module is available from Sparkfun.

		http://www.sparkfun.com/products/582
		http://www.sparkfun.com/products/10269

	Send questions or comments to Jeff Boody - jeffboody@gmail.com

[Installing Android SDK]

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

[Installing Arduino Dev Kit]

	# some Linux distributions install brltty which cause
	# problems with Arduino
	# http://www.ladyada.net/learn/arduino/lesson0-lin.html
	sudo apt-get remove brltty

	# http://www.arduino.cc/en/Main/Software
	# http://www.arduino.cc/playground/Learning/Linux
	# java should be sun-java6-jre
	java -version
	sudo apt-get install gcc-avr
	sudo apt-get install avr-libc
	cd $ARDUINO
	tar -xzf arduino-0021-2.tgz

[Clone Project]

	# download the source with git
	cd $SRC
	git clone git://github.com/jeffboody/bluesmirf-demo.git

	# or download the source as a zip or tarball
	https://github.com/jeffboody/bluesmirf-demo/archives/master

	# configure the profile
	cd bluesmirf-demo
	vim profile
	# edit SDK to point to $ANDROID

[Building and installing the bluesmirf-demo]

	cd $SRC/bluesmirf-demo
	source profile

	# start the adb server as root
	sudo adb devices

	./build-java.sh
	./install.sh

[Building and uploading the Arduino firmware]

	# launch the Arduino IDE
	cd $ARDUINO/arduino-0021
	./arduino &

	# verify board settings under Tools->Board
	Arduino Duemilanove or Nano w/ATmega328

	# verify serial port under Tools->Serial Port
	# when Arduino is connected
	/dev/ttyUSB0

	# open $SRC/bluesmirf-demo/sketchbook/firmware/firmware.pde
	# compile using Sketch->Verify/Compile

	# use the upload icon to program the Arduino firmware
	# if bluesmirf is connected then unplug one of the tx/rx wires first

[Building the circuit]

	# see circuit/circuit.pdf

[Configuration Files]

	# edit the bluesmirf.cfg file to contain your Bluetooth mac address
	# (Bluetooth mac address is printed on the Bluesmirf chip)
	vim bluesmirf.cfg
	adb push bluesmirf.cfg /sdcard/bluesmirf.cfg
