package net.multicast;

import net.messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MCMulticastChannel extends MulticastChannel
{
    public MCMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
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
