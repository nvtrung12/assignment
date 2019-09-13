package bayesian;

import static java.lang.Math.sqrt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import util.XlsxUtil;

/**
 * Implement Question, Concept Bayesian Network
 *
 */
public class QCBayesianNetwork {
	public QCBayesianNetwork() {
		// do nothing
	}
	private String oneWord(String input) {
		return String.format("s%s", "-", input);
	}
	protected final static char NOT_SIGN = '-';

	protected Map<ImmutablePair<Set<String>, Set<String>>, Double> pp;

	protected Map<String, Set<String>> outNodes;
	protected Map<String, Set<String>> inNodes;
	protected Set<String> questionNodes;
	protected Set<String> conceptNodes;

	// logger
	public final Logger logger = Logger.getLogger(this.getClass());
	public QCBayesianNetwork(Map<String, Set<String>> outNodes, Map<String, Set<String>> inNodes,
			Set<String> questionNodes, Set<String> conceptNodes) throws Exception {
		this.classInit(outNodes, inNodes, questionNodes, conceptNodes, new HashMap<>());
	}

	public QCBayesianNetwork(Map<String, Set<String>> outNodes, Map<String, Set<String>> inNodes,
			Set<String> questionNodes, Set<String> conceptNodes, Map<String, Object> x) throws Exception {
		this.classInit(outNodes, inNodes, questionNodes, conceptNodes, x);
	}

	public static QCBayesianNetwork fromExcel(String filePath, String sheetName) throws Exception {
		QCBayesianNetwork qc = new QCBayesianNetwork();

		// read Excel file from filePath
		List<List<String>> data = XlsxUtil.readXlsx(filePath, sheetName);

		// format
		// line 1: a k m n
		// line 2: Q1 Q2 ...
		// line 3: C1 C2 ...
		// lines: U V
		//
		Scanner sc = new Scanner(data.get(0).get(0));
		double a = sc.nextDouble();
		double k = sc.nextDouble();
		double m = sc.nextDouble();
		double n = sc.nextDouble();

		// for nodes
		qc.questionNodes = new HashSet<>();
		qc.conceptNodes = new HashSet<>();
		for (String s : data.get(1).get(0).split("\\s+"))
			qc.questionNodes.add(s);
		for (String s : data.get(2).get(0).split("\\s+"))
			qc.conceptNodes.add(s);

		// for connection
		qc.inNodes = new HashMap<String, Set<String>>();
		qc.outNodes = new HashMap<String, Set<String>>();

		Set<String> nodes = new HashSet<>(qc.questionNodes);
		nodes.addAll(qc.conceptNodes);
		for (String node : nodes)
			if (qc.inNodes.get(node) == null)
				qc.inNodes.put(node, new HashSet<>());
		for (String node : nodes)
			if (qc.outNodes.get(node) == null)
				qc.outNodes.put(node, new HashSet<>());

		for (int line = 3; line < data.size(); ++line) {
			String u = data.get(line).get(0).trim();
			String v = data.get(line).get(1).trim();

			// add edge U-V to graph
			qc.outNodes.get(u).add(v);
			qc.inNodes.get(v).add(u);
		}

		// setup TODO remove hardcode here
		qc.pp = new HashMap<>();

		// P(Qi) = a
		double pQ = a;
		for (String qNode : qc.questionNodes) {
			qc.put(qNode, pQ);
			qc.put(getNeg(qNode), 1 - pQ);
		}

		// P(Ci) = k
		for (String cNode : qc.conceptNodes) {
			qc.put(cNode, k);
			qc.put(getNeg(cNode), 1 - k);
		}

		// P(C..|-Qj) = m
		// P(C1C2|-Q1) = m
		// P(C2|-Q2) = m
		qc.put("C1", getNeg("Q1"), Math.sqrt(m));
		qc.put("C2", getNeg("Q1"), Math.sqrt(m));

		qc.put("C2", getNeg("Q2"), m);

		// ?
		qc.put("C3", getNeg("C1"), sqrt(n));
		qc.put("C4", getNeg("C1"), sqrt(n));

		qc.put("C4", getNeg("C2"), n);

		qc.put("C5", getNeg("C3"), n);
		qc.put("C5", getNeg("C4"), n);
		
		for (String ci: qc.conceptNodes) {
			
		}

		// calculate and cache all adjective node
		for (String node : nodes) {
			for (String toNode : qc.outNodes.get(node)) {
				double p = qc.p(node, toSet(toNode));
				qc.put(node, toNode, p);
				qc.put(getNeg(node), toNode, 1 - p);

				p = qc.p(toNode, toSet(node));
				qc.put(toNode, toSet(node), p);
				qc.put(getNeg(toNode), toSet(node), 1 - p);
			}
		}

		return qc;
	}

