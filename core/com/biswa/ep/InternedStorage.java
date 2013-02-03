package com.biswa.ep;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.biswa.ep.entities.substance.Substance;


public class InternedStorage {
	private HashMap<Substance,InternedStorage.InternWeakReference<?extends Substance>> storage = new HashMap<Substance,InternedStorage.InternWeakReference<? extends Substance>>();
	private ReferenceQueue<Substance> refQueue = new ReferenceQueue<Substance>();

	@SuppressWarnings("unchecked")
	public void put(Substance subject, Substance actualSubstance) {
		InternWeakReference<? extends Substance> ref = null;
		while ((ref = (InternedStorage.InternWeakReference<Substance>) refQueue.poll()) != null) {
			System.out.println("Collecting garbage...."+ref.getKey());
			storage.remove(ref.getKey());
		}
		storage.put(subject,new InternWeakReference<Substance>(actualSubstance,subject));
	}
	
	public Substance get(Substance subject){
		return storage.get(subject).get();
	}
	
	class InternWeakReference<T1 extends Substance> extends WeakReference<T1> {
		private Substance key;
		public InternWeakReference(T1 referent,Substance key) {
			super(referent,refQueue);
			this.key=key;
		}
		public Substance getKey() {
			return key;
		}
	}
}