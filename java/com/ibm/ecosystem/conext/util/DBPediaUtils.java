package com.ibm.ecosystem.conext.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.ecosystem.conext.core.QueryDBPedia;

public class DBPediaUtils {

	public static void main(String[] args) {

		DBPediaUtils e = new DBPediaUtils();
		List<String> seeds = Arrays.asList("food", "water", "wine", "drinks", "pizza", "beverage", "cheese", "fish", "chicken", "chocolate");
		Map<String, Integer> cat = e.findBestCategories(seeds);
		for(Entry<String, Integer> c : cat.entrySet()){
			System.out.println(c);
		}

	}

	public int getNumWordsCovered(List<Entry<String, Integer>> subList, Map<String, List<String>> categoryCoverage) {
		Set<String> coveredWords = new HashSet<String>();
		for (Entry<String, Integer> e : subList) {
			String categoryName = e.getKey();
			List<String> wordsForCategory = categoryCoverage.get(categoryName);
			coveredWords.addAll(wordsForCategory);
		}
		return coveredWords.size();
	}
	@SuppressWarnings("unchecked")
	public List<Entry<String, Integer>> findTopNCategories(Map<String, List<String>> categoryCoverage, int n) {

		//java8 
//		List<Map.Entry<String, Integer>> sortedResults = MapUtils.sortAsList(
//				
//				categoryCoverage.entrySet().stream().collect(Collectors.toMap(
//						Entry::getKey, (Map.Entry<String, List<String>> x) -> x.getValue().size())));
		//
		
		
		//Java 7
		Map<String,Integer> tc = new HashMap<String,Integer>();
		for(Entry<String, List<String>> entry : categoryCoverage.entrySet()){
			String key = entry.getKey();
			int value = entry.getValue().size();
			tc.put(key,value);
		}
		List<Map.Entry<String, Integer>> sortedResults = MapUtils.sortAsList(tc);
		//
		List<Entry<String, Integer>> subList = sortedResults.subList(0, (n < sortedResults.size()) ? n : sortedResults.size());
		return subList;
	}

	public Map<String, List<String>> removeCoveredWords(Map<String, List<String>> categoryCoverage, Set<String> coveredWords) {
		Map<String, List<String>> newMap = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> e : categoryCoverage.entrySet()) {
			List<String> uncoveredWords = new ArrayList<String>();
			for (String word : e.getValue()) {
				if (!coveredWords.contains(word))
					uncoveredWords.add(word);
			}
			if (!uncoveredWords.isEmpty()) {
				newMap.put(e.getKey(), uncoveredWords);
			}
		}
		return newMap;
	}

	public Map<String, List<String>> filterCategories(Map<String, List<String>> categoryCoverage, Set<String> uncoveredWords) {
		HashMap<String, List<String>> newCat = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> e : categoryCoverage.entrySet()) {
			boolean usefulCategory = false;
			String cat = e.getKey();
			List<String> wordsInCat = e.getValue();
			for (String word : uncoveredWords) {
				if (wordsInCat.contains(word))
					usefulCategory = true;
			}
			if (usefulCategory) {
				//java8
//				wordsInCat = wordsInCat.stream().filter((w) -> uncoveredWords.contains(w)).collect(Collectors.toList());
				//
				
				//java 7 
				ArrayList<String> temp = new ArrayList<String>();
				for(String c : wordsInCat){
					if (uncoveredWords.contains(c)){
						temp.add(c);
					}
				}
				wordsInCat = temp;
				//
				newCat.put(cat, wordsInCat);

			}
		}

		return newCat;

	}
	public Map<String, Integer> computeCoverage(Map<String, List<String>> categoryCoverage, int coverage, List<String> seeds) {
		double total = seeds.size();
		double min = .5;
		//java8 
//		List<Map.Entry<String, Integer>> sortedResults = MapUtils.sortAsList(categoryCoverage.entrySet().stream().
		//collect(Collectors.toMap(Entry::getKey, 
		//(Map.Entry<String, List<String>> x) -> x.getValue().size())));
		//
		
		//java7 
		Map<String,Integer> temp = new HashMap<String,Integer>();
		for(Entry<String, List<String>> entry : categoryCoverage.entrySet()){
			temp.put(entry.getKey(), entry.getValue().size());
		}
		List<Map.Entry<String, Integer>> sortedResults = MapUtils.sortAsList(temp);
		List<Entry<String, Integer>> subList = sortedResults.subList(0, Math.min(sortedResults.size(), coverage));
		Set<String> coveredWords = new HashSet<String>();
		Set<String> uncoveredWords = new HashSet<String>();

		// get covered words
		for (Entry<String, Integer> e : subList) {
			String categoryName = e.getKey();
			List<String> wordsForCategory = categoryCoverage.get(categoryName);
			coveredWords.addAll(wordsForCategory);
		}

		for (String word : seeds) {
			if (!coveredWords.contains(word))
				uncoveredWords.add(word);
		}

		Map<String, List<String>> uncoveredData = removeCoveredWords(categoryCoverage, coveredWords);
		List<Entry<String, Integer>> uncoveredCoverage = findTopNCategories(uncoveredData, 2);
		double leftUncovered = getNumWordsCovered(uncoveredCoverage, uncoveredData) + (double) coveredWords.size();
		Map<String, Integer> result = new HashMap<String, Integer>();
		if ((leftUncovered / total) >= min) {
			for (Entry<String, Integer> e : subList) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}
	

//	

	@SuppressWarnings("unchecked")
	public Map<String, Integer> findBestCategories(List<String> seeds) {
		Map<String, List<String>> categoryCoverage = new HashMap<String, List<String>>();

		for (String seedWord : seeds) {
			List<String> categories = QueryDBPedia.getCategories(seedWord);
			for (String category : categories) {
				List<String> oldCategories = categoryCoverage.get(category);
				if (oldCategories == null)
					oldCategories = new ArrayList<String>();
				oldCategories.add(seedWord);
				categoryCoverage.put(category, oldCategories);
			}
		}

		 return computeCoverage(categoryCoverage, 10, seeds);


	}

	



}
