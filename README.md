Welcome to ConExT the Concept Extraction Toolkit

Here is the quick & dirty API documentation for the three capabilities:

================CONCEPT EXTRACTION================

This API extracts concept from text. 

[Location]: http://localhost:8080/drd/extract

[Documentation]: 

The API responds to POST that contains JSON formatted like the example below: 

{ 
"text" : "Assist the Patient Care Provider (PCP) to provide patient care. Through documentation and oral communication essential information shared with health care team members regarding patients care and requests. Duties preformed include blood glucose testing, patient transfers, obtain acute vitals signs, patient hygiene and other duties delegated by supervising RN.  Hospitality Attendant  Hospital food tray deliverer responsible for customer service in a timely and efficient manor with strict adherence to patient privacy policy. Also responsible for sanitation, cleaning and infection control practices that ensure Food and patient Safety. ", 
"gram" : 2,
"referenceDomains" : [ "2002 US Open – Men's Singles", "2econd Season" ],
"showPosTags" : true
}


"text" is the text from which ngrams will be extracted, The text in the sample above is extremely small, so the results aren't fantastic. I recommend trying with a larger sample, consisting of one (or several) documents.
"gram"  is the order of n-grams that will be extracted (any value can be used, but 1,2 or 3 work best).
"referenceDomains" [optional] is a list of the domains that determine what n-grams are 'statistically interesting' in "text". Currently you can only use 2 reference domains (this will eventually be unlimitied). This field is also optional, since most users won't need to configure it. I will eventually add documentation on the reference corpora available, and the ability for a user to add their own. 
"showPosTags" [optional] is a boolean flag that determines if the output will include part of speech tags (lives_VB) or just words (example: lives). It defaults to not showing PoS tags, since most users don't know what to do with them. 

The output of this is a JSON array containing  list of ngrams extracted from the input text. It looks like: 

["blood glucose","patient transfers","patients care",...]


================TERM CLUSTERING================

This API clusters a list of words using DBpedia. 

[Location]: http://localhost:8080/drd/cluster

[Documentation]: 


The API responds to POST that contains JSON formatted like the example below:

{ 
"words" : ["burgers","pizza","salad","cheerios","chocolate","ghirardelli chocolate","chocolates","beer","drinks","water","steak","whisky","brandy","tacos","beef","roast","pork","ham","apple","wine","fish","clam","muscles","milk","cheese","sandwich","orange","chicken","fish oil","sauce","beans","milkshake","tap water","apple pie" ] 
} 

"words"  is a list of terms (I will eventually changed the field list to "terms" to be consistent) that you want to be clustered using according to their DBpedia Lookup similarity. This functionality is very alpha (in other words, the clusters aren't always good) 

The output is a numbered list of clusters. cluster 0 represents words that couldn't be clustered (because they couldn't be found via DBpedia Lookup). 
This is slow right now because it has to reach out to DBpedia via the internet, but we could host a copy of it ourselves locally if it were necessary. 

================DBPEDIA CATEGORY COVERAGE================

This API finds the best DBpedia cateogries that cover a list of words.

[Location]: http://localhost:8080/drd/categorize

[Documentation]: 

The API responds to POST that contains JSON formatted like the example below:

{ 
"words" : ["streets","street","food","homeless","hygiene","resources","products","lives","employment" ] 
}


"words"  is a list of that you terms (again, sorry about the bad field name) that you want to categorize. Currently the API returns the top 4 DBpedia categories, and how many words they cover. An example return value is :


{"Homelessness":4,"Poverty":3,"Humanitarian aid":2,"Public health":2}


The categories are ranked in order of most to least covered words. Each pair represents a category, and the number of terms that category covers. Eventually I will change this to be a list, instead of separate JSON elements. 
