package br.com.assistatecnologia.uaa_analyser;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Analyser
{
    private static final long MAX_DELAY = 2750;

    private Message lastMessage = null;
    private int messageCount = 0;
    private List<Interval> longDelays = new ArrayList<>();
    private LinkedList<Integer> lostMessages = new LinkedList<>();
    private List<Integer> unorderedMessages = new ArrayList<>();
    private int delaySum = 0;
    private int delayCount = 0;

    public int getLocalCount() {
        return this.messageCount;
    }

    public float getAverageDelay()
    {
        if (this.delayCount == 0) return 0;
        return this.delaySum / this.delayCount;
    }

    public List<Interval> getLongDelays() {
        return Collections.synchronizedList(this.longDelays);
    }

    public int getLostMessagesCount() {
        return this.lostMessages.size();
    }

    public int getUnorderedMessagesCount() {
        return this.unorderedMessages.size();
    }

    public void add(Message message)
    {
        if (this.messageCount > 0)
        {
            if (message.count < this.lastMessage.count)
            {
                Log.d("Analyser", String.format("Received unordered message (%s)",
                        message.count));

                this.unorderedMessages.add(message.count);
                this.lostMessages.remove(message.count);
            }
            else
            {
                long delay = message.time - this.lastMessage.time;

                if (delay > MAX_DELAY)
                    this.longDelays.add(new Interval(this.lastMessage.time, message.time));
                else {
                    this.delayCount++;
                    this.delaySum += delay;
                }

                int lost = this.lastMessage.count + 1;
                while (lost < message.count) this.lostMessages.add(lost++);
            }
        }

        this.lastMessage = message;
        this.messageCount++;
    }
}
