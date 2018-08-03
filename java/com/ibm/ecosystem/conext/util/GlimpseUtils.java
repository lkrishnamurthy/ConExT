package com.ibm.ecosystem.conext.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class GlimpseUtils {

	
	/**
	 * Loads seeds from a glimpse dictionary. You need Glimpse authentication to do this 
	 * @param dictionary the name of the dictionary
	 * @param username w3 username
	 * @param password w3 password
	 * 
	 * @return
	 */
	public static List<String> loadSeedsFromDictionary(String dictionary, String username, String password) {

		List<String> seeds = new ArrayList<String>();

		String login = username + ":" + password;

		String base64login = new String(org.apache.commons.codec.binary.Base64.encodeBase64(login.getBytes()));

		try {
			Document document = Jsoup.connect("http://littlemill.almaden.ibm.com/glimpse/api/dictionary/bnyname/" + URLEncoder.encode(dictionary) + "/seeds").header("Authorization", "Basic " + base64login).get();
			for (Element e : document.select("seed")) {
				seeds.add(e.text());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return seeds;
	}
}
