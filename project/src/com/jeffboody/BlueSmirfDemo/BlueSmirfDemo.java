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

package com.jeffboody.BlueSmirfDemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BlueSmirfDemo extends Activity implements Runnable, Handler.Callback
{
	private static final String TAG = "BlueSmirfDemo";

	// bluetooth code is based on this example
	// http://groups.google.com/group/android-beginners/browse_thread/thread/322c99d3b907a9e9/e1e920fe50135738?pli=1

	// well known SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// menu constant(s)
	private static final int MENU_TOGGLE_LED = 0;

	// Bluetooth state
	private boolean          mIsConnected      = false;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket  mBluetoothSocket  = null;
	private String           mBluetoothAddress = null;
	private OutputStream     mOutputStream     = null;
	private InputStream      mInputStream      = null;

	// app state
	private boolean  mIsAppRunning = false;
	private TextView mTextView;
	private Handler  mHandler;

	// Arduino state
	private int mStateLED = 0;
	private int mStatePOT = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mTextView = new TextView(this);
		mHandler  = new Handler(this);
		setContentView(mTextView);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// read the Bluetooth mac address
		try
		{
			BufferedReader buf = new BufferedReader(new FileReader("/sdcard/bluesmirf.cfg"));
			mBluetoothAddress = buf.readLine();
			mBluetoothAddress.toUpperCase();
			buf.close();
		}
		catch(Exception e)
		{
			Log.e(TAG, "failed to read /sdcard/bluesmirf.cfg", e);
			mBluetoothAddress = null;
		}

		UpdateUI();
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	protected void onPause()
	{
		mIsAppRunning = false;
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	/*
	 * menu to toggle LED
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, MENU_TOGGLE_LED, 0, "Toggle LED");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == MENU_TOGGLE_LED)
		{
			mStateLED = 1 - mStateLED;
			return true;
		}

		return false;
	}

	/*
	 * main loop
	 */

	public void run()
	{
		Looper.prepare();
		mIsAppRunning = true;

		while(mIsAppRunning)
		{
			if(mIsConnected)
			{
				BTWrite(mStateLED);
				BTFlush();
				mStatePOT = BTRead();
				mStatePOT |= BTRead() << 8;
			}
			else
			{
				BTConnect();
			}
			mHandler.sendEmptyMessage(0);

			// wait briefly before sending the next packet
			try { Thread.sleep((long) (1000.0F/30.0F)); }
			catch(InterruptedException e) { Log.e(TAG, e.getMessage());}
		}

		BTDisconnect();
		mStateLED = 0;
		mStatePOT = 0;
	}

	/*
	 * update UI
	 */

	public boolean handleMessage (Message msg)
	{
		UpdateUI();
		return true;
	}

	private void UpdateUI()
	{
		mTextView.setText("Bluetooth mac address is " + mBluetoothAddress + "\n" +
		                  "Bluetooth is " + (mIsConnected ? "connected" : "disconnected") + "\n" +
		                  "LED is " + (mStateLED == 1 ? "on" : "off") + "\n" +
		                  "POT is " + mStatePOT);
	}

	/*
	 * bluetooth helper functions
	 */

	private boolean BTConnect()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
			return false;

		if(mBluetoothAdapter.isEnabled() == false)
			return false;

		try
		{
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothAddress);
			mBluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

			// discovery is a heavyweight process so
			// disable while making a connection
			mBluetoothAdapter.cancelDiscovery();

			mBluetoothSocket.connect();
			mOutputStream = mBluetoothSocket.getOutputStream();
			mInputStream = mBluetoothSocket.getInputStream();
		}
		catch (Exception e)
		{
			Log.e(TAG, "BTConnect", e);
			BTDisconnect();
			return false;
		}

		Log.i(TAG, "BTConnect");
		mIsConnected = true;
		return true;
	}

	private void BTDisconnect()
	{
		Log.i(TAG, "BTDisconnect");
		BTClose();
		mIsConnected = false;
	}

	private void BTWrite(int b)
	{
		try
		{
			mOutputStream.write(b);
		}
		catch (IOException e)
		{
			Log.e(TAG, "BTWrite" + e);
			BTDisconnect();
		}
	}

	private int BTRead()
	{
		int b = 0;
		try
		{
			b = mInputStream.read();
		}
		catch (IOException e)
		{
			Log.e(TAG, "BTRead" + e);
			BTDisconnect();
		}
		return b;
	}

	private void BTClose()
	{
		try { mOutputStream.close(); }
		catch(Exception e) { Log.e(TAG, "BTClose" + e); }

		try { mInputStream.close(); }
		catch(Exception e) { Log.e(TAG, "BTClose" + e); }

		try { mBluetoothSocket.close(); }
		catch(Exception e) { Log.e(TAG, "BTClose" + e); }

		mBluetoothSocket  = null;
		mBluetoothAdapter = null;
	}

	private void BTFlush()
	{
		if (mOutputStream != null)
		{
			try
			{
				mOutputStream.flush();
			}
			catch (IOException e)
			{
				Log.e(TAG, "BTFlush" + e);
				BTDisconnect();
			}
		}
	}
}
