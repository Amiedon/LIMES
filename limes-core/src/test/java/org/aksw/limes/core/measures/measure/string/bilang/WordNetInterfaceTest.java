package org.aksw.limes.core.measures.measure.string.bilang;

import static org.junit.Assert.*;

import org.junit.Test;

public class WordNetInterfaceTest {

  @Test
  public void testSimilarity() {
    WordNetInterface wn = new WordNetInterface("src/test/resources/WordNet-3.0");
    double eps = 0.00001;
    assertEquals(1.0, wn.computeWuPalmerSimilarity("dog", "dog"), eps);
    assertEquals(0.0, wn.computeWuPalmerSimilarity("cat", "between"), eps);
    assertEquals(1.0, wn.computeWuPalmerSimilarity("listen", "hear"), eps);
    assertTrue(wn.computeWuPalmerSimilarity("dog", "rocket") < wn.computeWuPalmerSimilarity("dog", "mammal"));
    assertTrue(wn.computeWuPalmerSimilarity("draw", "cook") > wn.computeWuPalmerSimilarity("enter", "sign"));
    assertTrue(wn.computeWuPalmerSimilarity("draw", "cook") < wn.computeWuPalmerSimilarity("draw", "paint"));
    System.out.println(wn.computeWuPalmerSimilarity("cat", "rocket"));
    System.out.println(wn.computeWuPalmerSimilarity("dog", "rocket"));
    System.out.println(wn.computeWuPalmerSimilarity("cat", "tree"));
    System.out.println(wn.computeWuPalmerSimilarity("dog", "tree"));
  }

}