/**
 * 
 */
package com.kbox.commons.queue;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jun.huang
 * 
 */
public class DefaultQueue implements Queue {
	private int size = 0;
	private List list;
	private long pushNum = 0L;
	private long popNum = 0L;
	protected Object pushLock = new Object() {
	};
	protected Object popLock = new Object() {
	};

	public DefaultQueue(int size) {
		if (size < 0)
			throw new IllegalArgumentException(
					"create queue fail cause size < 0");
		this.size = size;
		this.list = Collections.synchronizedList(new LinkedList());
	}

	public Object push(Object ob) {
		if (ob == null)
			return null;
		Object ret = null;
		synchronized (this.pushLock) {
			if (this.list.size() < this.size) {
				this.list.add(ob);
			} else {
				ret = this.list.remove(0);
				this.list.add(ob);
			}
			this.pushNum += 1L;
		}

		if (ret == null) {
			synchronized (this.popLock) {
				this.popLock.notify();
			}
		}
		return ret;
	}

	public boolean push(Object ob, long timeout) {
		if (ob == null)
			return false;
		if (timeout < 0L)
			timeout = 0L;
		boolean b = false;
		synchronized (this.pushLock) {
			if (this.list.size() < this.size) {
				this.list.add(ob);
				b = true;
			} else {
				long tt = 0L;
				while (!b) {
					if ((timeout > 0L) && (tt >= timeout)) {
						return false;
					}
					long st = System.currentTimeMillis();
					try {
						if (timeout == 0L)
							this.pushLock.wait();
						else
							this.pushLock.wait(timeout - tt);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (this.list.size() < this.size) {
						this.list.add(ob);
						b = true;
					}
					tt += System.currentTimeMillis() - st;
				}
			}
		}
		if (b) {
			synchronized (this.popLock) {
				this.pushNum += 1L;
				this.popLock.notify();
			}
		}
		return b;
	}

	public Iterator iterator() {
		return new Iterator() {
			List l = DefaultQueue.this.list;
			int s = this.l.size();

			public boolean hasNext() {
				return (this.s > 0) && (this.l.size() > 0);
			}

			public Object next() {
				if (this.l.size() > 0) {
					if (this.s > this.l.size()) {
						this.s = this.l.size();
					}

					this.s -= 1;
					Object o = this.l.get(this.s);
					return o;
				}
				throw new ArrayIndexOutOfBoundsException();
			}

			public void remove() {
				if ((this.s >= 0) && (this.l.size() > this.s)) {
					DefaultQueue.this.list.remove(this.s);
				}
			}
		};
	}

	public Iterator invert_iterator() {
		return this.list.iterator();
	}

	public int size() {
		return this.list.size();
	}

	public void clear() {
		this.pushNum = (this.popNum = 0L);
		this.list.clear();
	}

	public int getMaxSize() {
		return this.size;
	}

	public void setMaxSize(int s) {
		this.size = s;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean isFull() {
		return size() == getMaxSize();
	}

	public Object pop() {
		Object o = null;
		synchronized (this.popLock) {
			if (this.list.size() > 0) {
				o = this.list.remove(0);
			}
		}
		if (o != null) {
			synchronized (this.pushLock) {
				this.popNum += 1L;
				this.pushLock.notify();
			}
		}
		return o;
	}

	public Object pop(long timeout) {
		if (timeout < 0L)
			timeout = 0L;
		Object ret = null;
		synchronized (this.popLock) {
			if (this.list.size() > 0) {
				ret = this.list.remove(0);
			} else {
				long tt = 0L;

				while (ret == null) {
					if ((timeout > 0L) && (tt >= timeout)) {
						return null;
					}

					long st = System.currentTimeMillis();
					try {
						if (timeout == 0L)
							this.popLock.wait();
						else
							this.popLock.wait(timeout - tt);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (this.list.size() > 0) {
						ret = this.list.remove(0);
					}
					tt += System.currentTimeMillis() - st;
				}
			}
		}
		if (ret != null) {
			synchronized (this.pushLock) {
				this.popNum += 1L;
				this.pushLock.notify();
			}
		}
		return ret;
	}

	public Object peek() {
		synchronized (this.popLock) {
			if (this.list.size() > 0) {
				return this.list.get(0);
			}

			return null;
		}
	}

	public long getPopNum() {
		return this.popNum;
	}

	public long getPushNum() {
		return this.pushNum;
	}
}
