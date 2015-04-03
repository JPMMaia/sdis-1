package net.tasks;

import net.services.RestoreService;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by João on 03/04/2015.
 */
public class ReceiveChunkTcpTask implements Runnable
{
    private RestoreService m_restoreService;
    private int m_port;

    public ReceiveChunkTcpTask(RestoreService restoreService, int port)
    {
        m_restoreService = restoreService;
        m_port = port;
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(m_port);

            Socket socket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            int length;
            length = inputStream.read();
            byte[] data = new byte[length];

            int readLength = 0;
            while(readLength < length)
                readLength += inputStream.read(data, readLength, length);

            socket.close();
            serverSocket.close();

            // Send data received to the restore service:
            m_restoreService.setBody(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
