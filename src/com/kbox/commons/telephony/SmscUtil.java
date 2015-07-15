/**
 * 
 */
package com.kbox.commons.telephony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.kbox.commons.watchdog.ConfigFileChangedAdapter;
import com.kbox.commons.watchdog.ConfigFileChangedListener;


/**
 * @author jun.huang
 *
 */
public class SmscUtil implements ConfigFileChangedListener {
	
	private static SmscUtil _instance;
	
	public synchronized static SmscUtil getInstance()
	{
		if (_instance == null)
			_instance = new SmscUtil();
		return _instance;
	}

	private Map<String, SmscUtil.Smsc> map;
	protected SmscUtil()
	{
		File file = new File(SmscUtil.class.getResource("/").getFile() + "smsc_file.txt");
		this.load(file);
		ConfigFileChangedAdapter adapter = ConfigFileChangedAdapter.getInstance();
		adapter.addListener(file, this);
	}
	
	public void load(File file)
	{
		long time = System.currentTimeMillis();

    	BufferedReader reader = null;
        try {
        	
        	FileInputStream input = new FileInputStream(file);
        	
            reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            Map<String, Smsc> m = new HashMap<String, SmscUtil.Smsc>();
            String l = null;
            int c = 0;
            while ((l = reader.readLine()) != null) {
                c++;
                l = l.trim();
                if (l.length() == 0 || l.charAt(0) == '#')
                    continue;
                String ss[] = l.split(",");
                try {
                	String smsc = ss[3].trim();
                    String province = ss[0].trim();
                    String city = ss[1].trim();
                    String carrier = ss[2].trim();
                    m.put(smsc, new Smsc(smsc, province, city, carrier));
                } catch (Exception e) {
                    // e.printStackTrace();
                    System.err.println("[" + getClass() + "] [ParserError] [Line:"
                            + c + "] [" + l + "] [" + e + "]");
                }
            }
            map = m;
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

    /**
     * 根据手机号码获得号码地区信息，如果无法知道地区信息，返回null.
     * 
     * @param mobile
     * @return
     */
    public Smsc findSmsc(String smsc) {
        return map.get(smsc.trim());
    }

	@Override
	public void fileChanged(File file) {
		if (file != null)
			this.load(file);
	}
	
	public class Smsc 
	{
		private String smsc;
		private String province;
		private String city;
		private String carrier;
		
		public Smsc(String smsc, String province, String city, String carrier) {
			super();
			this.smsc = smsc;
			this.province = province;
			this.city = city;
			this.carrier = carrier;
		}
		/**
		 * @return the smsc
		 */
		public String getSmsc() {
			return smsc;
		}
		/**
		 * @param smsc the smsc to set
		 */
		public void setSmsc(String smsc) {
			this.smsc = smsc;
		}
		/**
		 * @return the province
		 */
		public String getProvince() {
			return province;
		}
		/**
		 * @param province the province to set
		 */
		public void setProvince(String province) {
			this.province = province;
		}
		/**
		 * @return the city
		 */
		public String getCity() {
			return city;
		}
		/**
		 * @param city the city to set
		 */
		public void setCity(String city) {
			this.city = city;
		}
		/**
		 * @return the carrier
		 */
		public String getCarrier() {
			return carrier;
		}
		/**
		 * @param carrier the carrier to set
		 */
		public void setCarrier(String carrier) {
			this.carrier = carrier;
		}
		
	}

}
