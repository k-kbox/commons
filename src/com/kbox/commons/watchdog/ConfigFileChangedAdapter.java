package com.kbox.commons.watchdog;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
/**
 * 配置文件修改跟踪适配器
 * 
 * @author myzdf
 *
 */
public class ConfigFileChangedAdapter {

	protected static ConfigFileChangedAdapter m_self = null;
	//synchronized关键字的存在保证了只会产生一个对象，但也成了多线程环境下的性能瓶颈。一个多线程的程序，
	//到了这里却要排队等候成了一个单线程式的执行流程，这在高并发环境下是不可容忍的。
	public static ConfigFileChangedAdapter getInstance(){
		if(m_self != null)
		
			return m_self; 
		synchronized(ConfigFileChangedAdapter.class)
		{
			m_self = new ConfigFileChangedAdapter();
			
			return m_self;
		}
	}
	protected MyFileWatchdog w;
	@SuppressWarnings("rawtypes")
	protected HashMap<String,List> maps = new HashMap<String,List>();
	protected ConfigFileChangedAdapter(){
		w = new MyFileWatchdog();
		w.setDelay(30000L);
		w.setName("File-Change-Adapter-Thread");
		w.start();
	}
	
	/**
	 * 对文件进行监听，当文件发生改变的时候，通知ConfigFileChangedListener。
	 * 
	 * @param file
	 * @param listener
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addListener(File file,ConfigFileChangedListener listener){
		/*
		 * 　
		 * 假定垃圾回收器确定在某一时间点上某个对象是弱可到达对象。这时，它将自动清除针对此对象的所有弱引用，
		 * 以及通过强引用链和软引用，可以从其到达该对象的针对任何其他弱可到达对象的所有弱引用
		 * WeakReference wr = new WeakReference(obj);
　　			if (wr.get()==null) 
			{ 
　　　		　System.out.println("obj 已经被清除了 "); 
　　			} else
 			{
　　				System.out.println("obj 尚未被清除，其信息是 "+obj.toString());
			}
			这类的技巧，在设计 Optimizer 或 Debugger 这类的程序时常会用到，因为这类程序需要取得某对象的信息，
			但是不可以 影响此对象的垃圾收集。
		 */
		WeakReference wr = new WeakReference(listener);
		List l = maps.get(file.getAbsolutePath());
		if(l != null){
			l.add(wr);			
		}else{
			l = new LinkedList();
			maps.put(file.getAbsolutePath(),l);
			l.add(wr);
			w.addFile(file);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public void removeListener(File file,ConfigFileChangedListener listener){
		List l = maps.get(file.getAbsolutePath());
		if(l != null){
			Iterator it = l.iterator();
			while(it.hasNext()){
				WeakReference wr = (WeakReference)it.next();
				if(wr != null && listener.equals(wr.get())){//尚未被清除
					it.remove();
				}
			}
			if(l.size() == 0){
				maps.remove(file.getAbsolutePath());
				
			}
		}
	}
	
	protected class MyFileWatchdog extends FileWatchdog{

		@SuppressWarnings("rawtypes")
		protected void doOnChange(File file) {
			List l = maps.get(file.getAbsolutePath());
			if(l != null){
				Iterator it = l.iterator();
				while(it.hasNext()){
					WeakReference wr = (WeakReference)it.next();
					if(wr != null){
						ConfigFileChangedListener listener = (ConfigFileChangedListener)wr.get();
						try{
							listener.fileChanged(file);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}

