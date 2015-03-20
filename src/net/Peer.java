package net;

/**
 * Created by João on 20/03/2015.
 */
public class Peer
{
    public Peer(String mcAddress, String mcPort, String mdbAddress, String mdbPort, String mdrAddress, String mdrPort)
    {
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
