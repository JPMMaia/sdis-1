package net.services;

/**
 * Created by Miguel on 30-03-2015.
 */
public abstract class UserService implements Runnable
{
    public enum ServiceType {UPDATE, RESTORE};
    private ServiceType m_type;

    public ServiceType getType()
    {
        return m_type;
    }



}
