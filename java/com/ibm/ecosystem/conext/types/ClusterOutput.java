package com.ibm.ecosystem.conext.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterOutput {

	ClusterInput allWords;
	List<Set<String>> clusters;
	public ClusterOutput(ClusterInput allWords, List<Set<String>> clusters) {
		this.allWords = allWords;
		this.clusters = clusters;
	}

	public static void main(String[] args) {
//
//		List<String> t = Arrays.asList("A", "b", "c");
//		Map<Object, Object> m = t.stream().collect(Collectors.toMap(x -> x.toString(), x -> x));
//		m.entrySet().forEach(System.out::println);
	}

	public Map<Integer, Set<String>> response() {

		Set<String> unclusteredWords = findUnclusteredWords();
		Map<Integer, Set<String>> results = new HashMap<Integer, Set<String>>();
		results.put(0, unclusteredWords);
		for (int i = 0; i < clusters.size(); i++) {
			results.put(i + 1, clusters.get(i));
		}

		return results;
	}

	public boolean resultsContainWord(String word) {
		for (Set<String> cluster : clusters) {
			if (cluster.contains(word))
				return true;
		}
		return false;
	}

	public Set<String> findUnclusteredWords() {
		Set<String> unclusteredWords = new HashSet<String>();

		for (String word : allWords.getWords()) {
			if (!resultsContainWord(word)) {
				unclusteredWords.add(word);
			}
		}
		return unclusteredWords;
	}

}
