package net;

import net.multicast.MCMuliticastChannel;
import net.multicast.MDBMulticastChannel;
import net.multicast.MDRMulticastChannel;

import java.io.IOException;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class Peer
{
    private MCMuliticastChannel m_mcChannel;
    private MDBMulticastChannel m_mdbChannel;
    private MDRMulticastChannel m_mdrChannel;

    public Peer(String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException
    {
        m_mcChannel = new MCMuliticastChannel(mcAddress, mcPort);
        m_mdbChannel = new MDBMulticastChannel(mdbAddress, mdbPort);
        m_mdrChannel = new MDRMulticastChannel(mdrAddress, mdrPort);
    }



    public static void main(String[] args) throws IOException
    {
        if(args.length != 6)
        {
            System.err.println("Peer::main: Number of arguments must be 6!");
            return;
        }

        String mcAddress = args[0];
        int mcPort = Integer.parseInt(args[1]);
        String mdbAddress = args[2];
        int mdbPort = Integer.parseInt(args[3]);
        String mdrAddress = args[4];
        int mdrPort = Integer.parseInt(args[5]);

        Peer peer = new Peer(mcAddress, mcPort, mdbAddress, mdbPort, mdrAddress, mdrPort);
    }
}
