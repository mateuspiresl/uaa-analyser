package br.com.assistatecnologia.uaa_analyser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

public class SimpleHandler<T extends Serializable> extends Handler
{
    private MessageListener<T> listener;

    public SimpleHandler(MessageListener<T> listener) {
        this.listener = listener;
    }

    public void sendMessage(T object)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable("message", object);

        Message message = new Message();
        message.setData(bundle);

        super.sendMessage(message);
    }

    public void sendEmptyMessage() {
        super.sendMessage(new Message());
    }

    @Override
    public void handleMessage(Message message)
    {
        if (message.getData() == null)
            this.listener.handleMessage(null);
        else
            this.listener.handleMessage((T) message.getData().getSerializable("message"));
    }

    public interface MessageListener<T extends Serializable> {
        void handleMessage(T message);
    }
}
