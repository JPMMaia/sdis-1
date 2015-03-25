package net;

import filemanagement.Chunk;

import java.util.ArrayList;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class Peer
{
    private ArrayList<Chunk> m_chunks = new ArrayList<Chunk>();

    public Peer(String mcAddress, String mcPort, String mdbAddress, String mdbPort, String mdrAddress, String mdrPort)
    {
        // Read config file, or receive requests to backup a certain file

        // For each chunk, send putchunk messages
    }

    public static void main(String[] args)
    {
        if(args.length != 6)
        {
            System.err.println("Peer::main: Number of arguments must be 6!");
            return;
        }

        String mcAddress = args[0];
        String mcPort = args[1];
        String mdbAddress = args[2];
        String mdbPort = args[3];
        String mdrAddress = args[4];
        String mdrPort = args[5];

        Peer peer = new Peer(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
    }
}
