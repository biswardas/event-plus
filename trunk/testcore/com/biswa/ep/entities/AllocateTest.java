package com.biswa.ep.entities;

public class AllocateTest {
	static int primeidentity = 0;
public static void main(String[] args) {
	allocate(0);
	System.out.println(primeidentity);
	allocate(1);
	System.out.println(primeidentity);
	allocate(1);
	System.out.println(primeidentity);
	allocate(2);
	System.out.println(primeidentity);
	allocate(2);
	System.out.println(primeidentity);
	allocate(2);
	System.out.println(primeidentity);
	allocate(3);
	System.out.println(primeidentity);
	allocate(1);
	System.out.println(primeidentity);
	allocate(1);
	System.out.println(primeidentity);
}

static private void allocate(int numOfClients) {
	if(numOfClients>0){
		if(primeidentity<numOfClients){
			primeidentity = primeidentity+1;			
		}else{
			primeidentity=1;
		}
	}else{
		primeidentity = 0;
	}
}	
}
