package com.ibm.ecosystem.conext.core;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DBpediaClustering {

	public static void main(String[] args) {
		List<String> allDicts = Arrays.asList("AAAIillness", "AAAIfood", "AAAIweapon", "AAAIvehicle");
		for (String dict : allDicts) {
			System.out.println(dict);
			DBpediaClustering e = new DBpediaClustering();
			List<String> seeds = loadSeedsFromDictionary(dict);

			Set<String> allWords = new HashSet<String>();
			List<Set<String>> cat = e.cluster(seeds);
			for (Set<String> cluster : cat) {
				Set<String> categoriesForCluster = new HashSet<String>();
				for (String word : cluster) {
					Pattern p = Pattern.compile("([a-zA-Z]*)\\[(.*)\\]");
					Matcher m = p.matcher(word);
					while (m.find()) {
						allWords.add(m.group(1));
						categoriesForCluster.add(m.group(2));
					}
				}


				List<String> r = QueryDBPedia.querySPARQL(new ArrayList(categoriesForCluster));
				for(String s : cluster){
					System.out.print(s + ",");
				}

				System.out.println();
				for(int i = 0; i < r.size(); i++){
					System.out.print(r + ", ");
				}
				System.out.println();
				System.out.println();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			}

		}
	

	}
	Map<String, List<String>> categoryCoverage = new HashMap<String, List<String>>();
	public DBpediaClustering() {

	}

	public double similarity(Map.Entry<String, List<String>> word1WithCats, Map.Entry<String, List<String>> word2WithCats) {
		double sim = 0;
		List<String> word1Cats = word1WithCats.getValue();
		List<String> word2Cats = word2WithCats.getValue();
		for (String cat : word1Cats) {
			if (word2Cats.contains(cat))
				sim = sim + 1;

		}
		return sim / (double) word1Cats.size();
	}

	public double similarity(Set<String> s1, Set<String> s2, Map<String, List<String>> coverageMap) {
		double sim = 0;
		for (String word1 : s1) {
			double wordSim = 0;
			List<String> word1Cats = coverageMap.get(word1);
			if (word1Cats == null)
				word1Cats = new ArrayList<String>();
			for (String word2 : s2) {
				List<String> word2Cats = coverageMap.get(word2);
				if(word2Cats == null)
					word2Cats = new ArrayList<String>();
				for (String cat : word1Cats) {
					if (word2Cats.contains(cat))
						wordSim = wordSim + 1;
				}
			}
			wordSim = wordSim / (double) word1Cats.size();
			sim += wordSim;
		}

		return sim;
	}

	public double similarity(String word1, String word2, Map<String, List<String>> coverageMap) {
		double sim = 0;
		List<String> word1Cats = coverageMap.get(word1);
		if (word1Cats == null)
			word1Cats = new ArrayList<String>();
		List<String> word2Cats = coverageMap.get(word2);
		if (word2Cats == null)
			word1Cats = new ArrayList<String>();
		for (String cat : word1Cats) {
			if (word2Cats.contains(cat))
				sim = sim + 1;

		}
		return sim / (double) word1Cats.size();
	}

	public List<Set<String>> cluster(Collection<String> seeds) {

		boolean sense = true;
		for (String seedWord : seeds) {

			if (sense) {
				Map<String, List<String>> disambiguatedCats = QueryDBPedia.getDisambiugatedCategories(seedWord);
				List<String> categories;
				for (Entry<String, List<String>> disambiguatedCat : disambiguatedCats.entrySet()) {
					String senseOfSeedWord = disambiguatedCat.getKey();
					categories = disambiguatedCat.getValue();
					categoryCoverage.put(senseOfSeedWord, categories);

				}
			} else {
				List<String> categories = QueryDBPedia.getCategories(seedWord);
				if (categories.size() > 0) {
					categoryCoverage.put(seedWord, categories);
				}
			}

		}

		List<Set<String>> clusters = new ArrayList<Set<String>>();

		for (Entry<String, List<String>> wordWithCategories : categoryCoverage.entrySet()) {
			for (Entry<String, List<String>> word2WithCategories : categoryCoverage.entrySet()) {
				String word = wordWithCategories.getKey();
				String word2 = word2WithCategories.getKey();
				if (!word.equals(word2)) {
					double sim = similarity(wordWithCategories, word2WithCategories);
					if (sim > .2) {
						Set<String> clusterToAdd = null;
						for (Set<String> cluster : clusters) {
							if (cluster.contains(word) || cluster.contains(word2)) {
								clusterToAdd = cluster;
								clusterToAdd.add(word);
								clusterToAdd.add(word2);
								break;
							}
						}
						if (clusterToAdd == null) {
							clusterToAdd = new HashSet<String>();
							clusterToAdd.add(word);
							clusterToAdd.add(word2);
							clusters.add(clusterToAdd);
						}

					}
				}
			}
		}
		return clusters;

	}

	public List<Set<String>> mergeClusters(List<Set<String>> clusters, Map<String, List<String>> categoryCoverage) {
		double thresh = .05;

		Map<Integer, List<Integer>> pairsToMerge = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < clusters.size() - 1; i++) {
			for (int j = 0; j < clusters.size() - 1; j++) {
				if (i != j) {
					Set<String> c1 = clusters.get(i);
					Set<String> c2 = clusters.get(j);
					// double numMatching = ((double)
					// c1.stream().filter(c2::contains).collect(Collectors.toList()).size())
					// / c2.size();
					double numMatching = similarity(c1, c2, categoryCoverage);
					if (numMatching >= thresh) {
						System.out.println("merge " + (i + 1) + " with " + (j + 1) + " at " + numMatching);
						List<Integer> oldList = pairsToMerge.get(i);
						if (oldList == null)
							oldList = new ArrayList<Integer>();
						oldList.add(j);
						pairsToMerge.put(i, oldList);

					}
				}
			}
		}
		List<Set<String>> mergedClusters = new ArrayList<Set<String>>();

		for (Entry<Integer, List<Integer>> pair : pairsToMerge.entrySet()) {
			Integer baseClusterIndex = pair.getKey();
			Set<String> baseCluster = clusters.get(baseClusterIndex);
			for (Integer indexOfClusterToMerge : pair.getValue()) {
				if (baseClusterIndex < indexOfClusterToMerge) {
					Set<String> clusterToMerge = clusters.get(indexOfClusterToMerge);
					baseCluster.addAll(clusterToMerge);
				}
			}
			mergedClusters.add(baseCluster);
		}
		return mergedClusters;
	}

	public static List<String> loadSeedsFromDictionary(String dictionary) {

		List<String> seeds = new ArrayList<String>();
		String username = "<USERID";
		String password = "<PASSWORD>";
		String login = username + ":" + password;

		String base64login = new String(org.apache.commons.codec.binary.Base64.encodeBase64(login.getBytes()));

		try {
			Document document = Jsoup.connect("http://mood.element.almaden.ibm.com/glimpse/api/dictionary/bnyname/" + URLEncoder.encode(dictionary) + "/seeds").header("Authorization", "Basic " + base64login).get();
			for (Element e : document.select("seed")) {
				seeds.add(e.text());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return seeds;
	}

	// merge by most common category if distnace < threshold
}
