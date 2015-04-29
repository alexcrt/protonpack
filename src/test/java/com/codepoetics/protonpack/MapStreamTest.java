/*
 * Author: 		Alexis Cartier <alexcrt>
 * Date :  		27 déc. 2014
*/

package com.codepoetics.protonpack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.codepoetics.protonpack.maps.MapStream;
import org.junit.Before;
import org.junit.Test;

public class MapStreamTest {
    
    private MapStream<String, Integer> mapStream;
    
    @Before
    public void setUp() {
        mapStream = MapStream.of("John", 1, "Alice", 2);
    }
    
    @Test
    public void testMapKeys() {
        Map<String, Integer> map = mapStream.mapKeys(x -> x.substring(2)).collect();
        assertTrue(map.containsKey("hn"));
        assertEquals(Integer.valueOf(1), map.get("hn"));
    }
    
    @Test
    public void testMapValues() {
        Map<String, Integer> map = mapStream.mapValues(x -> x + 5).collect();
        assertEquals(Integer.valueOf(6), map.get("John"));
    }
    
    @Test
    public void testMapEntries() {
        Map<String, Integer> map = mapStream.mapEntries(x -> x.concat(" Doe"), i -> i % 2).collect();
        assertEquals(Integer.valueOf(1), map.get("John Doe"));
        assertEquals(Integer.valueOf(0), map.get("Alice Doe"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testFailMergeKeys() {
       mapStream.mapKeys(x -> Character.isUpperCase(x.charAt(0))).collect();
    } 
    
    @Test
    public void testMergeKeys() {
        Map<Boolean, List<Integer>> map = mapStream.mapKeys(x -> Character.isUpperCase(x.charAt(0))).mergeKeys().collect();
        assertEquals(Arrays.asList(1, 2), map.get(true));
        assertEquals(null, map.get(false));
    }
    
    @Test
    public void testMergeKeysWithBinaryFunction() {
        Map<Boolean, Integer> map = mapStream.mapKeys(x -> Character.isUpperCase(x.charAt(0))).mergeKeys(Integer::sum).collect();
        assertEquals(Integer.valueOf(3), map.get(true));
        assertEquals(null, map.get(false));
    }
    
    @Test
    public void testReverseMapping() {
        Map<Integer, String> map = mapStream.inverseMapping().collect();
        assertEquals("John", map.get(1));
        assertEquals("Alice", map.get(2));
        
        map.put(3, "John");
        
        Map<String, Integer> mapReversed = MapStream.of(map).inverseMapping().collect(Integer::sum);
        assertEquals(Integer.valueOf(4), mapReversed.get("John"));
        assertEquals(Integer.valueOf(2), mapReversed.get("Alice"));
    }


    @Test
    public void testEmptyMapStream() {
        MapStream<String, Integer> mapStream = MapStream.emptyMapStream();
        assertEquals(0L, mapStream.count());
    }

    @Test
    public void testCountValue() {

        Map<String, Integer> map = new HashMap<>();
        for(int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), i);
        }

        for(int i = 0; i < 100; i++) {
            assertEquals(1L, MapStream.of(map).countValue(i));
        }

        assertEquals(11L, MapStream.of(map).countKeys(k -> k.startsWith("1")));
        assertEquals(1L, MapStream.of(map).countKeys(k -> k.startsWith("0")));
        assertEquals(0L, MapStream.of(map).countKeys(k -> k.startsWith("bob")));


        assertEquals(50L, MapStream.of(map).countValues(v -> v % 2 == 0));
        assertEquals(50L, MapStream.of(map).countValues(v -> v % 2 == 1));
        assertEquals(10L, MapStream.of(map).countValues(v -> v < 10));
        assertEquals(30L, MapStream.of(map).countValues(v -> v >= 20 && v < 50));
    }
}
