package net.multicast;

import net.messages.ChunkMessage;
import net.messages.Header;
import net.messages.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

/**
 * Created by João on 25/03/2015.
 */
public class MDRMulticastChannel extends MulticastChannel
{
    public MDRMulticastChannel(String address, int port) throws IOException
    {
        super(address, port);
    }
}
