package net.multicast;

import net.messages.Header;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MulticastChannelReceive implements Runnable
{
    private static final int s_MAX_PACKET_SIZE = 65000;
    private byte[] m_buffer = new byte[s_MAX_PACKET_SIZE];
    private MulticastSocket m_socket;
    private InetAddress m_address;
    private int m_port;
    private List<IMulticastChannelListener> m_listeners = new ArrayList<>();

    public MulticastChannelReceive(String address, int port) throws IOException
    {
        m_address = InetAddress.getByName(address);
        m_port = port;

        m_socket = new MulticastSocket(port);
        m_socket.setTimeToLive(1);
        m_socket.joinGroup(m_address);
        m_socket.setLoopbackMode(false);
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(m_buffer, s_MAX_PACKET_SIZE);
                m_socket.receive(packet);

                notifyDataReceived(packet.getData(), packet.getLength(), packet.getAddress() + ":" + Integer.toString(packet.getPort())); // TODO: por agora tá com a porta
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(-5);
            }
        }
    }

    public InetAddress getAddress()
    {
        return m_address;
    }

    public int getPort()
    {
        return m_port;
    }

    public void addListener(IMulticastChannelListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeListener(IMulticastChannelListener listener)
    {
        m_listeners.remove(listener);
    }

    private void notifyDataReceived(byte[] data, int length, String peerAddress)
    {
        for (IMulticastChannelListener listener : m_listeners)
        {
            listener.onDataReceived(data, length, peerAddress);
        }
    }
}