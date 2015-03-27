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
    protected MulticastSocket m_socket;

    public MulticastChannel(String address, int port) throws IOException
    {
        m_socket = new MulticastSocket(port);
        m_socket.setTimeToLive(1);
        m_socket.joinGroup(InetAddress.getByName(address));
    }

    protected void sendMessage(Message message) throws IOException
    {
        byte[] data = message.toBytes();

        DatagramPacket packet = new DatagramPacket(data, data.length);

        m_socket.send(packet);
    }
}