package net;

import java.io.IOException;
import java.net.*;

/**
 * Created by Jo�o on 20/03/2015.
 */
public abstract class MulticastChannel
{
    protected MulticastSocket m_socket;

    public MulticastChannel(String address, int port) throws IOException
    {
        m_socket = new MulticastSocket(port);
        m_socket.setTimeToLive(1);
        m_socket.joinGroup(InetAddress.getByName(address));
    }
}
