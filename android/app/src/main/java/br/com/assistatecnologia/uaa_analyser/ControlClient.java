package br.com.assistatecnologia.uaa_analyser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ControlClient extends MulticastClient implements MulticastClient.OnPacketArrivedListener
{
    private static final String LABEL_TIMELINE = "timeline";
    private static final String LABEL_CONNECTED = "connected";
    private static final String LABEL_STATUS = "status";

    private final ControlUpdateListener listener;
    private Control control = new Control(Status.STOPPED, 0L, true);
    private long messageCounter = 0, messageInterval = 0;
    private Timer messageTimer = new Timer();
    private int iasCounter = -1;

    /**
     * This constructor makes it use a statusListener, that is called from the client thread.
     * @param listener The listener.
     */
    public ControlClient(ControlUpdateListener listener)
    {
        super(C.MULTICAST_ADDRESS, C.CONTROL_PORT);
        super.setPacketArriveListener(this);
        super.setReconnectOnError(true);
        this.listener = listener;
    }

    /**
     * This constructor makes it use a handler given to send the content.
     * @param externalThread the handler for status and sync.
     */
    @SuppressWarnings("WeakerAccess")
    public ControlClient(final SimpleHandler<Control> externalThread)
    {
        this(new ControlUpdateListener() {
            @Override
            public void onControlUpdate(Control control) {
                externalThread.sendMessage(control);
            }
        });
    }

    /**
     * This constructor makes it use a listener in the thread where it's been created.
     * @param listener the handler listener for status and sync.
     */
    @SuppressWarnings("WeakerAccess")
    public ControlClient(SimpleHandler.MessageListener<Control> listener) {
        this(new SimpleHandler<>(listener));
    }

    @Override
    public void start() {
        super.start();
        this.messageTimer.reset().start();
    }

    /**
     * Parses the data to the status type and sends to the statusListener.
     * @param data the raw data that comes from the server.
     */
    @Override
    public void onPacketArrived(byte[] data, int length)
    {
        String dataAsString = new String(data, 0, length);
        Log.d("ControlClient", "Received: " + dataAsString);

        this.messageCounter++;
        this.messageInterval = this.messageTimer.getElapsed();
        this.messageTimer.reset();

        try {
            JSONObject json = new JSONObject(dataAsString);
            if (json.has("counter")) this.iasCounter = json.getInt("counter");

            Long sync = json.getLong(LABEL_TIMELINE);
            boolean connected = json.getBoolean(LABEL_CONNECTED);
            Status status = Status.parse(json.getInt(LABEL_STATUS));

            this.control = new Control(status, sync, connected);
            this.listener.onControlUpdate(this.control);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Control getControl() {
        return this.control;
    }
    public long getMessageCounter() {
        return this.messageCounter;
    }
    public long getMessageInterval() {
        return this.messageInterval;
    }
    public int getIasCounter() {
        return this.iasCounter;
    }

    /**
     * A statusListener for status.
     */
    public interface ControlUpdateListener {
        void onControlUpdate(Control control);
    }

    public static class Control implements Serializable
    {
        public final Status status;
        public final Long sync;
        public final boolean connected;

        Control(Status status, Long sync, boolean connected)
        {
            this.status = status;
            this.sync = sync;
            this.connected = connected;
        }
    }
}
