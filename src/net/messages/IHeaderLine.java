package net.messages;

import java.io.Serializable;

/**
 * Created by João on 02/04/2015.
 */
public interface IHeaderLine extends Serializable
{
    String toString();
    String getType();
}
