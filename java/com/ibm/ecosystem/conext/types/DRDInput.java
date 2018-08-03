package com.ibm.ecosystem.conext.types;

import java.util.Arrays;
import java.util.List;

import com.ibm.ecosystem.conext.util.ResourceUtils;

public class DRDInput {

	private  final ResourceUtils fileFinder = ResourceUtils.getInstance();
	private List<String> referenceDomains = Arrays.asList(
			fileFinder.getInstance().getTaggedWikipediaFile("Ambassador.txt"), 
			fileFinder.getInstance().getTaggedWikipediaFile("Census.txt"));
	private Integer gram;
	private String text;

	private boolean showPosTags = false;

	public boolean shouldShowPosTags() {
		return showPosTags;
	}
	public void setShowPosTags(boolean showPosTags) {
		this.showPosTags = showPosTags;
	}

	@Override
	public String toString() {
		return "DRDInput [text=" + text + ", referenceDomains=" + referenceDomains + ", gram=" + gram + ", showPosTags=" + showPosTags + ", taggedText=" + taggedText + "]";
	}

	private String taggedText;

	public String getTaggedText() {
		return taggedText;
	}
	public void setTaggedText(String taggedText) {
		this.taggedText = taggedText;
	}

	public Integer getGram() {
		return gram;
	}
	public void setGram(Integer gram) {
		this.gram = gram;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<String> getReferenceDomains() {
		return referenceDomains;
	}
	public void setReferenceDomains(List<String> referenceDomains) {
		this.referenceDomains = referenceDomains;
	}

	

}
