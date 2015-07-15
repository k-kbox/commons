/**
 * 
 */
package com.kbox.commons.telephony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.kbox.commons.watchdog.ConfigFileChangedAdapter;
import com.kbox.commons.watchdog.ConfigFileChangedListener;


/**
 * @author jun.huang
 *
 */
public class MobileRegionUtil implements ConfigFileChangedListener {
	
	private static MobileRegionUtil _instance;
	
	public synchronized static MobileRegionUtil getInstance()
	{
		if (_instance == null)
			_instance = new MobileRegionUtil();
		return _instance;
	}

	List<MobileRegion> regions;
	protected MobileRegionUtil()
	{
		File file = new File(MobileRegionUtil.class.getResource("/").getFile() + "mobile_region_file.txt");
		load(file);
		ConfigFileChangedAdapter adapter = ConfigFileChangedAdapter.getInstance();
		adapter.addListener(file, this);
	}
			
	public void load(File file)
	{
		long time = System.currentTimeMillis();

    	BufferedReader reader = null;
        try {
        	
        	FileInputStream input = new FileInputStream(file);
        	
            reader = new BufferedReader(new InputStreamReader(input, "GBK"));

            ArrayList<MobileRegion> list = new java.util.ArrayList<MobileRegion>();
            String l = null;
            int c = 0;
            while ((l = reader.readLine()) != null) {
                c++;
                l = l.trim();
                if (l.length() == 0 || l.charAt(0) == '#')
                    continue;
                String ss[] = l.split(",");
                try {
                    String province = ss[2].trim();
                    String city = ss[3].trim();
                    String startNum = ss[0].trim();
                    String endNum = ss[1].trim();
                    String mobileType = ss[5].trim();
                    // String opId = ss[4].trim();
    
                    MobileRegion ri = new MobileRegion(province, city, startNum,
                            endNum, mobileType);
                    list.add(ri);
                } catch (Exception e) {
                    // e.printStackTrace();
                    System.err.println("[" + getClass() + "] [ParserError] [Line:"
                            + c + "] [" + l + "] [" + e + "]");
                }
            }
    
            regions = list;
            java.util.Collections.sort(regions);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
		time = System.currentTimeMillis() - time;
		System.out.println(this.getClass().getName()+" Init cost:" + time + "ms");
	}
	
	public void load()
	{
		
	}

    /**
     * 根据手机号码获得号码地区信息，如果无法知道地区信息，返回null.
     * 
     * @param mobile
     * @return
     */
    public MobileRegion findRegion(String mobile) {
        String l = mobile.trim();

        MobileRegion ri = new MobileRegion(null, null, l, l, "");
        int k = java.util.Collections.binarySearch(regions, ri);
        if (k >= 0) {
            return regions.get(k);
        }
        return null;
    }

	@Override
	public void fileChanged(File file) {
		if (file != null)
			load(file);
	}
}
