package edu.assessment;

import distance.MeaningConnection;

public class TestMeaning {

	public static void main(String[] args) {
		MeaningConnection mn = new MeaningConnection();
		String s1 = "data structure";
		String s2 = "input data";
		System.out.println(mn.getRelation(s1, s2));

	}

}
