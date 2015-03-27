package net.multicast;

import net.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Jo�o on 20/03/2015.
 */
public abstract class MulticastChannel implements Runnable
{
    protected static final int s_MAX_PACKET_SIZE = 65000;
    protected byte[] m_buffer = new byte[s_MAX_PACKET_SIZE];
    protected MulticastSocket m_socket;
    protected InetAddress m_address;
    protected int m_port;

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

                processHeader(packet.getData());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected abstract void processHeader(byte[] header);

    protected void sendMessage(Message message) throws IOException
    {
        byte[] data = message.toBytes();

        DatagramPacket packet = new DatagramPacket(data, data.length, m_address, m_port);

        m_socket.send(packet);
    }
}