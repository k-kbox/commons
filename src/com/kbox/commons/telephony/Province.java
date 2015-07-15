/**
 * 
 */
package com.kbox.commons.telephony;

/**
 * @author jun.huang
 *
 */
public class Province 
{
	private String id;
	private String code;
	private String name;
	
	public Province(String id, String code, String name) {
		super();
		this.id = id;
		this.code = code;
		this.name = name;
	}
	
	public Province(Province o) {
		this.id = o.id;
		this.code = o.code;
		this.name = o.name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the mame
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param mame the mame to set
	 */
	public void setName(String name) {
		this.name = name;
	}
		
}
