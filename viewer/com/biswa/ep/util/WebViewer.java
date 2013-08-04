package com.biswa.ep.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConnectionEvent;
import com.biswa.ep.entities.ContainerEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebViewer extends AbstractViewer {
        class ContentHandler implements HttpHandler {
                //HEHEH i know its not thread safe
                private String filter = "true";
                public void handle(HttpExchange t) throws IOException {
                        InputStream is = t.getRequestBody();
                        if(connected){
                                Map<String,String> params = new HashMap<String,String>();
                                URI uri = t.getRequestURI();
                                StringTokenizer queryTokenizer = new StringTokenizer(uri.getQuery(),"&");
                                while(queryTokenizer.hasMoreTokens()){
                                        String oneParam = queryTokenizer.nextToken();
                                        int index = oneParam.indexOf('=');
                                        if(index!=-1){
                                                params.put(oneParam.substring(0, index), oneParam.substring(index+1));
                                        }else{
                                                params.put(oneParam, null);
                                        }
                                }
                                synchronized(this){//Needs ton of improvement
                                        String query = params.get("query");
                                        if(query!=null && !filter.equals(query)){
                                                filter=query;
                                                applyFilter(filter);
                                                System.out.println(filter);
                                        }else if(query==null && !filter.equals("true")){
                                                filter = "true";
                                                applyFilter(filter);
                                                System.out.println(filter);
                                        }
                                }
                                int maxEntry = getSortedEntryCount();
                                int startRecord = Integer.parseInt(params.get("start"));
                                if(startRecord<0) startRecord=0;
                                if(startRecord>maxEntry) startRecord=maxEntry-1;
                                int requestedRecordCount = Integer.parseInt(params.get("limit"));
                                requestedRecordCount=(startRecord+requestedRecordCount)>maxEntry?(maxEntry-startRecord):requestedRecordCount;
                                
                                
                                StringBuilder response = new StringBuilder();
                                response.append("{\"start\":").append(startRecord).append(",\n\r");
                                response.append("\"maxEntry\":").append(maxEntry).append(",\n\r");
                                //response.append("\"requestedRecordCount\":").append(requestedRecordCount).append(",\n\r");
                                response.append("\"records\": [\n\r");
                                Object[] columns = getAttributes();
                                for(int index=0;index<requestedRecordCount;index++){
                                		if(index>0){
                                            response.append(",\n\r");
                                		}
                                        response.append("{");
                                        int recordIndex = startRecord+index;
                                        Object[] substances = getLightWeightEntry(recordIndex).getSubstances();
                                        response.append("\"id\":").append(recordIndex);
                                        for(int inner=0;inner<columns.length;inner++){
                                        		response.append(",\n\r");
                                                response.append("\"").append(columns[inner]).append("\":");
                                                Object substance = substances[inner];
                                                if(substance!=null && substance.getClass().isArray()){
                                                    if(substance.getClass().getComponentType().isPrimitive()){
                                                            try {
                                                                    Method m =Arrays.class.getMethod("toString",substance.getClass());
                                                                    substance = m.invoke(Arrays.class, substance);
                                                            } catch (Exception e) {
                                                            }
                                                        	response.append(substance);
                                                    }else if(Number.class.isAssignableFrom(substance.getClass().getComponentType())){
                                                    	substance = Arrays.toString((Object[])substance);
                                                    	response.append(substance);
                                                    }else{ 
                                                    	substance = Arrays.toString((Object[])substance);
                                                    	response.append("\"").append(substance).append("\"");
                                                    }
                                                }else{
                                                	response.append("\"").append(substance==null?"":substance).append("\"");
                                                }
                                        }
                                        response.append("}");
                                }
                                response.append("]}");
                                t.getResponseHeaders().add("Content-Type", "application/json");
                                t.sendResponseHeaders(200, response.length());
                                OutputStream os = t.getResponseBody();
                                os.write(response.toString().getBytes());
                                os.close();
                        }else{
                                String response = "Server Not Yet Connected";
                                t.sendResponseHeaders(201, response.length());
                                OutputStream os = t.getResponseBody();
                                os.write(response.getBytes());
                                os.close();
                        }
                }
        }

        public WebViewer(String name) {
                super(name, "0");
                try {
                        HttpServer server = HttpServer.create(new InetSocketAddress(8000),
                                        0);
                        server.createContext("/applications/myapp", new ContentHandler());
                        server.setExecutor(null); // creates a default executor
                        server.start();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
        
        private boolean connected = false;
        
        @Override
        public void postConnected(ConnectionEvent connectionEvent) {
                connected = true ;
        }

        @Override
        public void postDisconnected(ConnectionEvent connectionEvent) {
                System.out.println("Received PostConnected" + connectionEvent);
        }

        @Override
        public void postAttributeAdded(final ContainerEvent ce) {
                System.out.println("Received postAttributeAdded" + ce);
        }

        @Override
        public void postAttributeRemoved(final ContainerEvent ce) {
                System.out.println("Received postAttributeRemoved" + ce);
        }

        @Override
        public void preCommitTran() {
                //System.out.println("Received preCommit");
        }

        public static int launchViewer() {
                final AbstractContainer cs = ContainerContext.CONTAINER.get();
                final String name = cs.getName() + "-Viewer";
                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                WebViewer viewer = new WebViewer(name) {
                                        @Override
                                        public void disconnect(ConnectionEvent connectionEvent) {
                                                cs.agent().disconnect(
                                                                new ConnectionEvent(cs.getName(), name));
                                        }
                                };
                                viewer.setSourceAgent(cs.agent());
                                cs.agent()
                                                .connect(
                                                                new ConnectionEvent(cs.getName(), name, viewer
                                                                                .agent()));
                        }
                }, "WebAccessThread").start();
                return 0;
        }
}