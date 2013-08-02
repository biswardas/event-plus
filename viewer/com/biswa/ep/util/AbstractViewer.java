package com.biswa.ep.util;

import java.util.Properties;
import java.util.WeakHashMap;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.biswa.ep.entities.ContainerStructureEvent;
import com.biswa.ep.entities.LightWeightEntry;
import com.biswa.ep.entities.Predicate;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.spec.Spec;
import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.provider.CompiledAttributeProvider;
import com.biswa.ep.provider.PredicateBuilder;
import com.biswa.ep.provider.ScriptEngineAttributeProvider;
public abstract class AbstractViewer extends ConcreteContainer implements UIOperations {
        private Agent sourceAgent;
        public AbstractViewer(final String name,final String threadModel) {
                super(name,new Properties(){
                        private static final long serialVersionUID = 1L;

                        {
                                put("concurrent",threadModel);
                        }
                });
                ContainerContext.initialize(AbstractViewer.this);
        }

        @Override
        final public void connected(ConnectionEvent connectionEvent) {
                super.connected(connectionEvent);
                postConnected(connectionEvent);
        }

        protected abstract void postConnected(ConnectionEvent connectionEvent);

        @Override
        final public void disconnected(ConnectionEvent connectionEvent) {
                postDisconnected(connectionEvent);
        }
        
        protected abstract void postDisconnected(ConnectionEvent connectionEvent);

        @Override
        final public void attributeAdded(final ContainerEvent ce) {
                super.attributeAdded(ce);
                postAttributeAdded(ce);
        }

        protected abstract void postAttributeAdded(ContainerEvent ce);

        @Override
        final public void attributeRemoved(final ContainerEvent ce) {
                super.attributeRemoved(ce);
                postAttributeRemoved(ce);
        }
        protected abstract void postAttributeRemoved(ContainerEvent ce);
        
        @Override
        final public void commitTran(){
                preCommitTran();
                super.commitTran();
        }

        protected abstract void preCommitTran();

        final public void setSourceAgent(Agent sourceAgent) {
                this.sourceAgent=sourceAgent;
        }
        
        @Override
        public int getSortedEntryCount() {
                return sourceAgent.getEntryCount(getName(), 2);
        }
        
        @Override
        public LightWeightEntry getLightWeightEntry(int id) {
                return sourceAgent.getSortedEntry(getName(), id, 2);
        }
        
        @Override
        public void applySpecInSource(Spec spec) {
                sourceAgent.applySpec(spec);            
        }

        @Override
        public void addCompiledAttributeToSource(String data) {
                com.biswa.ep.entities.Attribute schemaAttribute = new CompiledAttributeProvider().getAttribute(data,sourceAgent.getTypeMap());
                ContainerEvent ce = new ContainerStructureEvent(getName(),schemaAttribute);
                sourceAgent.attributeRemoved(ce);               
                sourceAgent.attributeAdded(ce);
        }
        @Override
        public void addScriptAttributeToSource(String data) {
                com.biswa.ep.entities.Attribute schemaAttribute = new ScriptEngineAttributeProvider().getAttribute(data,sourceAgent.getTypeMap());
                ContainerEvent ce = new ContainerStructureEvent(getName(),schemaAttribute);
                sourceAgent.attributeRemoved(ce);
                sourceAgent.attributeAdded(ce);
        }
        @Override
        public void removeEntryFromSource(String data) {
                sourceAgent.entryRemoved(new ContainerDeleteEvent(getName(), Integer.parseInt(data), 0));
        }
        @Override
        public String[] getAttributes(){
                return sourceAgent.getAttributes();
        }
        final private WeakHashMap<Integer, LightWeightEntry> cachedEntries = new WeakHashMap<Integer, LightWeightEntry>() {
                public LightWeightEntry get(Object key) {
                        LightWeightEntry tEntry = super.get(key);
                                if (tEntry == null) {
                                        // System.out.println("Requesting record:"+key);
                                        tEntry = getLightWeightEntry((Integer)key);
                                        put((Integer) key, tEntry);
                                }
                        return tEntry;
                }
        };
        @Override
        public void disConnectFromSource() {
                sourceAgent.disconnect(new ConnectionEvent(sourceAgent.getName(), getName()));
        }

        final public LightWeightEntry getCachedEntries(int index) {
                return cachedEntries.get(index);
        }
        
        final public void clearCachedEntries() {
                cachedEntries.clear();
        }


        public void applyFilter(String filter) {
                if(filter.isEmpty()){
                        filter=String.valueOf(Boolean.TRUE);
                }
                Predicate pred = PredicateBuilder.buildPredicate(filter);
                applySpecInSource(new FilterSpec(getName(),pred));
        }
}