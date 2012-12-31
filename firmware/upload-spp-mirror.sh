avrdude -v -patmega328p -carduino -Pnet:127.0.0.1:6800 -D -Uflash:w:.build/uno/firmware.hex:i
