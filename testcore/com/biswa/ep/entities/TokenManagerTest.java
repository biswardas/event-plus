package com.biswa.ep.entities;

import java.util.ArrayList;

public class TokenManagerTest {
public static void main(String[] args) {
	final ArrayList<Integer> clientTokens = new ArrayList<Integer>(){		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			for(int i=0;i<32;i++){
				this.add(1<<i);
			}
		}
	};
	for(int i:clientTokens){
		System.out.println(i);
	}
	System.out.format("0x%x",0xFFFF^2);
}


}
