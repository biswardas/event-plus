package com.biswa.ep;
/**This class generates the identity which is in the order of 2^X. All released
 * token are acquired eagerly before bigger tokens are issued. Ane new ClientToken
 * will issue tokens as 1,2,4,8,16,.... till 2^31. And release of any of the token
 * will be acquired in the next token issuance. After all the tokens exhausted 0 token
 * is issued.
 * 
 * @author biswa
 *
 */
public class ClientToken {
	public static final int ALL_AVAILABLE= Integer.MAX_VALUE; 
	private int available = ALL_AVAILABLE;
	/**Gets the next available token.
	 * 
	 * @return int
	 */
	public int getToken(){
		int current = 0;
		for(int index=0;index<32;index++){
			if((available>>index&1)==1){
				current=1<<index;
				available=available&(Integer.MAX_VALUE^current);
				break;
			}
		}
		return current;
	}
	/**Releases a token to the pool.
	 * 
	 * @param token
	 */
	public void releaseToken(int token){
		available = available | token;
	}
	/**Method primarily used for testing. And any other debugging purpose. 
	 * 
	 * @return int
	 */
	public int getCurrentState(){
		return available;
	}
}
