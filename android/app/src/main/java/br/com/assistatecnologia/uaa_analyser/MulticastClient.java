package br.com.assistatecnologia.uaa_analyser;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class MulticastClient implements Runnable
{
    private static final int RECONNECTION_DELAY = 500;
    private static final int EXIT = 0;
    private static final int ERROR = 1;
    private static final int TIMEOUT = 2;

    private final String address;
    private final int port;
    private final int bufferSize;

    private OnPacketArrivedListener listener;
    private MulticastSocket socket;

    private String logTag = "MulticastClient";
    private boolean running = false;
    private boolean reconnectOnError = false;
    private int timeout = 6000;
    private Timer connectionTimer = new Timer();

    public MulticastClient(String address, int port, int bufferSize)
    {
        this.address = address;
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public MulticastClient(String address, int port) {
        this(address, port, 1024);
    }

    private String getId() {
        return String.format("(thread %s, port %d)", Thread.currentThread().getId(), this.port);
    }

    public void setPacketArriveListener(OnPacketArrivedListener listener) {
        this.listener = listener;
    }

    public void setReconnectOnError(boolean reconnect) {
        Log.i(this.logTag, "Reconnect on error: " + reconnect);
        this.reconnectOnError = reconnect;
    }

    public void setTimeout(int time) {
        this.timeout = time;
    }

    public void notifyConnection() {
        this.connectionTimer.reset();
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop()
    {
        Log.d(this.logTag, "Stopping " + getId());

        this.running = false;
        this.socket.close();
        this.socket = null;
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void run()
    {
        if (isRunning()) stop();

        int result = work();
        while (this.running && (result == ERROR && this.reconnectOnError || result == TIMEOUT))
        {
            Log.e(this.logTag, String.format("Running and set to reconnect: trying again %s",
                    getId()));

            if (result == ERROR) try { Thread.sleep(RECONNECTION_DELAY); }
            catch (InterruptedException ignored) { }

            if (this.running) result = work();
        }
    }

    private synchronized int work()
    {
        this.running = true;
        this.connectionTimer.reset().start();

        Log.i(this.logTag, String.format("Starting the client at %s %s", this.port, getId()));

        try (final MulticastSocket socket = new MulticastSocket(this.port)) {
            this.socket = socket;

            Log.i(this.logTag, "Joining group... " + getId());
            socket.joinGroup(InetAddress.getByName(this.address));
            socket.setSoTimeout(this.timeout);

            final DatagramPacket packet = new DatagramPacket(new byte[this.bufferSize], this.bufferSize);

            while (this.running) {
                try {
                    socket.receive(packet);
                    this.connectionTimer.reset();
                    this.listener.onPacketArrived(packet.getData(), packet.getLength());
                }
                catch (SocketTimeoutException e) {
                    if (this.connectionTimer.hasElapsed(this.timeout))
                    {
                        Log.e(this.logTag, "Socket timeout " + getId());
                        return TIMEOUT;
                    }
                }
            }
        }
        catch (IOException e) {
            String message = String.format("%s, %s %s", e.getClass().getSimpleName(),
                    e.getMessage(), getId());

            if (e instanceof SocketException
                    && !e.getMessage().trim().equalsIgnoreCase("socket closed"))
            {
                Log.e(this.logTag, message);
                return ERROR;
            }

            Log.i(this.logTag, message);
        }

        Log.i(this.logTag, "Client shutdown " + getId());
        return EXIT;
    }

    public interface OnPacketArrivedListener {
        void onPacketArrived(byte[] data, int length);
    }
}