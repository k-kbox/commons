/**
 * 
 */
package com.kbox.commons.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author jun.huang
 *
 */
public class FilePersistentQueue implements PersistentQueue, Runnable{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	protected static String FILE_EXTEND = ".pq";

	  protected static String MQ_FILENAME = ".memery_queue";
	  protected File dir;
	  protected DefaultQueue mq;
	  protected int memeryCapacity;
	  protected int storageCapacity;
	  protected LinkedList<File> files = new LinkedList();

	  protected long totalFileLength = 0L;
	  protected Thread m_thread;
	  protected static Logger logger = Logger.getLogger(Runnable.class);

	  protected Object lock = new Object() { } ;

	  protected int counter = 0;
	  protected long lastCounterTime = 0L;

	  public FilePersistentQueue(File dir)
	  {
	    this(dir, 1024, 524288);
	  }

	  public FilePersistentQueue(File dir, int memeryCapacity, int storageCapacity)
	  {
	    if ((dir == null) || (!dir.exists()) || (!dir.isDirectory())) {
	      throw new IllegalArgumentException("dir [" + dir + "] not exists.");
	    }
	    this.dir = dir;
	    this.memeryCapacity = memeryCapacity;
	    this.storageCapacity = storageCapacity;

	    this.mq = new DefaultQueue(memeryCapacity);

	    loadMemeryQueue();
	    loadStorageQueue();

	    this.m_thread = new Thread(this, "PersistentQueue-Thread");
	    this.m_thread.start();

	    Runtime.getRuntime().addShutdownHook(new MyShutdownHook());
	  }

	  protected String getQueueStatusForLogger()
	  {
	    return "[" + getMemerySize() + "/" + getMemeryCapacity() + "] [" + getStorageSize() + "/" + getStorageCapacity() + "] [" + this.totalFileLength + "]";
	  }

