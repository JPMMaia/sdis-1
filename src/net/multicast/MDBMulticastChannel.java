package net.multicast;

import net.messages.Header;
import net.messages.Message;
import net.messages.PutChunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class MDBMulticastChannel extends MulticastChannel
{
    public MDBMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }
}
