package org.aksw.limes.core.measures.measure.string.bilang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for the bilingual WordNet based semantic similarity metric {@link BilangWordNetWuPalmerMeasure}.
 * As expected, we see for example, that "hund" (German) and "mammal" (English) are more similar to each other
 * than "hund" (German) and "rocket" (English)
 * @author Swante Scholz
 */
public class BilangWordNetWuPalmerMeasureTest {
    
    @Test
    public void testSimilarity() {
        WordNetInterface wn = new WordNetInterface(WordNetInterface.DEFAULT_WORDNET_HOME);
        BilangDictionary dictionary = new BilangDictionary(
            BilangDictionary.DEFAULT_DICTIONARY_PATH);
        BilangWordNetWuPalmerMeasure measure = new BilangWordNetWuPalmerMeasure(wn, dictionary);
        double eps = 0.00001;
        System.out.println(
            measure.getSimilarity("hund", "cat") + " " + measure.getSimilarity("katze", "dog"));
        System.out.println(measure.getSimilarity("mammal", "hund") + " " + measure
            .getSimilarity("hund", "rocket"));
        System.out.println(measure.getSimilarity("car", "rakete") + " " + measure
            .getSimilarity("football", "universe"));
        assertEquals(0.0, measure.getSimilarity("cat", "cat"),
            eps);  // because they should be in different languages, the score here is expected to be zero
        assertEquals(0.0, measure.getSimilarity("hund", "hund"), eps);
        assertEquals(1.0, measure.getSimilarity("dog", "hund"), eps);
        assertEquals(1.0, measure.getSimilarity("katze", "cat"), eps);
        assertEquals(measure.getSimilarity("hund", "cat"), measure.getSimilarity("katze", "dog"),
            eps);
        assertTrue(
            measure.getSimilarity("mammal", "hund") > measure.getSimilarity("hund", "rocket"));
        assertTrue(
            measure.getSimilarity("car", "rakete") > measure.getSimilarity("football", "universe"));
    }
    
}