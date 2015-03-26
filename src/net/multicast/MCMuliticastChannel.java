package net.multicast;

import net.messages.GetChunkMessage;
import net.messages.RemovedMessage;
import net.messages.StoredMessage;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MCMuliticastChannel extends MulticastChannel
{
    private static final int s_MAX_PACKET_SIZE = 65000;
    private byte[] m_buffer = new byte[s_MAX_PACKET_SIZE];

    public MCMuliticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }

    @Override
    public void run()
    {
        while(true)
        {
            DatagramPacket packet = new DatagramPacket();
            m_socket.receive(packet);
        }
    }

    public void sendStoredMessage(StoredMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendGetChunkMessage(GetChunkMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendDeleteMessage(StoredMessage message) throws IOException
    {
        sendMessage(message);
    }

    public void sendRemovedMessage(RemovedMessage message) throws IOException
    {
        sendMessage(message);
    }
}
