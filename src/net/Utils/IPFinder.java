package net.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Miguel on 02-04-2015.
 */
public class IPFinder
{
    final static String ADDR = "239.1.0.9";
    final static int PORT = 8888;

    public static String getIP()
    {
        try
        {
            MulticastSocket socket = new MulticastSocket(PORT);
            socket.joinGroup(InetAddress.getByName(ADDR));

            String ss = "test";
            DatagramPacket packet = new DatagramPacket(ss.getBytes(), ss.getBytes().length, InetAddress.getByName(ADDR), PORT);
            socket.send(packet);

            byte[] array = new byte[50];
            DatagramPacket packet2 = new DatagramPacket(array, array.length);
            socket.receive(packet2);

            return packet2.getAddress().toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Error getting IP");
            System.exit(-5);
        }

        return null;
    }
}
