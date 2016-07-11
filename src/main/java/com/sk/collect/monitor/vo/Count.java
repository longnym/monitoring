package com.sk.collect.monitor.vo;

public class Count {
	private String dt;
	private int svc;
	private String host;
	private String typ;
	private String agnt;
	private String src;
	private int cyc;
	private long cnt;
	private int hb;

	public String getDt() {
		if (dt == null) {
			return "";
		}
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

	public int getSvc() {
		return svc;
	}

	public void setSvc(int svc) {
		this.svc = svc;
	}

	public String getHost() {
		if (host == null) {
			return "";
		}
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getTyp() {
		if (typ == null) {
			return "";
		}
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getAgnt() {
		if (agnt == null) {
			return "none";
		}
		return agnt;
	}

	public void setAgnt(String agnt) {
		this.agnt = agnt;
	}

	public String getSrc() {
		if (src == null) {
			return "";
		}

		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public int getCyc() {
		return cyc;
	}

	public void setCyc(int cyc) {
		this.cyc = cyc;
	}

	public long getCnt() {
		return cnt;
	}

	public void setCnt(long cnt) {
		this.cnt = cnt;
	}

	public int getHb() {
		return hb;
	}

	public void setHb(int hb) {
		this.hb = hb;
	}
}