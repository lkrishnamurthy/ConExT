package com.ibm.ecosystem.conext.util;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


//Very hacky class 
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapUtils {

	

	
	//Sorts map by values
	public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
            
        });
      
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
      
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }
	

	
	//sorts map on values. Returns a list with the map entries, for ease of use. 
	public static List sortAsList(Map results){
		List list = new LinkedList(results.entrySet());
		     Collections.sort(list, new Comparator() {
		          public int compare(Object o1, Object o2) {
		               return ((Comparable) ((Map.Entry) (o2)).getValue())
		              .compareTo(((Map.Entry) (o1)).getValue());
		          }

		          
		     });
		     return list;
	}
	
	//sorts map on values. Returns a list with the map entries, for ease of use. 
	public static List sortAsListReversed(Map results){
		List list = new LinkedList(results.entrySet());
		     Collections.sort(list, new Comparator() {
		          public int compare(Object o1, Object o2) {
		               return ((Comparable) ((Map.Entry) (o1)).getValue())
		              .compareTo(((Map.Entry) (o2)).getValue());
		          }

		          
		     });
		     return list;
	}
}

