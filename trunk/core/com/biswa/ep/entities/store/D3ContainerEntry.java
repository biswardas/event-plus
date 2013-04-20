package com.biswa.ep.entities.store;

import java.util.HashMap;

import com.biswa.ep.entities.Attribute;
/**3 dimensional container entry where each column can contain multiple values 
 * Once an attribute is detected as multi value then the substance is wrapped
 * inside a multi value substance and added the the container entry. Any subsequent
 * value will be maintained in the map against the minor attribute.<br>
 * 
 * 1. if a non multi value attribute already exists then the substance is lost and can not
 * be recovered.<br>
 * 2. If the last multi value is removed then the entire multi value substance will be removed.<br>
 * 3. If a non multi value attribute arrives then the multi value is removed and the ordinary substance.<br>
 * 4. if a multi value arrives with same minor value as previous then it replaces the previous multi value.<br>
 * @author biswa
 *
 */
class D3ContainerEntry extends ConcreteContainerEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6533598208126483052L;

	/**Constructor to create a D3 container entry
	 * 
	 * @param externalidentity int
	 * @param concrete boolean
	 */
	public D3ContainerEntry(int externalidentity) {
		super(externalidentity);
	}

	@Override
	public Object silentUpdate(Attribute attribute, Object substance,int minor) {
			HashMap<Integer,Object> multiSubstance = (HashMap<Integer, Object>) getSubstance(attribute);
			if(multiSubstance==null){
				multiSubstance = new HashMap<Integer,Object>();
			}
			multiSubstance.put(minor,substance);
			return super.silentUpdate(attribute, multiSubstance);
	}

	@Override
	public void remove(Attribute attribute,int minor) {
		HashMap<Integer,Object> multiSubstance = (HashMap<Integer, Object>) getSubstance(attribute);
		if(multiSubstance==null){
			return;
		}else{
			multiSubstance.remove(minor);
			if(multiSubstance.isEmpty()){
				super.remove(attribute);
			}
			return;
		}
	}
}
