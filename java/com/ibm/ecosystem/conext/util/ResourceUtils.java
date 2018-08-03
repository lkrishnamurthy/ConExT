package com.ibm.ecosystem.conext.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
public class ResourceUtils {

	private static ResourceUtils self = null;
	private ResourceUtils() {
	}

	public static ResourceUtils getInstance() {
		if (self == null)
			self = new ResourceUtils();
		return self;
	}

	public String getTaggedWikipediaFile(String file) {
		InputStream is = getClass().getResourceAsStream("/" + file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line = reader.readLine();
			return line;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void tagAndSaveWikipediaArticleAsResource(String name, String rawTextDir, String outputDir) {
		try {
//			String f = "Census.txt";
//			String inFile = "C:\\Users\\IBM_ADMIN\\project\\old\\data\\wikilm\\text\\" + f;
//			String outFile = "C:\\Users\\IBM_ADMIN\\workspace\\ConExT\\src\\main\\resources\\" + f;
			String inFile = rawTextDir + name;
			String outFile = outputDir + name;

			PrintWriter w = new PrintWriter(new FileWriter(new File(outFile)));
			String taggedText = StanfordAnnotator.getInstance().posTag(new String(Files.readAllBytes(Paths.get(inFile))));
			w.println(taggedText);
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}