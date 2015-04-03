package net.tasks;

import net.IPeerDataChange;
import net.chunks.Chunk;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by João on 03/04/2015.
 */
public class SendChunkTcpTask extends Task
{
    private Chunk m_chunk;
    private InetAddress m_address;
    private int m_port;

    public SendChunkTcpTask(IPeerDataChange peerAccess, Chunk chunk, String peerAddress, int port) throws UnknownHostException
    {
        super(peerAccess);

        m_chunk = chunk;

        String[] split = peerAddress.split("[:/]");
        m_address = InetAddress.getByName(split[1]);
        m_port = port;
    }

    @Override
    public void run()
    {
        try
        {
            Socket socket = new Socket(m_address, m_port);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            byte[] data = m_chunk.getData();
            outputStream.write(data.length);
            outputStream.write(data);

            socket.close();
        }
        catch (IOException e)
        {
            System.err.println("SendChunkTcpTask::run socket is busy!");
        }
    }
}
