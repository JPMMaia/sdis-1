package net.multicast;

import net.messages.Header;
import net.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jo�o on 20/03/2015.
 */
public abstract class MulticastChannel implements Runnable
{
    private static final int s_MAX_PACKET_SIZE = 65000;
    private byte[] m_buffer = new byte[s_MAX_PACKET_SIZE];
    private MulticastSocket m_socket;
    private InetAddress m_address;
    private int m_port;
    private List<IMulticastChannelListener> m_listeners = new ArrayList<>();

    public MulticastChannel(String address, int port) throws IOException
    {
        m_address = InetAddress.getByName(address);
        m_port = port;

        m_socket = new MulticastSocket(port);
        m_socket.setTimeToLive(1);
        m_socket.joinGroup(m_address);
        m_socket.setLoopbackMode(true);
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

                notifyDataReceived(packet.getData(), packet.getAddress().toString());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addListener(IMulticastChannelListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeListener(IMulticastChannelListener listener)
    {
        m_listeners.remove(listener);
    }

    public void sendHeader(Header header) throws IOException
    {
        byte[] data = header.toBytes();

        DatagramPacket packet = new DatagramPacket(data, data.length, m_address, m_port);

        m_socket.send(packet);
    }

    private void notifyDataReceived(byte[] data, String peerAddress)
    {
        for (IMulticastChannelListener listener : m_listeners)
        {
            listener.onDataReceived(data, peerAddress);
        }
    }
}