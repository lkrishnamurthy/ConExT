package com.ibm.ecosystem.conext.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.ibm.ecosystem.conext.types.DRDInput;
import com.ibm.ecosystem.conext.util.MapUtils;
import com.ibm.ecosystem.conext.util.StopWordUtils;

public class PhraseFinder {

	Map<String, Integer> startsWith = new HashMap<String, Integer>();
	Map<String, Integer> endsWith = new HashMap<String, Integer>();

	Map<Integer, Map<String, Integer>> fgGrams = new HashMap<Integer, Map<String, Integer>>();
	Map<Integer, Double> fgGramCounts = new HashMap<Integer, Double>();

	Map<Integer, Map<String, Integer>> bgGrams = new HashMap<Integer, Map<String, Integer>>();
	Map<Integer, Double> bgGramCounts = new HashMap<Integer, Double>();

	private static List<String> unallowableTags = Arrays.asList("CD", "CC", "DT", "IN", "LRB", "RRB", "RB", "TO", "MD", "VBD", "VBZ", "VB", "VBG", "VBP", "WDT", "PRP", "WRB", "WP");
	private static List<String> unallowableWords = Arrays.asList("LCB", "LRB", "RRB", "RCB");
	static Pattern usableNgram = Pattern.compile("[a-zA-Z_0-9 ]*");

	
	
	// heuristics for filtering an ngram
	public static boolean ngramIsUsable(String text) {
		if (!usableNgram.matcher(text).matches()) {
			return false;
		}
		String[] words = text.split(" ");
		for (int i = 0; i < words.length; i++) {
			String wordWithTag = words[i];
			List<String> parts = Arrays.asList(wordWithTag.split("_"));

			if (parts.size() != 2) {
				return false;
			}

			String word = parts.get(0);
			String tag = parts.get(1);

			// Remove words less than 3 long
			if (word.length() < 3)
				return false;
			// Remove words with '(' or ')'
			if (unallowableWords.contains(word))
				return false;

			// PoS filters
			// remove certain tags that never make good phrases
			if (unallowableTags.contains(tag))
				return false;

			// remove adjective phrases
			if ((i + i >= words.length) && tag.equals("JJ"))
				return false;
		}

		boolean containsStopWord = StopWordUtils.getInstance().containsStopWords(text);
		if (containsStopWord) {
			return false;
		} else
			return true;
	}
	
	public void setup(String fgText, String bgText, int gram) {

		fgGrams = new HashMap<Integer, Map<String, Integer>>();
		fgGramCounts = new HashMap<Integer, Double>();
		bgGrams = new HashMap<Integer, Map<String, Integer>>();
		bgGramCounts = new HashMap<Integer, Double>();

		// setup
		for (int i = 1; i <= gram; i++) {
			Map<String, Integer> ngrams = NgramExtractor.countGrams(fgText, i);
			fgGrams.put(i, ngrams);
			// java8
			// fgGramCounts.put(i, (double) ngrams.values().stream().reduce(0,
			// (a, b) -> a + b));
			//
			// java7
			double sum = 0.0;
			for (Integer val : ngrams.values()) {
				sum = sum + val;
			}
			fgGramCounts.put(i, sum);

		}
		for (int i = 1; i <= gram; i++) {
			Map<String, Integer> ngrams = NgramExtractor.countGrams(bgText, i);
//			System.out.println(ngrams.keySet().size() + " bg");
			bgGrams.put(i, ngrams);

			// java8
			// bgGramCounts.put(i, (double) ngrams.values().stream().reduce(0,
			// (a, b) -> a + b));
			//

			// java7
			double sum = 0.0;
			for (Integer val : ngrams.values()) {
				sum = sum + val;
			}
			bgGramCounts.put(i, sum);
		}

	}
	
	
	private Map<String, Double> findNgrams(String fg, String bg, int grams) {
		setup(fg, bg, grams);
		Map<String, Double> ngramScores = new HashMap<String, Double>();

		Map<String, Integer> highestOrderFGGrams = fgGrams.get(grams);
		Double numHighestOrderFGGrams = fgGramCounts.get(grams);

		Map<String, Integer> highestOrderBgGrams = bgGrams.get(grams);
		Double numHighestOrderBGGrams = bgGramCounts.get(grams);

		Map<String, Integer> unigrams = fgGrams.get(1);
		Double unigramCounts = fgGramCounts.get(1);

		for (Entry<String, Integer> highestGramEntry : highestOrderFGGrams.entrySet()) {
			String ngram = highestGramEntry.getKey();
			if (ngramIsUsable(ngram)) {

				// compute Phrasiness
				double p = ((double) highestGramEntry.getValue()) / numHighestOrderFGGrams;
				String[] parts = ngram.split(" ");
				double q = 1;

				for (int i = 0; i < grams; i++) {
					Integer s = unigrams.get(parts[i]);
					if (s == null)
						s = 1;
					q *= ((double) s / unigramCounts);
				}
				double phraseness = p * Math.log(p / q);

				// compute informativenss
				Integer x = highestOrderBgGrams.get(ngram);
				if (x == null)
					x = 1;
				q = (((double) x) / ((double) numHighestOrderBGGrams));
				double informativeness = p * Math.log(p / q);

				// score them by combining
				double score = phraseness - informativeness;

				ngramScores.put(ngram, score);
			}
		}

		return ngramScores;
	}

	public double L(double p, int n, int k) {
		double pk = Math.pow(p, k);
		double v = pk * Math.pow((1 - p), (n - k));
		return v;
	}


	public static Map<String, Double> findNgrams(DRDInput rawData) {

		PhraseFinder p = new PhraseFinder();

		List<String> referenceDomains = rawData.getReferenceDomains();

		Map<String, Double> d1 = p.findNgrams(rawData.getTaggedText(), referenceDomains.get(0), rawData.getGram());
		Map<String, Double> d2 = p.findNgrams(rawData.getTaggedText(), referenceDomains.get(1), rawData.getGram());
		// java 7
		HashMap<String, Double> finalScores = new HashMap<String, Double>();
		for (Entry<String, Double> ngram : d1.entrySet()) {
			String key = ngram.getKey();
			Double d1Score = ngram.getValue();
			Double d2Score = d2.get(key);
			if (d2Score == null)
				d2Score = 0.0;
			finalScores.put(key, d1Score + d2Score);
		}
		for (Entry<String, Double> ngram : d2.entrySet()) {
			if (!finalScores.containsKey(ngram.getKey()))
				finalScores.put(ngram.getKey(), ngram.getValue());
		}

		@SuppressWarnings("unchecked")
		List<Map.Entry<String, Double>> sorted = (List<Map.Entry<String, Double>>) MapUtils.sortAsList(finalScores);
		Map<String, Double> topEntries = new LinkedHashMap<String, Double>();
		for (int i = 0; i < sorted.size(); i++) {
			Entry<String, Double> entry = sorted.get(i);
			String word = entry.getKey();
			Double newScore = entry.getValue();
			if (Math.log(newScore) >= -7.4)
				topEntries.put(word, newScore);
			else
				break;

		}
		return topEntries;
	}
	public void splitBigrams(HashMap<String, Integer> bigrams) {

		for (Entry<String, Integer> e : bigrams.entrySet()) {
			String[] parts = e.getKey().split(" ");
			Integer oldCount = startsWith.get(parts[0]);
			if (oldCount == null)
				oldCount = 0;
			startsWith.put(parts[0], oldCount + 1);
			oldCount = endsWith.get(parts[1]);
			if (oldCount == null)
				oldCount = 0;
			endsWith.put(parts[1], 0);
		}
	}
}
