package com.ibm.ecosystem.conext.web.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 *
 */
public class Main {
	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://localhost:8080/";

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in com.example package
		final ResourceConfig rc = new ResourceConfig().packages("com.ibm");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		 final HttpServer server = startServer();
		 System.out.println(String.format("Jersey app started with WADL available at "
		 + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
		 System.in.read();
		 server.stop();


//		 String f = "Census.txt";
//		 String inFile =
//		 "C:\\Users\\IBM_ADMIN\\project\\old\\data\\wikilm\\text\\" + f;
//		 String outFile =
//		 "C:\\Users\\IBM_ADMIN\\workspace\\ConExT\\src\\main\\resources\\" +
//		 f;
//		
//		 PrintWriter w = new PrintWriter(new FileWriter(new File(outFile)));
//		 String taggedText = StanfordAnnotator.getInstance().posTag(new
//		 String(Files.readAllBytes(Paths.get(inFile))));
//		 w.println(taggedText);
//		 w.flush();
		// w.close();
	}

	public static String getString(String file) {
		
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
			int ch;
			StringBuilder sb = new StringBuilder();
			while ((ch = is.read()) != -1)
				sb.append((char) ch);
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
