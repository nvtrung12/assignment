//package edu.assessment;
//
//import edu.cmu.lti.lexical_db.ILexicalDatabase;
//import edu.cmu.lti.lexical_db.NictWordNet;
//import edu.cmu.lti.ws4j.RelatednessCalculator;
//import edu.cmu.lti.ws4j.impl.HirstStOnge;
//import edu.cmu.lti.ws4j.impl.JiangConrath;
//import edu.cmu.lti.ws4j.impl.LeacockChodorow;
//import edu.cmu.lti.ws4j.impl.Lesk;
//import edu.cmu.lti.ws4j.impl.Lin;
//import edu.cmu.lti.ws4j.impl.Path;
//import edu.cmu.lti.ws4j.impl.Resnik;
//import edu.cmu.lti.ws4j.impl.WuPalmer;
//
//public class SentenceMatcherSimilarityMatrix
//{
// private static ILexicalDatabase db = new NictWordNet();
// public double[][] getSimilarityMatrix( String[] words1, String[] words2, RelatednessCalculator rc )
//{
//    double[][] result = new double[words1.length][words2.length];
//    for ( int i=0; i<words1.length; i++ ){
//        for ( int j=0; j<words2.length; j++ ) {
//            double score = rc.calcRelatednessOfWords(words1[i], words2[j]);
//            result[i][j] = score;
//          }
//        }
//    return result;
//  }
//  private void compute (String[] words1, String[] words2)
//  {
//    System.out.println("WuPalmer");
//    RelatednessCalculator rc1 = new WuPalmer(db);
//       {
//        double[][] s1 = getSimilarityMatrix(words1, words2,rc1);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s1[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("Resnik");
//    RelatednessCalculator rc2 = new Resnik(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc2);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("JiangConrath");
//    RelatednessCalculator rc3 = new JiangConrath(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc3);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("Lin");
//    RelatednessCalculator rc4 = new Lin(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc4);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("LeacockChodrow");
//    RelatednessCalculator rc5 = new LeacockChodorow(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc5);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("Path");
//    RelatednessCalculator rc6 = new Path(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc6);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("Lesk");
//    RelatednessCalculator rc7 = new Lesk(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc7);
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//        }}
//    System.out.println();
//    System.out.println();
//
//    System.out.println("HirstStOnge");
//    RelatednessCalculator rc8 = new HirstStOnge(db);
//    {
//        double[][] s2 = getSimilarityMatrix(words1, words2,rc8);
//
//        for(int i=0; i<words1.length; i++){
//            for(int j=0; j< words2.length; j++){
//                System.out.print(s2[i][j] +"\t");
//            } 
//            System.out.println();
//           }}
//          }
//
//public static void main(String[] args) 
//{
//    String sent1 = "The boy is playing with a dog.";
//    String sent2 = "The kid is playing with his pet.";
//
//    String[] words1 = sent1.split(" ");
//    String[] words2 = sent2.split(" ");
//    SentenceMatcherSimilarityMatrix sm1 = new SentenceMatcherSimilarityMatrix(); 
//    sm1.compute(words1, words2);
//}
//}