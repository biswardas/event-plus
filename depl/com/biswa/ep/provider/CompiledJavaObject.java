package com.biswa.ep.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.biswa.ep.entities.Attribute;

public class CompiledJavaObject extends SimpleJavaFileObject {
	private String expression;
	private String name = null;
	private Attribute attribute = null; 
	private final Map<String, Class<? extends Object>> typeMap; 
	protected CompiledJavaObject(String expression, Map<String, Class<? extends Object>> typeMap) {
		super(URI.create("string:///com/biswa/ep/provider/CompiledAttribute"+expression.substring(0, expression.indexOf("="))+".java"), Kind.SOURCE);
		this.expression = expression;
		this.name = expression.substring(0, expression.indexOf("="));
		this.typeMap=typeMap;
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		JavaFileManager fileManager = new ClassFileManager(jc.getStandardFileManager(null, null, null));
		List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
		jfiles.add(this);
		jc.getTask(null, fileManager, null, null, null, jfiles).call();
		try {
			attribute = (Attribute) fileManager.getClassLoader(null).loadClass("com.biswa.ep.provider.CompiledAttribute"+name).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		EPJavaObject epJavaObject = new EPJavaObject(expression);
		StringBuilder sb = new StringBuilder();
		sb.append("package com.biswa.ep.provider;\n");
		sb.append("import com.biswa.ep.entities.Attribute;\n");
		sb.append("import com.biswa.ep.entities.LeafAttribute;\n");
		sb.append("import com.biswa.ep.entities.ContainerEntry;\n");
		sb.append("public class CompiledAttribute").append(name);
		sb.append(" extends Attribute{");
		sb.append("{\n");
		for(String oneVariable:epJavaObject.getVariables()){
			if(typeMap.containsKey(oneVariable)){
				sb.append("addDependency(new LeafAttribute(\""+oneVariable+"\"));\n");
			}
		}
		sb.append("}\n");

		sb.append("public CompiledAttribute"+name+"(){\n");
			sb.append("super(\""+name+"\");\n");
		sb.append("}\n");
		sb.append("public Object evaluate(Attribute attribute,ContainerEntry ce){\n");
			for(String oneVariable:epJavaObject.getVariables()){
				if(typeMap.containsKey(oneVariable)){
					sb.append("Object "+oneVariable+" = super.getValue(ce,\""+oneVariable+"\");\n");
				}
			}
			sb.append("return "+expression.substring(expression.indexOf("=") + 1)+";\n");
		sb.append("}\n");
		sb.append("}\n");
		return sb.toString();
	}

	public Attribute getCompiledAttribute() {
		return attribute;
	}
	
	class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager>{
		private EPCompiledJavaObject javaClassObject;
		public ClassFileManager(StandardJavaFileManager standardManager){
			super(standardManager);
		}
		@Override
		public ClassLoader getClassLoader(Location location){
			return new SecureClassLoader(){
				@Override
				protected Class<?> findClass(String name)
						throws ClassNotFoundException {
					byte[] b =javaClassObject.getBytes();
					return super.defineClass(name, b, 0, b.length);
				}				
			};
		}
		@Override
		public JavaFileObject getJavaFileForOutput(Location location,String className,Kind kind,FileObject sibling){
			javaClassObject = new EPCompiledJavaObject(className, kind);
			return javaClassObject;
		}
	}
	class EPCompiledJavaObject extends SimpleJavaFileObject{
		protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		public EPCompiledJavaObject(String name,Kind kind){
			super(URI.create("string:///"+name.replace('.', '/')+kind.extension),kind);
		}
		public byte[] getBytes(){
			return bos.toByteArray();
		}
		@Override
		public OutputStream openOutputStream(){
			return bos;
		}
	}

	public static void main(String[] args) {
		CompiledJavaObject jfo = new CompiledJavaObject("x=(double)a+(double)b",new HashMap<String,Class<? extends Object>>());
		System.out.println(jfo.getCompiledAttribute());
	}
}