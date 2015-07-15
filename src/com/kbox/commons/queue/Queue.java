/**
 * 
 */
package com.kbox.commons.queue;

/**
 * @author jun.huang
 *
 */
public abstract interface Queue
{
  public abstract Object push(Object paramObject);

  public abstract boolean push(Object paramObject, long paramLong);

  public abstract Object pop();

  public abstract Object pop(long paramLong);

  public abstract Object peek();

  public abstract int size();

  public abstract void clear();

  public abstract boolean isEmpty();

  public abstract boolean isFull();
}
