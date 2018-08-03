package com.ibm.ecosystem.conext.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class QueryDBPedia {

	public static Document readDocumentFromUrl(String url) {
		Document doc = null;
		try {
			InputStream is = new URL(url).openStream();
			BufferedReader b = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = "";
			try {
				while ((line =b.readLine()) != null){
					sb.append(line + " \n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			doc = Jsoup.parse(sb.toString(), "", Parser.xmlParser());

			return doc;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}

	public static List<String> getClasses(Document doc) {
		List<String> classes = new ArrayList<String>();
		for (Element e : doc.select("classes class label")) {
			classes.add(e.text());
		}
		return classes;
	}

	public static List<String> getCategories(String queryWord) {
		Document doc = queryWord(queryWord);
		List<String> classes = new ArrayList<String>();
		for (Element e : doc.select("categories category label")) {
			classes.add(e.text());
		}
		return classes;
	}

	public static Map<String, List<String>> getDisambiugatedCategories(String queryWord) {
		Document doc = queryWord(queryWord);
		Map<String, List<String>> categoriesByClass = new HashMap<String, List<String>>();
		Elements senses = doc.select("ArrayOfResult > result");
		for (Element sense : senses) {
			String label = queryWord + "[" + sense.select("result > label").text() + "]";

			List<String> classes = new ArrayList<String>();
			for (Element e : sense.select("result > categories > category > label")) {
				classes.add(e.text());
			}
			categoriesByClass.put(label, classes);
		}

		return categoriesByClass;
	}

	public static List<String> getCategories(Document doc) {
		List<String> classes = new ArrayList<String>();
		for (Element e : doc.select("categories category label")) {
			classes.add(e.text());
		}
		return classes;
	}

	public static Document queryWord(String word) {
		return readDocumentFromUrl("http://lookup.dbpedia.org/api/search/KeywordSearch?QueryString=" + URLEncoder.encode(word));
	}

	public static void printResults(Map<String, Map<String, Integer>> dictMap, String outputFile) {
		try {
			PrintWriter w = new PrintWriter(new FileWriter(new File(outputFile)));
			for (Entry<String, Map<String, Integer>> e : dictMap.entrySet()) {
				String dictName = e.getKey();
				Map<String, Integer> categories = e.getValue();
				w.println("****" + dictName + "****");
				for (Entry<String, Integer> topCat : categories.entrySet()) {
					w.println("\t" + topCat);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printResults2(Map<String, Map<String, Integer>> dictMap, String outputFile, Map<String, List<String>> glimpseData) {
		try {
			PrintWriter w = new PrintWriter(new FileWriter(new File(outputFile)));
			for (Entry<String, Map<String, Integer>> e : dictMap.entrySet()) {
				String dictName = e.getKey();
				Map<String, Integer> categories = e.getValue();
				double total = (double) glimpseData.get(dictName).size();
				w.println("****" + dictName + " " + total + "****");
				int counter = 0;
				for (Entry<String, Integer> topCat : categories.entrySet()) {
					if (++counter > 5)
						break;
					double score = (double) topCat.getValue() / total;
					if (score > .01)
						w.println("\t" + topCat.getKey() + " " + score);
				}
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> querySPARQL(List<String> articles) {
		List<String> categories = new ArrayList<String>();
		String baseURL = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=";
		String query = "select distinct ?category where { \n" + "?category ^(dcterms:subject/(skos:broader{0,1})) \n";
		for (int i = 0; i < articles.size(); i++) {
			String article = articles.get(i).replaceAll("[^a-zA-Z0-9 ]*","");
				query += "\t\t dbpedia:" + article.replaceAll(" ", "_");
				if (i + 1 != articles.size())
					query += ",\n";
				else
					query += "\n";
			
		}
		query = query + "\n}";
		baseURL = baseURL + URLEncoder.encode(query) + "&format=text%2Fhtml&timeout=30000&debug=on";
		try {
			Document doc = Jsoup.connect(baseURL).get();
			Pattern p = Pattern.compile("http://dbpedia.org/resource/Category:([a-zA-Z_]*)");
			Matcher m = p.matcher(doc.text());
			while (m.find())
				categories.add(m.group(1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return categories;
	}

	public static List<String> banks = Arrays.asList("jpmorgan chase", "jpmorgan", "jp morgan", "chase", "bank of america", "bofa", "b of a", "citi", "citigroup", "citi group", "wells fargo", "wf", "wellsfargo", "goldman sachs", "morgan stanley", "bancorp", "bank of new york", "hsbc",
			"capital one", "pnc", "bank", "state street");
}
