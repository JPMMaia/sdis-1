package net;

/**
 * Created by João on 20/03/2015.
 */
public abstract class MulticastChannel implements Runnable
{
    protected String m_address;
    protected String m_port;

    public MulticastChannel(String address, String port)
    {
        m_address = address;
        m_port = port;
    }
}
