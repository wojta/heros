package cz.sazel.android.heros_control;

import lombok.Data;

/**
 * Created on 31.7.15.
 */
@Data
public class Id {

	String id;
	String name;
	boolean ip;

	public Id(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public Id(String id, String name, boolean ip) {
		this.id = id;
		this.name = name;
		this.ip = ip;
	}

	@Override
	public String toString() {
		return name;
	}
}
