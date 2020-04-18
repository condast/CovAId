package org.covaid.core.data;

public class OptionsData {
		
	String name;
	String tk;
	int o;
	int mt;
	int ma;
	
	public OptionsData() {
		super();
	}

	public OptionsData(String name, String token, int options) {
		super();
		this.name = name;
		this.o = options;
		this.mt = 0;
		this.ma = 0;
	}

	public String getName() {
		return name;
	}

	public int getOptions() {
		return o;
	}

	public int getMaxThrust() {
		return mt;
	}

	public void setMaxThrust(int mt) {
		this.mt = mt;
	}

	public int getMaxAngle() {
		return ma;
	}

	public void setMaxAngle(int ma) {
		this.ma = ma;
	}
	
	
}