	/**
	 * class Initial
	 * 
	 * @param outNodes
	 * @param inNodes
	 * @param questionNodes
	 * @param conceptNodes
	 * @param kwargs
	 * @throws Exception
	 */
	
	public void classInit(Map<String, Set<String>> outNodes, Map<String, Set<String>> inNodes,
			Set<String> questionNodes, Set<String> conceptNodes, Map<String, Object> kwargs) throws Exception {
		this.logger.setLevel(Level.OFF);

		this.outNodes = outNodes;
		this.inNodes = inNodes;
		this.questionNodes = questionNodes;
		this.conceptNodes = conceptNodes;

		Set<String> nodes = new HashSet<>(questionNodes);
		nodes.addAll(conceptNodes);
		for (String node : nodes)
			if (this.inNodes.get(node) == null)
				this.inNodes.put(node, new HashSet<>());
		for (String node : nodes)
			if (this.outNodes.get(node) == null)
				this.outNodes.put(node, new HashSet<>());

		double a = (double) kwargs.get("l");
		double k = (double) kwargs.get("k");
		double m = (double) kwargs.get("m");
		double n = (double) kwargs.get("n");

		double pQ = a;
		this.pp = new HashMap<>();

		// P(Qi) = a
		for (String qNode : this.questionNodes) {
			put(qNode, pQ);
			put(getNeg(qNode), 1 - pQ);
		}

		// P(Ci) = k
		for (String cNode : this.conceptNodes) {
			put(cNode, k);
			put(getNeg(cNode), 1 - k);
		}

		// P(C..|-Qj) = m
		// P(C1C2|-Q1) = m
		// P(C2|-Q2) = m
		put("C1", getNeg("Q1"), Math.sqrt(m));
		put("C2", getNeg("Q1"), Math.sqrt(m));

		put("C2", getNeg("Q2"), m);

		// ?
		put("C3", getNeg("C1"), sqrt(n));
		put("C4", getNeg("C1"), sqrt(n));

		put("C4", getNeg("C2"), n);

		put("C5", getNeg("C3"), n);
		put("C5", getNeg("C4"), n);
		
		
		//{Q1=[C1, C2], C3=[C5], Q2=[C2], C4=[C5], C5=[], C1=[C3, C4], C2=[C4]}
		//incode {C3=[C1], Q1=[], C4=[C1, C2], Q2=[], C5=[C3, C4], C1=[Q1], C2=[Q1, Q2]}
//		this.inNodes.forEach((key,value)->{
//			value.forEach(s->{
//				boolean isQuestion = false;
//				for (String qu : questionNodes) {
//					if (s.equals(qu)) {
//						isQuestion = true;
//						break;
//					}
//				}
//				
//				if (isQuestion) {
//					//m
//					
////					System.out.println("question "+ key+" "+s);
////					put(key, getNeg(s),Math.sqrt(m));
//				}else {
//					//n
////					System.out.println("no question "+ key+" "+s);
////					put(key, getNeg(s),Math.sqrt(n));
//				}
//			});
//		});

		// calculate and cache all adjective node
		for (String node : nodes) {
			for (String toNode : this.outNodes.get(node)) {
				double p = p(node, toSet(toNode));
				put(node, toNode, p);
				put(getNeg(node), toNode, 1 - p);

				p = p(toNode, toSet(node));
				put(toNode, toSet(node), p);
				put(getNeg(toNode), toSet(node), 1 - p);
			}
		}

	}
	
