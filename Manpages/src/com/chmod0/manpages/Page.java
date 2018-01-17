package com.chmod0.manpages;

import java.io.Serializable;

public class Page implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
	private int section;
	
	public Page(String name, int section) {
		super();
		this.name = name;
		this.section = section;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSection() {
		return this.section;
	}

	public void setSection(int section) {
		this.section = section;
	}
	
	@Override
	public String toString(){
		return this.name + " (" + this.section + ")";
	}

}
