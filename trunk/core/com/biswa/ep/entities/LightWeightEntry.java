

package com.biswa.ep.entities;

import java.io.Serializable;

public class LightWeightEntry implements Serializable{
        /**
         * 
         */
        private static final long serialVersionUID = 1450527112657389711L;
        public final int id;
        private final Object[] substances;
        public Object[] getSubstances() {
                return substances;
        }
        public LightWeightEntry(int id,Object[] substances){
                this.id=id;
                this.substances=substances;
        }
        public Object substance(int index) {
                return substances.length>index?substances[index]:null;
        }
}

