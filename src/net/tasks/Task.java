package net.tasks;

import net.IPeerDataChange;
import net.messages.Message;

/**
 * Created by Miguel on 30-03-2015.
 */
public abstract class Task implements Runnable
{
    protected IPeerDataChange m_peerAccess;
    private volatile int m_returnValue;

    public Task(IPeerDataChange peer)
    {
        m_peerAccess = peer;
    }

    public int getReturn()
    {
        return m_returnValue;
    }

    protected void setReturn(int value)
    {
        m_returnValue = value;
    }

    public boolean wantsMessage(Message message, byte[] body)
    {
        return false;
    }
}
