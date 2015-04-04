package net.tasks;

import net.services.RestoreService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by João on 03/04/2015.
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
            System.out.println("ReceiveChunkTcpTask::run begin!");

            Socket socket = m_serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            // Flag for sending data:
            outputStream.writeBoolean(true);

            int length;
            length = inputStream.readInt();
            byte[] data = new byte[length];

            while(length != inputStream.available());

            inputStream.read(data, 0, length);

            socket.close();
            m_serverSocket.close();

            // Send data received to the restore service:
            m_restoreService.setBody(data);

            System.out.println("ReceiveChunkTcpTask::run end!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
