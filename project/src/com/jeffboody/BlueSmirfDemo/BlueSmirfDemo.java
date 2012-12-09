/*
 * Copyright (c) 2011-2012 Jeff Boody
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
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.Intent;
import android.util.Log;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.util.Set;
import java.util.ArrayList;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.jeffboody.BlueSmirf.BlueSmirfSPP;

public class BlueSmirfDemo extends Activity implements Runnable, Handler.Callback, OnItemSelectedListener
{
	private static final String TAG = "BlueSmirfDemo";

	// app state
	private BlueSmirfSPP      mSPP;
	private boolean           mIsThreadRunning;
	private String            mBluetoothAddress;
	private ArrayList<String> mArrayListBluetoothAddress;

	// UI
	private TextView     mTextViewStatus;
	private Spinner      mSpinnerDevices;
	private ArrayAdapter mArrayAdapterDevices;
	private Handler      mHandler;

	// Arduino state
	private int mStateLED;
	private int mStatePOT;

	public BlueSmirfDemo()
	{
		mIsThreadRunning           = false;
		mBluetoothAddress          = null;
		mSPP                       = new BlueSmirfSPP();
		mStateLED                  = 0;
		mStatePOT                  = 0;
		mArrayListBluetoothAddress = new ArrayList<String>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// initialize UI
		setContentView(R.layout.main);
		mTextViewStatus         = (TextView) findViewById(R.id.ID_STATUS);
		ArrayList<String> items = new ArrayList<String>();
		mSpinnerDevices         = (Spinner) findViewById(R.id.ID_PAIRED_DEVICES);
		mArrayAdapterDevices    = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		mHandler                = new Handler(this);
		mSpinnerDevices.setOnItemSelectedListener(this);
		mArrayAdapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerDevices.setAdapter(mArrayAdapterDevices);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// update the paired device(s)
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		mArrayAdapterDevices.clear();
		mArrayListBluetoothAddress.clear();
		if(devices.size() > 0)
		{
			for(BluetoothDevice device : devices)
			{
				mArrayAdapterDevices.add(device.getName());
				mArrayListBluetoothAddress.add(device.getAddress());
			}

			// request that the user selects a device
			if(mBluetoothAddress == null)
			{
				mSpinnerDevices.performClick();
			}
		}
		else
		{
			mBluetoothAddress = null;
		}

		UpdateUI();
	}

	@Override
	protected void onPause()
	{
		mSPP.disconnect();
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	/*
	 * Spinner callback
	 */

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		mBluetoothAddress = mArrayListBluetoothAddress.get(pos);
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
		mBluetoothAddress = null;
	}

	/*
	 * buttons
	 */

	public void onBluetoothSettings(View view)
	{
		Intent i = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(i);
	}

	public void onToggleLED(View view)
	{
		if(mSPP.isConnected())
		{
			mStateLED = 1 - mStateLED;
		}
	}

	public void onConnectLink(View view)
	{
		if(mIsThreadRunning == false)
		{
			mIsThreadRunning = true;
			UpdateUI();
			Thread t = new Thread(this);
			t.start();
		}
	}

	public void onDisconnectLink(View view)
	{
		mSPP.disconnect();
	}

	/*
	 * main loop
	 */

	public void run()
	{
		Looper.prepare();
		mSPP.connect(mBluetoothAddress);
		while(mSPP.isConnected())
		{
			mSPP.writeByte(mStateLED);
			mSPP.flush();
			mStatePOT = mSPP.readByte();
			mStatePOT |= mSPP.readByte() << 8;

			if(mSPP.isError())
			{
				mSPP.disconnect();
			}

			mHandler.sendEmptyMessage(0);

			// wait briefly before sending the next packet
			try { Thread.sleep((long) (1000.0F/30.0F)); }
			catch(InterruptedException e) { Log.e(TAG, e.getMessage());}
		}

		mStateLED        = 0;
		mStatePOT        = 0;
		mIsThreadRunning = false;
		mHandler.sendEmptyMessage(0);
	}

	/*
	 * update UI
	 */

	public boolean handleMessage (Message msg)
	{
		// received update request from Bluetooth IO thread
		UpdateUI();
		return true;
	}

	private void UpdateUI()
	{
		if(mSPP.isConnected())
		{
			mTextViewStatus.setText("connected to " + mSPP.getBluetoothAddress() + "\n" +
			                        "LED is " + (mStateLED == 1 ? "on" : "off") + "\n" +
			                        "POT is " + mStatePOT);
		}
		else if(mIsThreadRunning)
		{
			mTextViewStatus.setText("connecting to " + mBluetoothAddress);
		}
		else
		{
			mTextViewStatus.setText("disconnected");
		}
	}
}
