package org.aksw.limes.core.measures.measure.string.bilang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.apache.commons.math3.util.Pair;
import weka.Run;

/**
 * saves the word vectors (embeddings) for each word from potentially multiple languages
 */
public class WordEmbeddings {

  /**
   * A simple vector class with doubles to do some arithmetic
   */
  public class Vectord {

    public final double[] data;
    public final int size;

    public Vectord(double[] data) {
      this.data = data;
      this.size = data.length;
    }

    public Vectord(int size) {
      this.size = size;
      this.data = new double[size];
    }

    public Vectord plus(Vectord other) {
      checkLength(other);
      Vectord result = new Vectord(size);
      for (int i = 0; i < size; i++) {
        result.data[i] = this.data[i] + other.data[i];
      }
      return result;
    }

    public Vectord minus(Vectord other) {
      checkLength(other);
      Vectord result = new Vectord(size);
      for (int i = 0; i < size; i++) {
        result.data[i] = this.data[i] - other.data[i];
      }
      return result;
    }

    /**
     * @param other the other vector
     * @return the dot product of this and the other vector
     */
    public double dot(Vectord other) {
      checkLength(other);
      double result = 0.0;
      for (int i = 0; i < size; i++) {
        result += this.data[i] * other.data[i];
      }
      return result;
    }

    /**
     * @return the euclidean 2-norm
     */
    public double norm() {
      return Math.sqrt(normSq());
    }

    /**
     * @return the squared euclidean 2-norm
     */
    public double normSq() {
      double result = 0.0;
      for (int i = 0; i < size; i++) {
        result += data[i] * data[i];
      }
      return result;
    }

    public Vectord times(double factor) {
      Vectord result = new Vectord(size);
      for (int i = 0; i < size; i++) {
        result.data[i] = this.data[i] * factor;
      }
      return result;
    }

    public Vectord divide(double divider) {
      return times(1.0 / divider);
    }

    /**
     * @param other the other vector
     * @return the cosine similarity between this and the other vector: (A . B) / (||A||*||B||)
     */
    public double cosineSimilarity(Vectord other) {
      return dot(other) / Math.sqrt(this.normSq() * other.normSq());
    }

    private void checkLength(Vectord other) {
      if (this.size != other.size) {
        throw new RuntimeException("Vector lengths do not match!");
      }
    }
  }

  private HashMap<String, Vectord> words2vectors = new HashMap<>();
  private int dimensions = -1;

  public WordEmbeddings() {
  }

  public boolean contains(String word) {
    return words2vectors.containsKey(word);
  }

  public Vectord getWordVector(String word) {
    if (!contains(word)) {
      throw new RuntimeException("there is no embedding for word " + word);
    }
    return words2vectors.get(word);
  }

  /**
   * @param vector the source vector
   * @param n the number of neighbors to return
   * @return the n closest words to the given vector, including their cosine similarities to it.
   */
  public List<Pair<String, Double>> computeNNearestWords(Vectord vector, int n) {
    ArrayList<Pair<String, Double>> result = new ArrayList<>();
    words2vectors.forEach((key, value) ->
        result.add(new Pair<>(key, vector.cosineSimilarity(value))));
    result.sort((a,b)->b.getValue().compareTo(a.getValue()));
    return result.subList(0, n);
  }

  /**
   * adds new word vector, checks if the number of dimensions fit to other word vectors
   * that are already here.
   * if the same word is already present (perhaps for another language), the new word
   * vector for it is the average of the two
   * @param word new word
   * @param vector corresponding word vector
   */
  public void addWordVector(String word, Vectord vector) {
    if (words2vectors.size() == 0) {
      dimensions = vector.size;
    } else {
      if (vector.size != dimensions) {
        throw new RuntimeException("Bad number of dimensions of new word vector.");
      }
    }
    if (words2vectors.containsKey(word)) {
      Vectord oldVector = words2vectors.get(word);
      words2vectors.put(word, oldVector.plus(vector).times(0.5));
    } else {
      words2vectors.put(word, vector);
    }
  }

  /**
   * the language of the two words does not matter if the embeddings are from
   * bilingual training
   * @param word1
   * @param word2
   * @return the cosine similarity for these two words
   */
  public double getCosineSimilarityForWords(String word1, String word2) {
    Vectord vector1 = getWordVector(word1);
    Vectord vector2 = getWordVector(word2);
    return vector1.cosineSimilarity(vector2);
  }

  /**
   * reads two files from "Bilingual Word Representations with Monolingual Quality in Mind"
   * one english and one german one (e.g. "unsup.40.en" and "unsup.40.de")
   * the first line of the files is ignored
   *
   * @param basepath (e.g. "foo/bar/unsup.40"; ".en" and ".de" is added automatically)
   */
  public void readBilangDataFiles(String basepath) {
    readDataFile(basepath + ".en");
    readDataFile(basepath + ".de");
  }

  private void readDataFile(String filepath) {
    try {
      Stream<String> lines = Files.lines(Paths.get(filepath));
      lines.forEach(line -> {
        String[] parts = line.split(" ");
        if (parts.length > 2) {
          double[] data = new double[parts.length - 1];
          for (int i = 0; i < data.length; i++) {
            data[i] = Double.parseDouble(parts[i + 1]);
          }
          addWordVector(parts[0], new Vectord(data));
        }
      });
      lines.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
