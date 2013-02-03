package com.biswa.ep.entities.store;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.substance.MultiSubstance;
import com.biswa.ep.entities.substance.Substance;
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
	public Substance silentUpdate(Attribute attribute, Substance substance,int minor) {
			MultiSubstance multiSubstance = null;
			Substance existingSubstance = getSubstance(attribute);
			if(existingSubstance==null || !existingSubstance.isMultiValue()){
				multiSubstance = new MultiSubstance();
			}else{
				multiSubstance = (MultiSubstance)existingSubstance;
			}
			multiSubstance.addValue(minor,substance);
			return super.silentUpdate(attribute, multiSubstance);
	}

	@Override
	public void remove(Attribute attribute,int minor) {
		Substance existingSubstance = getSubstance(attribute);
		if(existingSubstance==null){
			return;
		}else{
			MultiSubstance multiSubstance = (MultiSubstance)existingSubstance;
			multiSubstance.removeValue(minor);
			if(multiSubstance.isEmpty()){
				super.remove(attribute);
			}
			return;
		}
	}
}
