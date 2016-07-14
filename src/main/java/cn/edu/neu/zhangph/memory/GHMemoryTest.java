package cn.edu.neu.zhangph.memory;

import java.util.*;

import com.github.davidmoten.rtree.*;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;

import cn.edu.neu.zhangph.composite.CommonCreateQuery;
import cn.edu.neu.zhangph.method.CreateTree;
import cn.edu.neu.zhangph.util.GlobalHeap;
import cn.edu.neu.zhangph.util.IndividualHeap;
import cn.edu.neu.zhangph.util.Pair;

public class GHMemoryTest {
	public static void main(String[] args) {
		String file = "./data/sample.txt";
		gh_k(file, 6);
	}
	public static void gh_k(String file, int traNum){
		System.out.println("Start time is : "
				+ GregorianCalendar.getInstance().getTime());
		//内存测试
		Runtime run = Runtime.getRuntime(); // Runtime is singletom object
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		} // 暂停程序执行
		run.gc();
		// 获取开始时内存使用量
		
		/*
		 * 0. Create r-tree for index
		 */
		RTree<Integer, Geometry> tree = CreateTree.createTree(file);
		
		long startMem = run.totalMemory() - run.freeMemory();
		// / 参数修改
		int q = 10;
//		int k = 5;
		double miu = 0.3;
		for (int k = 2; k < 21; k+=2) {
			/*
			 * 1. Create query points in query[]
			 */
			Point[] query = CommonCreateQuery.createQuery(q);
	
			/*
			 * 2. Construct the individual heap for each query point
			 */
			IndividualHeap[] individualHeap = createIndividual(query, tree, k);
	
			/*
			 * 3. initialize the global heap and for each individual heap pop a
			 * matching pair and push it to global
			 */
			GlobalHeap globalHeap = new GlobalHeap();
			for (int i = 0; i < individualHeap.length; i++) {
				globalHeap.push(i, individualHeap[i].pop());
			}
			Pair[][] candidateRes = new Pair[traNum][query.length];
	
			/**
			 * 4. initialize candidate set and repeat
			 */
			while (!isFullMatching(k, q, candidateRes)) {
				Pair pair = globalHeap.pop();
				int x = ((Point) pair.getGeometry()).getId();
				int y = pair.getFlag();
				if (candidateRes[x][y] == null && pair != null) {
					candidateRes[x][y] = pair;
				}
				if (!individualHeap[pair.getFlag()].getPairs().isEmpty()
						&& individualHeap[pair.getFlag()].getPairs().size() != 0) {
					globalHeap.push(pair.getFlag(),
							individualHeap[pair.getFlag()].pop());
				} else
					break;
			}
			// The CompareCandidateGeneration memory statistics
			long endMem = run.totalMemory() - run.freeMemory();
			System.out.println((endMem - startMem)/1000000);
		}
		
		System.out.println("End time is : "
				+ GregorianCalendar.getInstance().getTime());
	}
	public static boolean isFullMatching(int k, int qNum, Pair[][] result) {
		int allMatchTra = 0;
		for (int i = 0; i < result.length; i++) {
			int count = 0; 
			for (int j = 0; j < result[i].length; j++) {
				if (result[i][j] != null)
					count++;
			}
			if (count >= qNum)
				allMatchTra++;
		}
		if (allMatchTra >= k)
			return true;
		else
			return false;
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
