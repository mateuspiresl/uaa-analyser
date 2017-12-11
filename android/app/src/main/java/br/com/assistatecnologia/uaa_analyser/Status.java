package br.com.assistatecnologia.uaa_analyser;

public enum Status {
    STOPPED, PLAYING, PAUSED, TERMINATED, NONE;

    private static final Status[] values;
    static { values = Status.values(); }

    public static Status parse(int code)
    {
        try {
            return values[code];
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
