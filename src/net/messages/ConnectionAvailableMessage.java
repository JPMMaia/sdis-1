package net.messages;

import java.net.InetAddress;

/**
 * Created by João on 02/04/2015.
 */
public class ConnectionAvailableMessage implements IHeaderLine
{
    public static final String s_TYPE = "Connection Available Message";

    private int m_port;

    public ConnectionAvailableMessage(int port)
    {
        m_port = port;
    }

    @Override
    public String toString()
    {
        return s_TYPE + " ";
    }

    @Override
    public String getType()
    {
        return s_TYPE;
    }
}
