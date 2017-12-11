package br.com.assistatecnologia.uaa_analyser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Interval
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm HH:mm:ss", Locale.getDefault());
    public final long begin, end;

    public Interval(long begin, long end)
    {
        this.begin = begin;
        this.end = end;
    }

    public long getTime() {
        return this.end - this.begin;
    }

    @Override
    public String toString()
    {
        return String.format("%s - %s : %s", dateFormat.format(new Date(this.begin)),
                dateFormat.format(new Date(this.end)), getTime());
    }
}
