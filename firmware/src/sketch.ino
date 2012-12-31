/*
 * Copyright (c) 2011 Jeff Boody
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

// See the arduio website for more details on processing language
// http://arduino.cc/

// led and potentiometer
int led;
int pot;

// arduino pins
int PIN_LED = 13;
int PIN_POT = A0;

void setup()
{
  pinMode(PIN_LED, OUTPUT);
  Serial.begin(115200);   // 115200 is the default for Android
}

void loop()
{
  // read potentiometer (0-1023, 2 bytes)
  pot = analogRead(PIN_POT);

  if(Serial.available() == 1)
  {
    // Serial.read() reads one byte
    led = Serial.read();

    // Serial.write() writes one byte
    Serial.write(((short) pot) & 0xFF);
    Serial.write(((short) pot) >> 8);
  }

  // update led
  if(led)
    digitalWrite(PIN_LED, HIGH);
  else
    digitalWrite(PIN_LED, LOW);

  // add a 50 ms delay before next loop iteration
  delay(50);
}
