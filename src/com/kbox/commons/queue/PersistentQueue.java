/**
 * 
 */
package com.kbox.commons.queue;

import java.io.Serializable;

/**
 * @author jun.huang
 *
 */
public abstract interface PersistentQueue
{
  public abstract boolean push(Serializable paramSerializable);

  public abstract Serializable pop();

  public abstract Serializable pop(long paramLong);

  public abstract Serializable peek();

  public abstract int size();

  public abstract int capacity();

  public abstract void clear();

  public abstract boolean isEmpty();

  public abstract boolean isFull();
}