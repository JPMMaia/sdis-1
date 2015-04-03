package net.messages;

import java.security.InvalidParameterException;

/**
 * Created by João on 02/04/2015.
 */
public class TcpAvailableMessage implements IHeaderLine
{
    public static final String s_TYPE = "TCP_AVAILABLE";

    private int m_port;

    public TcpAvailableMessage(int port)
    {
        m_port = port;
    }

    @Override
    public String toString()
    {
        return s_TYPE + " " + m_port;
    }

    @Override
    public String getType()
    {
        return s_TYPE;
    }

    public int getPort()
    {
        return m_port;
    }

    public static TcpAvailableMessage createMessage(String[] fields) throws InvalidParameterException
    {
        if(fields.length != 2)
            throw new InvalidParameterException("TcpAvailableMessage::createMessage Must have a length of at least 2!");

        TcpAvailableMessage message = new TcpAvailableMessage(Integer.parseInt(fields[1]));

        return message;
    }
}
