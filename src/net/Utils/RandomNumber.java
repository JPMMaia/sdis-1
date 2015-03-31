package net.Utils;

import java.util.Random;

/**
 * Created by Miguel on 31-03-2015.
 */
public class RandomNumber
{
    static private Random m_rand = new Random();

    public static int getInt(int min, int max)
    {
        return m_rand.nextInt((max - min) + 1) + min;
    }
}
