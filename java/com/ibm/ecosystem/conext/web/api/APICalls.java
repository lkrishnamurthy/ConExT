package com.ibm.ecosystem.conext.web.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.ecosystem.conext.core.DBpediaClustering;
import com.ibm.ecosystem.conext.core.PhraseFinder;
import com.ibm.ecosystem.conext.types.CategorizationInput;
import com.ibm.ecosystem.conext.types.ClusterInput;
import com.ibm.ecosystem.conext.types.ClusterOutput;
import com.ibm.ecosystem.conext.types.DRDInput;
import com.ibm.ecosystem.conext.util.DBPediaUtils;
import com.ibm.ecosystem.conext.util.StanfordAnnotator;

/**
 * Contains the API calls that are currently supported
 * 
 * @author kaufmann
 */
@Path("/drd")
public class APICalls {

	@POST
	@Path("/extract")
	@Consumes("text/plain")
	@Produces(MediaType.APPLICATION_JSON)
	public String extract(String json) {
		Gson g = new GsonBuilder().serializeNulls().create();
		DRDInput i = g.fromJson(json, DRDInput.class);
		// Tag the text with POS tags
		String rawText = i.getText();
		String taggedText = StanfordAnnotator.getInstance().posTag(rawText);
		i.setTaggedText(taggedText);
		Map<String, Double> ngrams = PhraseFinder.findNgrams(i);
		if (!i.shouldShowPosTags()) {
			Map<String, Double> taglessWords = new HashMap<String, Double>();
			for (Entry<String, Double> e : ngrams.entrySet()) {
				taglessWords.put(e.getKey().replaceAll("_[a-zA-Z]*", ""), e.getValue());
			}
			ngrams = taglessWords;
		}
		return g.toJson(ngrams.keySet());
	}
	//
	@POST
	@Path("/cluster")
	@Consumes("text/plain")
	@Produces(MediaType.APPLICATION_JSON)
	public String cluster(String json) {
		Gson g = new Gson();
		ClusterInput wordlist = g.fromJson(json, ClusterInput.class);

		DBpediaClustering c = new DBpediaClustering();
		List<Set<String>> clusteredSeeds = c.cluster(wordlist.getWords());
		ClusterOutput clusterResponse = new ClusterOutput(wordlist, clusteredSeeds);
		// Tag the text with POS tags
		return g.toJson(clusterResponse.response());
	}

	@POST
	@Path("/categorize")
	@Consumes("text/plain")
	@Produces(MediaType.APPLICATION_JSON)
	public String categorize(String json) {
		Gson g = new Gson();
		CategorizationInput termList = g.fromJson(json, CategorizationInput.class);
		DBPediaUtils categorizer = new DBPediaUtils();
		Map<String, Integer> result = categorizer.findBestCategories(termList.getWords());
		return g.toJson(result);
	}

}