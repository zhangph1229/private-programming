package cn.edu.neu.zhangph.composite;

import java.util.*;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;

import cn.edu.neu.zhangph.method.CreateTree;
import cn.edu.neu.zhangph.util.GlobalHeap;
import cn.edu.neu.zhangph.util.IndividualHeap;
import cn.edu.neu.zhangph.util.Pair;
/**
 * 单个方法测试类
 * @author zhangph
 *
 */
public class Common {
	public static void main(String[] args) {
		System.out.println("Start time is : "
				+ GregorianCalendar.getInstance().getTime());
		/*
		 * 0. Create r-tree for index
		 */
		// String file = System.getProperty("user.dir") + "/data/uniform.csv";
		String file = "./data/sample.txt";
		RTree<Integer, Geometry> tree = CreateTree.createTree(file);
		
		// / 参数修改
		int q = 5;
		int k = 3;
		double miu = 0.3;
		int traNum = 1001;

		/*
		 * 1. Create query points in query[]
		 */
		Point[] query = CommonCreateQuery.createQuery(q);
		System.out.println(query[0] + "," + query[1] + "," + query[2]);

		 long start = System.currentTimeMillis(); // 1
		/*
		 * 2. Construct the individual heap for each query point
		 */
		IndividualHeap[] individualHeap = createIndividual(query, tree, k);
		Debug.debugIndividualHeap(individualHeap);

		/*
		 * 3. initialize the global heap and for each individual heap pop a
		 * matching pair and push it to global
		 */
//		GlobalHeap globalHeap = new GlobalHeap();
//		for (int i = 0; i < individualHeap.length; i++) {
//			globalHeap.push(i, individualHeap[i].pop());
//		}
//		Debug.debugGlobalHeap(globalHeap);
//		Pair[][] candidateRes = new Pair[traNum][query.length];
//		Method method = new Method();

//		 Map<Integer, Double> res = method.callCompare(individualHeap,
//		 globalHeap, candidateRes, k, query);

//		Map<Integer, Double> res = method.callSelf(individualHeap, globalHeap,
//				candidateRes, k, query);

//		 Map<Integer, Double> res = method.callQE(individualHeap,
//		 globalHeap, candidateRes, k, query, miu);
		
//		Map<Integer, Double> res = method.callAF(individualHeap,
//		 globalHeap, candidateRes, k, query, miu);
		
//		 Map<Integer, Double> res = method.callAFQH(individualHeap,globalHeap,
//		  candidateRes, k, query, miu);
		
//		Debug.debugGlobalHeap(globalHeap);
//		Debug.debugCandidateSet(candidateRes);
//		Debug.debugCandidateVerification(res);
//		Debug.debugIndividualHeap(individualHeap);

//		 long end = System.currentTimeMillis();
//		 System.out.println(end-start);
	}

	public static IndividualHeap[] createIndividual(Point[] query,
			RTree<Integer, Geometry> tree, int k) {
		IndividualHeap[] individualHeap = new IndividualHeap[query.length];
		for (int i = 0; i < query.length; i++) {
			List<Entry<Integer, Geometry>> queryNearest = tree
					.nearest(query[i], 900000000, 100000).toList().toBlocking()
					.single();
			List<Pair> pairs = tranPair(queryNearest, query[i], i);
			individualHeap[i] = new IndividualHeap(pairs);
		}
		
		for (int i = 0; i < individualHeap.length; i++) {
			 List<Pair> pairs = individualHeap[i].getPairs();
			 Iterator it = pairs.iterator();
			 List<Pair> tmp = new ArrayList<Pair>();
			 tmp.addAll(pairs);
			 Map<Integer, Pair> map = new HashMap<>();
			 while(it.hasNext()){
				 Pair pair = (Pair) it.next();
				 if(!map.containsKey(pair.getGeometry().getId())){
					 map.put(pair.getGeometry().getId(), pair);
				 }else tmp.remove(pair);
			 }
			 individualHeap[i] = new IndividualHeap(tmp);
		}
		return individualHeap;
	}

	/**
	 * 
	 * @param list
	 * @param q
	 * @param i
	 * @return
	 */
	private static List<Pair> tranPair(List<Entry<Integer, Geometry>> list,
			Point q, int i) {
		List<Pair> pairs = new ArrayList<Pair>();
		Iterator<Entry<Integer, Geometry>> it = list.iterator();
		while (it.hasNext()) {
			Entry<Integer, Geometry> entry = it.next();
			double score = entry.geometry().distance(q.mbr());
			pairs.add(new Pair(i, entry.geometry(), score));
		}
		return pairs;
	}

}
