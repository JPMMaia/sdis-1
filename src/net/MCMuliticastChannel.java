package net;

/**
 * Created by Jo�o on 20/03/2015.
 */
public class MCMuliticastChannel extends MulticastChannel
{
    public MCMuliticastChannel(String address, String port)
    {
        super(address, port);
    }

    @Override
    public void run()
    {
    }
}
