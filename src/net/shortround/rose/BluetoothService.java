package net.shortround.rose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothService {
	// Debugging
	private static final String TAG = "BluetoothService";
	private static final boolean D = true;
	
	// Name and UUID for the SDP record when creating server socket
	private static final String ROSE_SERVICE_NAME = "RoseService";
	private static final UUID ROSE_SERVICE_UUID = UUID.fromString("227600fc-217a-4766-83bb-49e596bb9e88");
	
	// Member fields
	private final BluetoothAdapter adapter;
	private final Handler handler;
	private AcceptThread acceptThread;
	private ConnectedThread connectedThread;
	private int state;
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // Doing nothing
	public static final int STATE_LISTEN = 1;     // Listening for a connection
	public static final int STATE_CONNECTING = 2; // Initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  // Connected to a remote device
	
	public BluetoothService(Context context, Handler handler) {
		if (D) Log.d(TAG, "New Bluetooth Service");
		
		adapter = BluetoothAdapter.getDefaultAdapter();
		state = STATE_NONE;
		this.handler = handler;
	}
	
	private synchronized void setState(int value) {
		if (D) Log.d(TAG, "setState() " + state + " -> " + value);
		state = value;
		
		// Tell the handle the state has changed
		handler.obtainMessage(RoseActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}
	
	public synchronized int getState() {
		return state;
	}
	
	public synchronized void start() {
		if (D) Log.d(TAG, "start");
		
		// Cancel any existing connected thread
		if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
		
		setState(STATE_LISTEN);
		
		// Start the accept thread
		if (acceptThread == null) {
			acceptThread = new AcceptThread();
			acceptThread.start();
		}
	}
	
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D) Log.d(TAG, "connected");
		
		// Cancel any connected threads
		if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
		
		// Cancel the accept thread because we don't need it any more
		if (acceptThread != null) { acceptThread.cancel(); acceptThread = null; }
		
		// Start the connected thread
		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		
		setState(STATE_CONNECTED);
	}
	
	public synchronized void stop() {
		if (D) Log.d(TAG, "stop");
		
		// Cancel connected thread
		if (connectedThread != null) { connectedThread.cancel(); connectedThread = null; }
		
		// Cancel the accept thread
		if (acceptThread != null) { acceptThread.cancel(); acceptThread = null; }
		
		setState(STATE_NONE);
	}
	
	public void write(byte[] out) {
		// Create a temporary connected thread
		ConnectedThread r;
		
		// Retrieve the thread
		synchronized(this) {
			if (state != STATE_CONNECTED) return;
			r = connectedThread;
		}
		
		// Perform the write
		r.write(out);
	}
	
	private void connectionLost() {
		Log.e(TAG, "Connection lost");
		
		Message message = handler.obtainMessage(RoseActivity.MESSAGE_FAILURE);
		handler.sendMessage(message);
		
		BluetoothService.this.start();
	}
	
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket serverSocket;
		
		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			
			// Create a new listening server socket
			try {
				tmp = adapter.listenUsingRfcommWithServiceRecord(ROSE_SERVICE_NAME, ROSE_SERVICE_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			
			serverSocket = tmp;
		}
		
		public void run() {
			if (D) Log.d(TAG, "BEGIN acceptThread " + this);
			setName("AcceptThread");
			
			BluetoothSocket socket = null;
			
			// Listen to the server socket if we're not connected
			while (state != STATE_CONNECTED) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}
				
				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (state) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Start the connected thread
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate the socket
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			
			if (D) Log.i(TAG, "END acceptThread");
		}
		
		public void cancel() {
			if (D) Log.d(TAG, "cancel " + this);
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}
	
	private class ConnectedThread extends Thread {
		private final InputStream inputStream;
		private final OutputStream outputStream;
		private final BluetoothSocket socket;
		
		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			this.socket = socket;
			
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}
			
			inputStream = tmpIn;
			outputStream = tmpOut;
		}
		
		public void run() {
			Log.i(TAG, "BEGIN connectedThread");
			byte[] buffer = new byte[1024];
			int bytes;
			
			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = inputStream.read(buffer);

					handler.obtainMessage(RoseActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					BluetoothService.this.start();
					break;
				}
			}
		}
		
		public void write(byte[] buffer) {
			try {
				outputStream.write(buffer);
				
				handler.obtainMessage(RoseActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}
		
		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
