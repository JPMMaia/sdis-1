package net.tasks;

import net.services.RestoreService;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jo�o on 03/04/2015.
 */
public class ReceiveChunkTcpTask implements Runnable
{
    private RestoreService m_restoreService;
    private ServerSocket m_serverSocket;

    public ReceiveChunkTcpTask(RestoreService restoreService) throws IOException
    {
        m_restoreService = restoreService;
        m_serverSocket = new ServerSocket(0);
    }

    public int getServerSocketPort()
    {
        return m_serverSocket.getLocalPort();
    }

    @Override
    public void run()
    {
        try
        {
            Socket socket = m_serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            int length;
            length = inputStream.read();
            byte[] data = new byte[length];

            int readLength = 0;
            while(readLength < length)
                readLength += inputStream.read(data, readLength, length);

            socket.close();
            m_serverSocket.close();

            // Send data received to the restore service:
            m_restoreService.setBody(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}