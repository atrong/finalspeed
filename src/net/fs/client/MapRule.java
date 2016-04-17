// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.io.Serializable;
import java.net.ServerSocket;

public class MapRule implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3504577683070928480L;

	int listen_port;
	
	int dst_port;
		
	String name;
	
	boolean using=false;
	
	ServerSocket serverSocket;

	public int getListen_port() {
		return listen_port;
	}

}