	public void classInits(Map<String, Set<String>> outNodes, Map<String, Set<String>> inNodes,
			Set<String> questionNodes, Set<String> conceptNodes, Map<String, Object> kwargs) throws Exception {
		this.logger.setLevel(Level.OFF);
		

		this.outNodes = outNodes;
		this.inNodes = inNodes;

		this.outNodes = new HashMap<>(outNodes);
		this.inNodes = new HashMap<>(inNodes);
		this.questionNodes = questionNodes;
		this.conceptNodes = conceptNodes;
		
		
		
		Set<String> nodes = new HashSet<>(questionNodes);
		nodes.addAll(conceptNodes);
		for (String node : nodes)
			if (this.inNodes.get(node) == null)
				this.inNodes.put(node, new HashSet<String>());
		for (String node : nodes)
			if (this.outNodes.get(node) == null)
				this.outNodes.put(node, new HashSet<>());

		double l = (double) kwargs.get("l");
		double k = (double) kwargs.get("k");
		double m = (double) kwargs.get("m");
		double n = (double) kwargs.get("n");

		double pQ = l;
		this.pp = new HashMap<>();

		// P(Qi) = a
		for (String qNode : this.questionNodes) {
			put(qNode, pQ);
			put(getNeg(qNode), 1 - pQ);
		}

		// P(Ci) = k
		for (String cNode : this.conceptNodes) {
			put(cNode, k);
			put(getNeg(cNode), 1 - k);
		}

		// P(C..|-Qj) = m
		// P(C1C2|-Q1) = m
		// P(C2|-Q2) = m
		//{C3=[C1], Q1=[], C4=[C1, C2], Q2=[], C5=[C3, C4], C1=[Q1], C2=[Q1, Q2]}
		
		this.inNodes.forEach((key,value)->{
			value.forEach(s->{
				boolean isQuestion =false;
				for (String qu : questionNodes) {
					if(s.equals(qu)) {
						isQuestion =true;
						break;
					}
				}
				if(isQuestion) {
					System.out.println("question   " +key + "    "+s);
					put(key, getNeg(s),Math.sqrt(m));
				}else {
					System.out.println("Collection  " +key + "    "+s);
					put(key, getNeg(s),Math.sqrt(n));
				}
			});
		});
		
//		put("C1", getNeg("Q1"), Math.sqrt(m));
//		put("C2", getNeg("Q1"), Math.sqrt(m));
//
//		put("C2", getNeg("Q2"), m);
//
//		// ?
//		put("C3", getNeg("C1"), sqrt(n));
//		put("C4", getNeg("C1"), sqrt(n));
//
//		put("C4", getNeg("C2"), n);
//
//		put("C5", getNeg("C3"), n);
//		put("C5", getNeg("C4"), n);

		// calculate and cache all adjective node
		
		for (String node : nodes) {
			//System.out.println(node);
			Set<String> set = this.outNodes.get(node);
			for (String toNode : set) {
				double p = p(node, toSet(toNode));
				put(node, toNode, p);
				put(getNeg(node), toNode, 1 - p);

				p = p(toNode, toSet(node));
				put(toNode, toSet(node), p);
				put(getNeg(toNode), toSet(node), 1 - p);
			}
		}

	}