	  protected void loadStorageQueue()
	  {
	    this.files.clear();

	    long startTime = System.currentTimeMillis();

	    loadFiles(this.dir, this.files);

	    logger.info("[load-storage-queue] [OK] [" + this.dir.getAbsolutePath() + "] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]");

	    Comparator comparator = new Comparator() {
	      public int compare(Object o1, Object o2) {
	        File f1 = (File)o1;
	        File f2 = (File)o2;

	        if (f1.lastModified() / 1000L < f2.lastModified() / 1000L)
	          return -1;
	        if (f1.lastModified() / 1000L > f2.lastModified() / 1000L) {
	          return 1;
	        }
	        String s1 = f1.getName().substring(0, f1.getName().length() - FilePersistentQueue.FILE_EXTEND.length());
	        String s2 = f2.getName().substring(0, f2.getName().length() - FilePersistentQueue.FILE_EXTEND.length());
	        s1 = s1.substring(s1.lastIndexOf("-") + 1);
	        s2 = s2.substring(s2.lastIndexOf("-") + 1);
	        int t1 = Integer.parseInt(s1);
	        int t2 = Integer.parseInt(s2);
	        if (t1 < t2)
	          return -1;
	        if (t1 > t2)
	          return 1;
	        try
	        {
	          throw new Exception("tow file has same time and counter.[" + f1 + "] = [" + f2 + "]");
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return 0;
	      }

	      public boolean equals(Object o)
	      {
	        return o == this;
	      }
	    };
	    Collections.sort(this.files, comparator);
	  }

	  protected void loadMemeryQueue()
	  {
	    File mqFile = new File(this.dir, MQ_FILENAME);
	    if ((mqFile.isFile()) && (mqFile.exists())) {
	      long startTime = System.currentTimeMillis();
	      try {
	        ObjectInputStream ois = getObjectInputStream(mqFile);
	        int count = ois.readInt();
	        Serializable obj = null;
	        for (int i = 0; i < count; i++) {
	          obj = (Serializable)ois.readObject();
	          if (!this.mq.isFull())
	            this.mq.push(obj);
	          else
	            pushIntoFile(obj);
	        }
	        ois.close();
	        if (!mqFile.delete()) {
	          mqFile.deleteOnExit();
	        }
	        logger.info("[load-memery-queue] [OK] [" + mqFile.getAbsolutePath() + "] [" + count + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]");
	      } catch (Exception e) {
	        e.printStackTrace();
	        logger.info("[load-memery-queue] [Error] [" + mqFile.getAbsolutePath() + "] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]", e);
	        if (!mqFile.delete())
	          mqFile.deleteOnExit();
	      }
	    }
	  }

	  protected synchronized void saveMemeryQueue() {
	    if (this.m_thread != null) {
	      this.m_thread.interrupt();
	    }
	    File mqFile = new File(this.dir, MQ_FILENAME);
	    long startTime = System.currentTimeMillis();
	    if (this.mq.size() > 0)
	      try {
	        int count = 0;
	        ObjectOutputStream oos = getObjectOutputStream(mqFile);
	        oos.writeInt(this.mq.size());
	        while (this.mq.size() > 0) {
	          Serializable obj = (Serializable)this.mq.pop();
	          if (obj != null) {
	            oos.writeObject(obj);
	            count++;
	          }
	        }
	        oos.close();
	        logger.info("[save-memery-queue] [OK] [" + mqFile.getAbsolutePath() + "] [" + count + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]");
	      } catch (Exception e) {
	        e.printStackTrace();
	        logger.info("[save-memery-queue] [Error] [" + mqFile.getAbsolutePath() + "] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]", e);
	      }
	  }

	  public synchronized void persistForShutdown()
	  {
	    if (this.m_thread != null) {
	      this.m_thread.interrupt();
	    }
	    File mqFile = new File(this.dir, MQ_FILENAME);
	    long startTime = System.currentTimeMillis();
	    if (this.mq.size() > 0)
	      try {
	        int count = 0;
	        ObjectOutputStream oos = getObjectOutputStream(mqFile);
	        oos.writeInt(this.mq.size());
	        while (this.mq.size() > 0) {
	          Serializable obj = (Serializable)this.mq.pop();
	          if (obj != null) {
	            oos.writeObject(obj);
	            count++;
	          }
	        }
	        oos.close();
	        logger.info("[persist-for-shutdown] [OK] [" + mqFile.getAbsolutePath() + "] [" + count + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]");
	      } catch (Exception e) {
	        e.printStackTrace();
	        logger.info("[persist-for-shutdown] [Error] [" + mqFile.getAbsolutePath() + "] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms]", e);
	      }
	  }

	  protected void loadFiles(File rootDir, List<File> fileList)
	  {
	    File[] fs = rootDir.listFiles(new FileFilter()
	    {
	      public boolean accept(File f) {
	        return (f.isDirectory()) || (f.getName().endsWith(FilePersistentQueue.FILE_EXTEND));
	      }
	    });
	    for (int i = 0; i < fs.length; i++) {
	      if (fs[i].isFile()) {
	        fileList.add(fs[i]);
	        this.totalFileLength += fs[i].length();
	      } else if (fs[i].isDirectory()) {
	        loadFiles(fs[i], fileList);
	      }
	    }

	    if ((fs.length == 0) && (!rootDir.equals(this.dir)) && 
	      (!rootDir.delete()))
	      rootDir.deleteOnExit();
	  }

	  public synchronized boolean push(Serializable object)
	  {
	    long startTime = System.currentTimeMillis();
	    int s = getStorageSize();
	    if (s > 0) {
	      if (s < this.storageCapacity) {
	        File b = pushIntoFile(object);
	        notifyAll();
	        logger.debug("[push] [" + (b == null ? "fail" : "succ") + "] [into-file] [" + (b == null ? "-" : b.getAbsolutePath()) + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + object + "]");
	        return b != null;
	      }

	      logger.debug("[push] [fail] [into-file] [storage_full] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + object + "]");
	      return false;
	    }
	    if (this.mq.isFull()) {
	      File b = pushIntoFile(object);
	      logger.debug("[push] [" + (b == null ? "fail" : "succ") + "] [into-file] [" + (b == null ? "-" : b.getAbsolutePath()) + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + object + "]");
	      return b != null;
	    }
	    this.mq.push(object);
	    if (this.mq.size() == 1) {
	      synchronized (this.lock) {
	        this.lock.notify();
	      }
	    }
	    logger.debug("[push] [succ] [into-memery] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + object + "]");
	    return true;
	  }

	  protected File pushIntoFile(Serializable object)
	  {
	    File file = getFile(object);
	    try {
	      ObjectOutputStream oos = getObjectOutputStream(file);
	      oos.writeObject(object);
	      oos.close();
	      this.files.add(file);
	      this.totalFileLength += file.length();
	      return file;
	    } catch (IOException e) {
	      logger.error("[save-object-into-file] [" + object + "] [" + file + "] [exception]", e);
	      e.printStackTrace();
	    }return null;
	  }

	  protected Serializable popFromFile(File file)
	  {
	    try {
	      ObjectInputStream bis = getObjectInputStream(file);
	      Serializable obj = (Serializable)bis.readObject();
	      bis.close();

	      if (obj != null) {
	        return obj;
	      }
	      return null;
	    }
	    catch (Exception e) {
	      logger.error("[read-object-from-file] [-] [" + file + "] [exception]", e);
	      e.printStackTrace();
	    }return null;
	  }

	  protected void deleteFile(File file)
	  {
	    boolean b = file.delete();
	    if (b)
	      this.totalFileLength -= file.length();
	    else
	      file.deleteOnExit();
	  }

	  protected ObjectOutputStream getObjectOutputStream(File file) throws IOException
	  {
	    return new ObjectOutputStream(new FileOutputStream(file));
	  }

	  protected ObjectInputStream getObjectInputStream(File file) throws IOException {
	    return new ObjectInputStream(new FileInputStream(file));
	  }

	  protected File getFile(Serializable object)
	  {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(System.currentTimeMillis());

	    int y = cal.get(1);
	    int m = cal.get(2) + 1;
	    int d = cal.get(5);
	    int h = cal.get(11);
	    int i = cal.get(12);
	    int s = cal.get(13);
	    int ms = cal.get(14);

	    File dir1 = new File(this.dir, y + "-" + m + "-" + d);
	    if (!dir1.exists()) {
	      dir1.mkdir();
	    }

	    File dir2 = new File(dir1, y + "-" + m + "-" + d + "-" + h + "-" + i);
	    if (!dir2.exists()) {
	      dir2.mkdir();
	    }

	    if (this.lastCounterTime != cal.getTimeInMillis() / 60000L) {
	      this.counter = 0;
	    }
	    this.counter += 1;
	    this.lastCounterTime = (cal.getTimeInMillis() / 60000L);

	    File f = new File(dir2, y + "-" + m + "-" + d + "-" + h + "-" + i + "-" + s + "-" + ms + "-" + this.counter + FILE_EXTEND);
	    return f;
	  }

	  public synchronized Serializable pop() {
	    long startTime = System.currentTimeMillis();
	    Serializable obj = (Serializable)this.mq.pop();
	    if (obj != null) {
	      if (getStorageSize() > 0)
	        notifyAll();
	      logger.debug("[pop] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	      return obj;
	    }
	    if (getStorageSize() > 0) {
	      notifyAll();
	      try {
	        synchronized (this.lock) {
	          this.lock.wait(10L);
	        }
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	      obj = (Serializable)this.mq.pop();

	      if (obj != null) {
	        logger.debug("[pop] [succ] [from-memery] [after-notify] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	        return obj;
	      }
	      logger.debug("[pop] [fail] [from-memery] [after-notify] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	      return null;
	    }

	    logger.debug("[pop] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	    return null;
	  }

	  public synchronized Serializable pop(long timeout)
	  {
	    if (timeout < 0L) timeout = 0L;
	    long startTime = System.currentTimeMillis();
	    long endTime = System.currentTimeMillis() + timeout;

	    Serializable obj = (Serializable)this.mq.pop();
	    if (obj != null) {
	      if (getStorageSize() > 0)
	        notifyAll();
	      logger.debug("[pop-timeout] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	      return obj;
	    }
	    while (true)
	    {
	      long now = System.currentTimeMillis();
	      if ((timeout > 0L) && (now >= endTime)) {
	        logger.debug("[pop-timeout] [succ] [timeout] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	        return null;
	      }
	      if (getStorageSize() > 0) {
	        notifyAll();
	      }
	      try
	      {
	        synchronized (this.lock) {
	          if (timeout == 0L)
	            this.lock.wait();
	          else
	            this.lock.wait(endTime - now);
	        }
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	      obj = (Serializable)this.mq.pop();
	      if (obj != null) {
	        logger.debug("[pop-timeout] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	        return obj;
	      }
	    }
	  }

	  public synchronized Serializable peek()
	  {
	    long startTime = System.currentTimeMillis();
	    Serializable obj = (Serializable)this.mq.peek();
	    if (obj != null) {
	      logger.debug("[peek] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	      return obj;
	    }
	    if (getStorageSize() > 0) {
	      notifyAll();
	      try {
	        synchronized (this.lock) {
	          this.lock.wait(10L);
	        }
	      } catch (Exception e) {
	        e.printStackTrace();
	      }
	      obj = (Serializable)this.mq.peek();
	      if (obj != null) {
	        logger.debug("[peek] [succ] [from-memery] [after-notify] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	        return obj;
	      }
	      logger.debug("[peek] [fail] [from-memery] [after-notify] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	      return null;
	    }

	    logger.debug("[peek] [succ] [from-memery] [direct] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	    return null;
	  }

	  public int size()
	  {
	    return this.mq.size() + getStorageSize();
	  }

	  public int capacity() {
	    return getMemeryCapacity() + getStorageCapacity();
	  }

	  public synchronized void clear() {
	    long startTime = System.currentTimeMillis();
	    int k = this.mq.size();
	    this.mq.clear();
	    int count = 0;
	    while (this.files.size() > 0) {
	      File file = (File)this.files.removeFirst();
	      deleteFile(file);
	      count++;
	    }
	    logger.debug("[clear] [succ] [" + k + "] [" + count + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	  }

	  public int getMemerySize() {
	    return this.mq.size();
	  }

	  public int getMemeryCapacity() {
	    return this.memeryCapacity;
	  }

	  public int getStorageSize() {
	    return this.files.size();
	  }

	  public int getStorageCapacity() {
	    return this.storageCapacity;
	  }

	  public void setMemeryCapacity(int capacity) {
	    if (capacity >= getMemerySize())
	      this.memeryCapacity = capacity;
	    else
	      throw new IllegalArgumentException("capacity less than current memery size");
	  }

	  public void setStorageCapacity(int capacity)
	  {
	    if (capacity >= getStorageSize())
	      this.storageCapacity = capacity;
	    else
	      throw new IllegalArgumentException("capacity less than current storage size");
	  }

	  public boolean isEmpty()
	  {
	    return size() == 0;
	  }

	  public boolean isFull() {
	    return getStorageSize() == getStorageCapacity();
	  }

	  public void run() {
	    long sss = System.currentTimeMillis();
	    while (!Thread.currentThread().isInterrupted()) {
	      try {
	        long startTime = System.currentTimeMillis();
	        if ((getStorageSize() > 0) && (getMemerySize() < getMemeryCapacity())) {
	          synchronized (this) {
	            int num = Math.min(getStorageSize(), getMemeryCapacity() - getMemerySize());
	            if (num > 0) {
	              File file = (File)this.files.get(0);
	              Serializable obj = popFromFile(file);
	              if (Thread.currentThread().isInterrupted())
	                break;
	              if (obj != null) {
	                this.mq.push(obj);
	                synchronized (this.lock) {
	                  this.lock.notify();
	                }
	                logger.debug("[in-queue-thread] [succ] [file-to-memery] [" + file + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [" + obj + "]");
	              } else {
	                logger.debug("[in-queue-thread] [fail] [file-to-memery] [" + file + "] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "ms] [-]");
	              }
	              this.files.removeFirst();
	              deleteFile(file);
	            }
	          }
	        } else {
	          if (Thread.currentThread().isInterrupted())
	            break;
	          synchronized (this) {
	            if (Thread.currentThread().isInterrupted())
	              break;
	            try {
	              logger.debug("[in-queue-thread] [-] [ready-to-sleep] [5s]" + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - startTime) + "] [-]");
	              wait(5000L);
	            } catch (Exception e) {
	            }
	          }
	        }
	      }
	      catch (Throwable e) {
	        logger.error("[in-queue-thread] [catch-exception]", e);
	        e.printStackTrace();
	      }
	    }

	    logger.debug("[in-queue-thread] [exit] [ready-to-exit] [-] " + getQueueStatusForLogger() + " [" + (System.currentTimeMillis() - sss) + "] [-]");
	  }

	  public class MyShutdownHook extends Thread {
	    public MyShutdownHook() {
	    }

	    public void run() {
	      if (FilePersistentQueue.this.m_thread != null)
	    	  FilePersistentQueue.this.m_thread.interrupt();
	      FilePersistentQueue.this.saveMemeryQueue();
	    }
	  }
}
