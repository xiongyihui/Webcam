package cn.xiongyihui.webcam;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

public abstract class SimpleServer implements Runnable {
    public final static String TAG = "Acore";
    
    private final CopyOnWriteArrayList<ConnectionThread> mConnectionThreads = 
            new CopyOnWriteArrayList<ConnectionThread>();
    private Callback mCallback = null;
    
    private ServerSocket mServer;
    private volatile boolean mStopServer = true;
    private Thread mServerThread;
    
    public interface Callback {
        public void onConnect();
        
        public void onDisconnect();
    }
    
    protected abstract void handleConnection(Socket socket) throws IOException;

    public void run() {
        Log.v(TAG, "Server is running");
        
        while (!mStopServer) {
            try {
                Socket socket = mServer.accept();
                
                Log.v(TAG, "Accept socket: " + socket.getPort());
                if (!mStopServer) {
                    startConnectionThread(socket);
                } else {
                    socket.close();
                }
            } catch (IOException e) {
                if (!mStopServer) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
    
    public void start(int port) throws IOException {
        mServer = new ServerSocket(port);
        
        mStopServer = false;
        mServerThread = new Thread(this);
        mServerThread.start();
    }
    
    public void close() {
        mStopServer = true;
        if (mServer != null) {
            try {
                mServer.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        
        for (ConnectionThread connectionThread : mConnectionThreads) {
            connectionThread.close();
        }
        
        mCallback = null;
    }
    
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    
    public int getNumberOfConnections() {
        return mConnectionThreads.size();
    }
    
    
    private final class ConnectionThread extends Thread {
        private final Socket mmSocket;
        
        private ConnectionThread(Socket socket) {
            setName("SimpleServer ConnectionThread" + getId());
            mmSocket = socket;
        }
        
        @Override
        public void run() {
            Log.v(TAG, "Server thread " + getId() + " started");
            
            if (mCallback != null) {
                mCallback.onConnect();
            }
            
            try {
                handleConnection(mmSocket);
            } catch (IOException e) {
                if (!mStopServer) {
                    Log.e(TAG, e.getMessage());
                }
            }
            
            if (mCallback != null) {
                mCallback.onDisconnect();
            }
            
            Log.v(TAG, "Server thread " + getId() + " died");
        }
        
        private void close() {
            if (mmSocket != null) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
    
    private void startConnectionThread(final Socket socket) {
        ConnectionThread connectionThread = new ConnectionThread(socket);
        mConnectionThreads.add(connectionThread);
        
        connectionThread.start();
    }
}
