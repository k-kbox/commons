/**
 * 
 */
package com.kbox.commons.telephony;

/**
 * @author jun.huang
 * 
 */
public class MobileRegion implements Comparable<MobileRegion> {

	protected String min_region;

	protected String max_region;

	protected String province;

	protected String city;

	protected String type;

	public MobileRegion(String p, String c, String min, String max, String t) {
		province = p;
		city = c;
		min_region = min;
		max_region = max;
		type = t;
	}

	@Override
	public int compareTo(MobileRegion o) {
		if (max_region.compareTo(o.min_region) < 0)
			return -1;
		if (o.max_region.compareTo(min_region) < 0)
			return 1;
		return 0;
	}

	public boolean equals(Object o) {
		if (o instanceof MobileRegion) {
			if (compareTo((MobileRegion) o) == 0)
				return true;
		}
		return false;
	}

	public String getMax_region() {
		return max_region;
	}

	public String getMin_region() {
		return min_region;
	}

	public String toString() {
		return province + " " + city + " " + type + " " + min_region + " "
				+ max_region;
	}

	/**
	 * 省份，比如北京，上海，江苏，浙江等。
	 * 
	 * @return
	 */
	public String getProvince() {
		return province;
	}

	/**
	 * 城市或者地区，比如北京，上海，杭州，天津，广州等
	 * 
	 * @return
	 */
	public String getCity() {
		return city;
	}

	/**
	 * 手机的型号，比如移动全球通，移动神州行，联通如意通，联通CMDA等。
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

}
