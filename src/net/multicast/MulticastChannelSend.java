package net.multicast;

import net.messages.Header;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Miguel on 02-04-2015.
 */
public class MulticastChannelSend
{
    private MulticastSocket m_socket;

    public MulticastChannelSend() throws IOException
    {
        m_socket = new MulticastSocket();
        m_socket.setTimeToLive(1);
        m_socket.setLoopbackMode(false);
    }

    public int getLocalPort()
    {
        return m_socket.getLocalPort();
    }

    /*
    public void sendHeader(Header header) throws IOException
    {
        byte[] data = header.toBytes();

        System.out.println("DEBUG: Sent " + header.getMessage(0).getType());

        DatagramPacket packet = new DatagramPacket(data, data.length, m_address, m_port);

        m_socket.send(packet);
    }
    */

    public void sendHeader(Header header, InetAddress address, int port) throws IOException
    {
        byte[] data = header.toBytes();

        System.out.println("DEBUG: Sent " + header.getMessage(0).getType());

        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

        m_socket.send(packet);
    }
}