	/**
	 * P(a) free of P<br/>
	 * calculate based on a, not a
	 * 
	 * @param a String, Set<String>
	 * @return
	 * @throws Exception
	 */
	public double p(Object a) throws Exception {
		String s = String.format("P(%s)", a);
		logger.debug(s);
		double pVal = 0.0;

		// support String or Set<String>
		Set<String> A = (a instanceof String) ? new HashSet<>(Arrays.asList((String) a)) : (Set<String>) a;

		if (containsKey(A)) {
			logger.debug(s + ": " + get(A));
			return get(A);
		} else {
			// if neg
			if (a instanceof String) {
				Set<String> negA = toSet(getNeg((String) a));
				if (containsKey(negA)) {
					put(A, 1 - get(negA));
					logger.debug(s + ": " + get(A));
					return get(A);
				} else {
					throw new Exception("Must found P(A) or P(-A) in data");
				}
			}

			// calculate A and store to use later
			// find the most ancestor to use as B on P(AB) = P(A|B) * P(B)
			// must be found, otherwise network not correct
			String one = findMostAncestor(A);
			pVal = p(setRemainder(A, one), toSet(one)) * p(one);
			put(A, pVal);

			logger.debug(s + ": " + get(A));
			return get(A);
		}
	}

	/**
	 * P(A|given)
	 * 
	 * @param A
	 * @param given
	 * @return
	 * @throws Exception
	 */
	public double p(String A, Set<String> given) throws Exception {
		String s = String.format("P(%s|%s)", A, given.toString());
//		this.printTest();
		logger.debug(s);
		double pVal = 0.0;

		Set<String> sA = toSet(A);
		if (containsKey(toPair(sA, given))) {
			pVal = get(toPair(sA, given));
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		Set<String> sNA = toSet(getNeg(A));
		if (containsKey(toPair(sNA, given))) {
			pVal = 1 - get(toPair(sNA, given));
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		// process with given
		final Set<String> ancestors = getAncestor(A);
		final Set<String> predecessor = getPredecessor(A);
		Set<String> depend = given.stream().filter(o -> ancestors.contains(o) || ancestors.contains(getNeg(o))
				|| predecessor.contains(o) || predecessor.contains(getNeg(o))).collect(Collectors.toSet());

		// only work with depend
		if (depend.size() == 0) {
			pVal = p(A);
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		// only get dependent, for small set
		if (depend.size() < given.size()) {
			pVal = p(A, depend);
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		if (given.size() == 1) {
			// direct => must be in pp, P(A|B) must in pp
			String B = given.iterator().next();
			if (this.inNodes.get(getOrg(A)).contains(getOrg(B)) || this.inNodes.get(getOrg(B)).contains(getOrg(A))) {
				if (containsKey(toPair(A, B))) {
					logger.debug(s + ": " + get(toPair(A, B)));
					pVal = get(toPair(A, B));
					logger.debug(s + ": " + pVal);
					return pVal;
				}

				// from 1 - P(-A|B)
				else if (containsKey(toPair(getNeg(A), B))) {
					pVal = 1 - get(toPair(getNeg(A), B));
					put(toPair(A, B), pVal);
					logger.debug(s + ": " + pVal);
					return pVal;
				}

				// from reverse order P(A|B) = P(B|A)*P(A)/P(B)
				else if (containsKey(toPair(B, A))) {
					pVal = get(toPair(B, A)) * p(A) / p(B);
					put(toPair(A, B), pVal);
					logger.debug(s + ": " + pVal);
					return pVal;
				} else if (containsKey(toPair(getNeg(B), A))) { // P(-B|A)
					put(toPair(B, A), 1 - get(toPair(getNeg(B), A)));
					pVal = (1 - get(toPair(getNeg(B), A))) * p(A) / p(B);
					logger.debug("P(" + toPair(getNeg(B), A) + ") :" + get(toPair(getNeg(B), A)));
					logger.debug(s + ": " + pVal);
					return pVal;
				} // A|-B => calculate P(-B|A) store and then call again
				else if (containsKey(toPair(A, getNeg(B)))) {
					double pnBA = get(toPair(A, getNeg(B))) * p(getNeg(B)) / p(A);
					put(toPair(getNeg(B), A), pnBA);
					pVal = p(A, given); // recalculate
					logger.debug(s + ": " + pVal);
					return pVal;
				}

				// P(-A|-B) => calculate P(A|-B), store and call again
				else if (containsKey(toPair(getNeg(A), getNeg(B)))) {
					put(toPair(A, getNeg(B)), 1 - get(toPair(getNeg(A), getNeg(B))));
					pVal = p(A, given); // recalculate
					logger.debug(s + ": " + pVal);
					return pVal;
				} else if (containsKey(toPair(B, getNeg(A)))) { // P(B|-A)
					double pnAB = get(toPair(B, getNeg(A))) * p(getNeg(A)) / p(B);
					put(toPair(getNeg(A), B), pnAB);

					pVal = p(A, given);
					logger.debug(s + ": " + pVal);
					return pVal;

				} else if (containsKey(toPair(getNeg(B), getNeg(A)))) { // P(-B|-A)
					put(toPair(B, getNeg(A)), 1 - get(toPair(getNeg(B), getNeg(A))));
					pVal = p(A, given); // recalculate with more info
					logger.debug(s + ": " + pVal);
					return pVal;

				} else {
					pVal = p(toSet(A, B)) / p(B);
					logger.debug(s + ": " + pVal);
					return pVal;
					// TODO if not enough info then this will be infined loop and overstack
					// throw new Exception(String.format("P(%s|%s) must given", A, B));
				}
			} else {
				// indirect => calc, P(A|B) = E(P(AB|X)) by X / P(B) which X in middle of A, B
				String X = findMidAncestor(A, B);

				// if null then calc from reverse from P(B|A)
				if (X == null)
					return p(B, toSet(A)) * p(A) / p(B);

				// P(A|B) = Sum(P(A|X) * P(X|B)) for X , -X in path A-B
				String negX = getNeg(X);
				double pabx = p(A, toSet(X)) * p(X, given);

				double pabnx = p(A, toSet(negX)) * p(negX, given);

				pVal = (pabx + pabnx); // / p(B);
				logger.debug(s + ": " + pVal);
				return pVal;
			}
		}

		// depend > 1
		// direct depend
		// indirect depend
		pVal = p(extendSet(given, A)) / p(given);
		put(toPair(A, given), pVal);
		logger.debug(s + ": " + pVal);
		return pVal;
	}

	/**
	 * enough A and given
	 * 
	 * @param A
	 * @param given
	 * @return
	 * @throws Exception
	 */
	public double p(Set<String> A, Set<String> given) throws Exception {
		String s = String.format("P(%s|%s)", A.toString(), given.toString());
		logger.debug(s);
		double pVal = 0.0;

		if (A.size() == 0) {
			pVal = 0.0;
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		if (A.size() == 1) {
			pVal = p(A.iterator().next(), given);
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		// given is empty then free of A
		if (given.size() == 0) {
			pVal = p(A);
			logger.debug(s + ": " + pVal);
			return pVal;
		}

		// remove X in both A and given
		// if both X and negX in A then => Zero

		// when given not empty, A more than 1
		if (given.size() == 1) {
			String B = given.iterator().next();

			if (containsKey(toPair(A, given))) {
				pVal = get(toPair(A, given));
				logger.debug(s + ": " + pVal);
				return pVal;
			} else {
				if (true) {
					// if A_1 and given (B) direct connect
					// type: B -> A_2 <- A_1 (A = A_1, A_2) (A_1 and B independent)
					// calculate: P(A_1A_2|B) = P(A_2|B) * P(A_1 | A_2 B)
					// P(C2Q2|Q1) = P(C2|Q1) * P(Q2|Q1C2)
					// TODO
					// A_2 most Predecessor
					// A_1 is A - A_2
					//
					String A_2 = findHighestNode(A);
					Set<String> A_1 = setRemainder(A, A_2);
					Set<String> A_2B = new HashSet<String>(given);
					A_2B.add(A_2);
					logger.info(String.format("P(%s|%s)  * P(%s|%s)", A_2, given, A_1, A_2B));
					pVal = p(A_2, given) * p(A_1, A_2B);
					logger.debug(s + ": " + pVal);
					return pVal;
				}

				// else
				// in case B is Ancestor of X, A
				// B -> A_1 -> A_2 => P(A_1A_2|B) = P(A_1|B) * P(A_2|A_1) TODO check fomular
				// B must in lowest of tree in A, then any A not in ancestor of B
				Set<String> bAncestor = getAncestor(B);
				int xsize = A.stream().filter(e -> bAncestor.contains(getOrg(e))).collect(Collectors.toList()).size();
				if (getAncestor(A).contains(getOrg(B)) && xsize == 0) {
					String A_1 = findMostAncestor(A);
					Set<String> A_2 = setRemainder(A, A_1);
					logger.info(String.format("A_1 -> %s  A_2 -> %s   given -> %s", A_1, A_2, given));
					logger.info(String.format("P(%s|%s)  * P(%s|%s)", A_1, given, A_2, A_1));
					double p = p(A_1, given) * p(A_2, toSet(A_1));
					put(toPair(A, given), p);
				} else {

					double p = p(extendSet(A, B)) / p(given);
					put(toPair(A, given), p);
				}
				pVal = get(toPair(A, given));
				logger.debug(s + ": " + pVal);
				return pVal;
			}
		} else {
			// given size greater than 1
			// TODO just do for Q1, Q2, ..., Qn, that independent
			pVal = p(extendSet(given, A)) / p(given);
			put(toPair(A, given), pVal);
			logger.debug(s + ": " + pVal);
			return pVal;
		}

//		return 0.0;
	}

	private String findHighestNode(Set<String> a) {
		for (String node : a) {
			Set<String> aR = setRemainder(a, node);
			Set<String> tmp = getPredecessor(node);
			boolean isFound = tmp.stream().anyMatch(o -> aR.contains(o) || aR.contains(getNeg(o)));
			if (!isFound)
				return node;

		}
		return null;
	}

	/**
	 * find O in a that ancestor of O not in a, correct network with a set then
	 * always found
	 * 
	 * @param a
	 * @return
	 */
	private String findMostAncestor(Set<String> a) {
		for (String o : a) {
			Set<String> oAncestor = getAncestor(o);
			Set<String> inA = oAncestor.stream().filter(e -> a.contains(e) || a.contains(getNeg(e)))
					.collect(Collectors.toSet());
			if (inA.size() == 0)
				return o;
		}
		return null;
	}

	/**
	 * Find mid ancestor of a and b: b -> mid -> a<br/>
	 * Use in special case P(A|B)
	 * 
	 * @param a
	 * @param b
	 * @return may be have many mid, only find one
	 */
	private String findMidAncestor(String a, String b) {
		Set<String> aAncestor = getAncestor(a);

		// get b predecessor
		final Set<String> bPredecessor = getPredecessor(b);

		List<String> ancestorOfb = aAncestor.stream()
				.filter(e -> bPredecessor.contains(e) || bPredecessor.contains(getNeg(e))).collect(Collectors.toList());

		if (ancestorOfb.size() == 0)
			return null;

		return ancestorOfb.get(0);
	}

	/**
	 * Get all ancestor of one node, that node will be dependent. <br/>
	 * Will get parent, parent of parent... <br/>
	 * TODO make sure there will be stop, then no cyrcle in network
	 * 
	 * @param node
	 * @return
	 */
	protected Set<String> getAncestor(String node) {

		// must get orginal of node
		node = getOrg(node);

		Set<String> ret = new HashSet<>();

		Set<String> parents = this.inNodes.get(node);
		ret.addAll(parents);

		for (String parent : parents)
			ret.addAll(getAncestor(parent));

		return ret;
	}

	protected Set<String> getAncestor(String... nodes) {
		return getAncestor(new HashSet<>(Arrays.asList(nodes)));
	}

	public Set<String> getAncestor(Set<String> nodes) {
		List<Set<String>> tmp = nodes.parallelStream().map(o -> getAncestor(o)).collect(Collectors.toList());
		Set<String> ret = new HashSet<>();
		for (Set<String> o : tmp)
			ret.addAll(o);
		return ret;
	}

	/**
	 * Get all childs
	 * 
	 * @param nodes
	 * @return
	 */
	public Set<String> getPredecessor(String... nodes) {
		Set<String> ret = new HashSet<>();

		for (String node : nodes) {
			// must get orginal of node
			node = getOrg(node);

			Set<String> childs = this.outNodes.get(node);
			ret.addAll(childs);

			for (String child : childs)
				ret.addAll(getPredecessor(child));
		}
		return ret;
	}

	public Set<String> getPredecessor(Set<String> nodes) {
		return getPredecessor((String[]) nodes.toArray());
	}

	public static boolean isNeg(String a) {
		return a.charAt(0) == QCBayesianNetwork.NOT_SIGN;
	}

	/**
	 * get Neg of a
	 * 
	 * @param a
	 * @return
	 */
	public static String getNeg(String a) {
		if (isNeg(a))
			return a.substring(1);
		else
			return QCBayesianNetwork.NOT_SIGN + a;
	}

	/**
	 * all A and -A must return A for use in general and check ancestor
	 * 
	 * @param a not null, not empty
	 * @return orginal version of a
	 */
	public static String getOrg(String a) {
		if (isNeg(a))
			return a.substring(1);
		else
			return a;
	}

	public static Set<String> toSet(String... e1) {
		return new HashSet<>(Arrays.asList(e1));
	}

	/**
	 * Extend old set to add elements
	 * 
	 * @param old
	 * @param es
	 * @return
	 */
	public static Set<String> extendSet(Set<String> old, String... es) {
		Set<String> ret = new HashSet<>(old);
		for (String e : es)
			ret.add(e);
		return ret;
	}

	public static Set<String> extendSet(Set<String> old, Set<String> es) {
		Set<String> ret = new HashSet<>(old);
		ret.addAll(es);
		return ret;
	}

	/**
	 * 
	 * @param a     String or Set<String> or null
	 * @param given String or Set<String> or null
	 * @return
	 */
	public static ImmutablePair<Set<String>, Set<String>> toPair(Object a, Object given) {
		if (null == a)
			a = new HashSet<>();
		if (null == given)
			given = new HashSet<>();

		Set<String> s = a instanceof String ? toSet((String) a) : (Set<String>) a;
		Set<String> g = given instanceof String ? toSet((String) given) : (Set<String>) given;
		return new ImmutablePair<>(s, g);
	}

	public static Set<String> setRemainder(Set<String> o, String removed) {
		Set<String> re = new HashSet<>(o);
		re.remove(removed);
		return re;
	}

	/**
	 * 
	 * @param key String, Set<String>, Pair<Set<String>,Set<String>>
	 * @return true/false in pp
	 */
	protected boolean containsKey(Object key) {
		return this.pp.containsKey(keyProcess(key));
	}

	protected Double put(Object key, double value) {
		ImmutablePair<Set<String>, Set<String>> pkey = keyProcess(key);

		return this.pp.put(pkey, value);
	}

	protected Double put(Object key, Object key2, double value) {
		return this.put(toPair(key, key2), value);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	protected double get(Object key) {
		return this.pp.get(keyProcess(key));
	}

	@SuppressWarnings("unchecked")
	protected ImmutablePair<Set<String>, Set<String>> keyProcess(Object key) {
		ImmutablePair<Set<String>, Set<String>> realKey = null;

		if (key instanceof String)
			return toPair(toSet((String) key), null);

		if (key instanceof Set)
			return toPair((Set<String>) key, null);

		realKey = (ImmutablePair<Set<String>, Set<String>>) key;
		// if another case, then will be throw cast exception here

		return realKey;
	}

	public void printTest() {
		for (ImmutablePair<Set<String>, Set<String>> key : this.pp.keySet())
			System.out.println(String.format("%s: %s", key, this.pp.get(key)));
	}

	
}
