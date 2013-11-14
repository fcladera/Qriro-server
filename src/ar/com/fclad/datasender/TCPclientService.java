package ar.com.fclad.datasender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TCPclientService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	 
	
	
	public static final String NOTIFICATION = "ar.com.fclad.datasender";
	
	public static final String COMMAND = "command";
	public static final String SERVER = "server";
	public static final String PORT = "port";
	public static final int CONNECT = 1;
	public static final int DISCONNECT = 0;
	
	public static final String STATUS = "status"; 
	public static final int CONNECTED = 1;
	public static final int DISCONNECTED = 0; 
	public static final int GETSTATUS = 3;

	
	private Socket socket;
	PrintWriter writer = null;
	private boolean isSending = false;
	private boolean isConnected = false;
	
	private String server = null;
	private int port = -1;
	
	private static final int timeout = 1000;

	
	
	public TCPclientService() {
		
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
	    }
	    @Override
	    public void handleMessage(Message msg) {
	    	switch(msg.getData().getInt(COMMAND)){
	    	case CONNECT:
	    		if(!isConnected){
	    			server = msg.getData().getString(SERVER);
	    			port = msg.getData().getInt(PORT);
	    			Log.d("TCPclienService","Connecting with server "+server+" port "+port);
	    			new Connect().execute(server);
	    			
	    		}
	    		break;
	    	case DISCONNECT:
	    		if(isConnected){
	    			Log.d("TCPclienService","Disconnecting from server");
	    			disconnect();
	    			
	    		}
	    		stopSelf();	// Destroy service on disconnection
	    			
	    		break;
	    	default:
	    		break;
	    	
	    	}
	    		    	
	    	
//	    	long endTime = System.currentTimeMillis() + 2*1000;
//	    	while (System.currentTimeMillis() < endTime) {
//	    		synchronized (this) {
//	    			try {
//	    				wait(endTime - System.currentTimeMillis());
//	    				Log.w("ServiceHandler",""+msg.arg1);
//	    				Log.w("ServiceHandler",""+msg.getData().getString("Test"));
//	    			} catch (Exception e) {
//	    			}
//		      }
//	    	}
	    	
	    	
			
			
			
			//stopSelf(msg.arg1);
	    }
	}

	private class Connect extends AsyncTask<String, Void, String>{
		/**
		 * Tries to connect in background to the socket.
		 * If succed, UI is changed allowing to send data
		 * If fails, show toast
		 */

		@Override
		protected String doInBackground(String... params) {
				//Toast.makeText(getApplicationContext(), "Connecting to server "+params[0].toString(), Toast.LENGTH_SHORT).show();
				InetSocketAddress serverAddr = new InetSocketAddress(params[0], port);
				socket = new Socket();
				try {
					
					socket.connect(serverAddr, timeout);
					isConnected = true;
					Intent intent = new Intent(NOTIFICATION); 
					intent.putExtra(STATUS, CONNECTED);
	    			sendBroadcast(intent);
				} catch (IOException e) {
					//e.printStackTrace();
					Log.e("Socket","Connection error");
					isConnected = false;
					Intent intent = new Intent(NOTIFICATION);
					intent.putExtra(STATUS, DISCONNECTED);
					sendBroadcast(intent);
					
					stopSelf();
				}
				return params[0];
		}
		
		protected void onPostExecute(String result){
			if(isConnected){
				Toast.makeText(getApplicationContext(), "Successfully connected to "+result, Toast.LENGTH_SHORT).show();
				//if(result==localServer){
					//connectRemote.setEnabled(false);
			//	}
				//else{
					
					//connectLocal.setEnabled(false);
			//	}
				//sendData.setEnabled(true);
			}
			else{
				Toast.makeText(getApplicationContext(), "Error connecting to "+result, Toast.LENGTH_SHORT).show();
				stopSelf();
			}
		}
		
	}
	
	private void disconnect(){
		// disconnect socket
		try {
			socket.close();
		} catch (IOException e) {
			Log.e("MainActivity", "Error on disconnect");
			stopSelf();
			//e.printStackTrace();
		}
		//sendData.setEnabled(false);
		//connectRemote.setEnabled(true);
		//connectLocal.setEnabled(true);
		isConnected = false;
		socket = null;
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(STATUS, DISCONNECTED);
		sendBroadcast(intent);
	}

	
	public void onCreate() {
		HandlerThread thread = new HandlerThread("TCPclientThread");
		thread.start();
		Log.w("TCPclientService","Service created");
		mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	    

	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	 @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.w("TCPclientService","Service started");
	    Message msg = mServiceHandler.obtainMessage();
	    msg.arg1 = startId;
	    msg.setData(intent.getExtras());
	    mServiceHandler.sendMessage(msg);
	    
	    
	    
	    return Service.START_NOT_STICKY;
	  }
	 
	 public void onDestroy(){
		 Log.w("TCPclientService","Service destroyed");
	 }

	 
}
