package com.biswa.ep.jdbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexTest {
	@Test
	public void testUrlPattern(){
		Pattern pattern = Pattern
				.compile("jdbc:ep:rmi://(.*?)\\[([0-9]*?)\\]");
		Matcher matcher = pattern.matcher("jdbc:ep:rmi://localhost[1099]");
		if(matcher.matches()){
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		}
		
	}
	@Test
	public void testPattern() {

		Pattern pattern = Pattern
				.compile("(?i)call\\s*(.*?)\\.((insert|update|delete)+)\\s*\\(\\s*\\?\\s*\\)\\s*");
		insert(pattern);
		System.out.println("----------------------------------");
		update(pattern);
		System.out.println("----------------------------------");
		delete(pattern);

	}

	protected static void insert(Pattern pattern) {
		Matcher matcher = pattern.matcher("call A.B.insert (	? ) ");
		dump(matcher);
	}

	protected static void update(Pattern pattern) {
		Matcher matcher = pattern.matcher("CALL A.B.dELETE ( ?   )");
		dump(matcher);
	}

	protected static void delete(Pattern pattern) {
		Matcher matcher = pattern.matcher("cALL A.B.update ( ?   ) ");
		dump(matcher);
	}

	protected static void dump(Matcher matcher) {
		System.out.println(matcher.matches());
		for (int i = 0; i < matcher.groupCount(); i++) {
			System.out.print(i);
			System.out.print("=");
			System.out.println(matcher.group(i));
		}
	}
}
