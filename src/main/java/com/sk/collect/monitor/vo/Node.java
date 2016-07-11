package com.sk.collect.monitor.vo;

import java.util.HashMap;
import java.util.Map;

public class Node {
	private String nodeSeq;
	private String nodeNm;
	private String nodeType;
	private Map<String, String> property;

	public Node() {
		property = new HashMap<String, String>();
	}

	public void addProperty(String key, String value) {
		property.put(key, value);
	}

	public String getProperty(String key) {
		return property.get(key);
	}

	public String getNodeSeq() {
		return nodeSeq;
	}

	public void setNodeSeq(String nodeSeq) {
		this.nodeSeq = nodeSeq;
	}

	public String getNodeNm() {
		return nodeNm;
	}

	public void setNodeNm(String nodeNm) {
		this.nodeNm = nodeNm;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
}