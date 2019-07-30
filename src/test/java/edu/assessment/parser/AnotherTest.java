//package edu.assessment.parser;
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//import Coreference.CoreNlpStandford;
//import Coreference.ResultCoreference;
//import Sentiment.ResultObjectSentiment;
//import Sentiment.ResultSentiment;
//import edu.stanford.nlp.pipeline.Annotation;
//import java.io.IOException;
//
///**
// *
// * @author WIN7
// */
//public class Parser {
//
//	public static void main(String[] args) throws IOException {
//		String[] arg = { "input.txt", "output.txt", "outXML.txt" };
//		// -------------------1-------------------
//		// input:lấy từ stanford core, //kết quả lấy RepresentativeMention
//		CoreNlpStandford core = new CoreNlpStandford(arg);
//		Annotation a = (Annotation) core.coreReso(arg);
//		System.out.println("Sentence: \"" + a + "\"");
//		// ---------------------2-----------------
//		System.out.println("-----------------------KẾT QUẢ ĐỒNG THAM CHIẾU ----------------------------");
//		ResultCoreference reCoref = new ResultCoreference();
//		reCoref.setResultCore(a);
//		reCoref.printResultCoreference();
//		// ---------------------3-----------------
//		System.out.println("-----------------------KẾT QUẢ PHÂN TÍCH CẢM XÚC ----------------------------");
//		ResultSentiment rese = new ResultSentiment();
//		rese.setResultSentiment(a);
//		rese.printResultSentiment();
//		// ---------------------4-------------------
//		System.out.println(
//				"-----------------------KẾT QUẢ ĐỒNG THAM CHIẾU  & PHÂN TÍCH CẢM XÚC----------------------------");
//		ResultObjectSentiment OAS = new ResultObjectSentiment(reCoref, rese);
//		ResultSentiment obAsSe = OAS.setResultObjectSentiment();
//		obAsSe.printResultSentiment();
//		// ---------------------5--------------------
//		System.out.println(
//				"-----------------------KẾT QUẢ ĐỒNG THAM CHIẾU ĐỐI TƯỢNG - KHÍA CẠNH - CẢM XÚC----------------------");
//
//	}
//	// -------------------3----------------------------
//	// ontology: là 1 file owl, đọc và đưa các RepresentativeMention vào để tìm
//	// ra mối quan hệ OBJ-ASPect
//	// output: đồng tham chiếu giữa OBJ - ASP - SEN
//}