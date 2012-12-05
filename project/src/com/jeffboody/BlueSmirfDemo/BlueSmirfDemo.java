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
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.FileReader;
import com.jeffboody.BlueSmirf.BlueSmirfSPP;

public class BlueSmirfDemo extends Activity implements Runnable, Handler.Callback
{
	private static final String TAG = "BlueSmirfDemo";

	// menu constant(s)
	private static final int MENU_TOGGLE_LED = 0;

	// app state
	private BlueSmirfSPP mSPP;
	private boolean      mIsAppRunning;
	private TextView     mTextView;
	private Handler      mHandler;
	private String       mBluetoothAddress;

	// Arduino state
	private int mStateLED;
	private int mStatePOT;

	public BlueSmirfDemo()
	{
		mIsAppRunning     = false;
		mBluetoothAddress = null;
		mSPP              = new BlueSmirfSPP();
		mStateLED         = 0;
		mStatePOT         = 0;
	}

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
			buf.close();
		}
		catch(Exception e)
		{
			Log.e(TAG, "onResume: ", e);
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
			if(mSPP.isConnected())
			{
				mSPP.writeByte(mStateLED);
				mSPP.flush();
				mStatePOT = mSPP.readByte();
				mStatePOT |= mSPP.readByte() << 8;

				if(mSPP.isError())
				{
					mSPP.disconnect();
				}
			}
			else
			{
				mSPP.connect(mBluetoothAddress);
			}
			mHandler.sendEmptyMessage(0);

			// wait briefly before sending the next packet
			try { Thread.sleep((long) (1000.0F/30.0F)); }
			catch(InterruptedException e) { Log.e(TAG, e.getMessage());}
		}

		mSPP.disconnect();
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
		                  "Bluetooth is " + (mSPP.isConnected() ? "connected" : "disconnected") + "\n" +
		                  "LED is " + (mStateLED == 1 ? "on" : "off") + "\n" +
		                  "POT is " + mStatePOT);
	}
}
