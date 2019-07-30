package edu.assessment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import graph.ContentGraph;
import graph.ContentJSONGraph;
import graph.IGraph;
import parser.IParser;
import parser.ParserV4;
import edu.stanford.nlp.ling.TaggedWord;
import webservice.StatisticalUtils;

public class TestGraph {

	public static void main(String[] args) throws Exception {
		// test1();
		// test2();
		test3();
	}

	private static void test3() throws Exception {
		// String phraseFile = "tmp/Phrase_File.docx";
		// String stopwordFile = "tmp/stopword.txt";
		// IParser p = new ParserV4(stopwordFile, phraseFile);
		// Map<String, Object> ret = p.processFile("tmp/1.txt", "a", "", "a", "a");
		// Map<String, Object> tmp = (Map<String, Object>) ret.get("metaInfo");
		//
		// JSONObject json = new JSONObject();
		// json.putAll(tmp);
		// System.out.printf("JSON: %s", json.toJSONString());
		//
		// Map<String, Object> meta_data = (Map<String, Object>) ret.get("metaInfo");
		// Map<String, Integer> tmp5 = (Map<String, Integer>)
		// meta_data.get("sentenceDegreeDistribution");
		//
		// int[] o = StatisticalUtils.histogram(tmp5);
		// JSONArray tmpx = new JSONArray();
		// for (int k : o)
		// tmpx.add(k);
		// // tmpx.add("2");
		// // tmpx.addAll(Arrays.asList(o));
		//
		// JSONObject json1 = new JSONObject(meta_data);
		// json1.put("ttt", tmpx);
		// System.out.println(json1.toJSONString());
	}

	private static void test2() throws Exception {

		// String phraseFile = "tmp/Phrase_File.docx";
		// String stopwordFile = "tmp/stopword.txt";
		// IParser p = new ParserV4(stopwordFile, phraseFile);
		// Map<String, Object> ret = p.processFile("tmp/1.txt", "a", "", "a", "a");
		// List<Object[]> gr = (List<Object[]>) ret.get("connection");
		// gr.remove(0);

		// IGraph graph = new ContentJSONGraph(gr, (Map<String, List<TaggedWord>>)
		// ret.get("sentencesMap"));
		// String st = graph.getJsonVisjsFormat();
		// System.out.println(st);
	}

	private static void test1() throws Exception {
		// String phraseFile = "tmp/Phrase_File.docx";
		// String stopwordFile = "tmp/stopword.txt";
		// IParser p = new ParserV4(stopwordFile, phraseFile);
		// Map<String, Object> ret = p.processFile("tmp/1.txt", "a", "", "a", "a");
		// List<Object[]> gr = (List<Object[]>) ret.get("connection");
		// gr.remove(0);
		//
		// IGraph graph = new ContentGraph(gr, (Map<String, List<TaggedWord>>)
		// ret.get("sentencesMap"));
		// String st = graph.getJsonVisjsFormat();
		// System.out.println(st);
	}

}
