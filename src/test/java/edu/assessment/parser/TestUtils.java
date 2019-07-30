package edu.assessment.parser;

import parser.Utils;

public class TestUtils {
	public static void main(String[] args) {
		testGetTag();

	}

	private static void testGetTag() {
		String text = "I have a dog";
		System.out.println(Utils.getTag(text));

	}
}
