package com.sk.collect.monitor.vo;

import java.util.HashMap;
import java.util.Map;

public class Node {
	private String nodeId;
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

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
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