package org.aksw.limes.core.io.parser;

import org.apache.log4j.Logger;

/**
 * @author Mohamed Sherif <sherif@informatik.uni-leipzig.de>
 * @version Nov 12, 2015
 */
public class Parser implements IParser {
	private static final Logger logger = Logger.getLogger(Parser.class.getName());

	protected double threshold;
	private double threshold1;
    private double threshold2;
    protected double coef1;
    protected double coef2;
    protected String term1;
    protected String term2;
    protected String operator;
    protected String expression;
	

    public Parser(String input, double theta) {
        expression = input.replaceAll(" ", "");
        //expression = expression.toLowerCase();
        threshold = theta;
        getTerms();
    }

    
    /**
     * Tests whether an expression is atomic or not
     *
     * @return True if atomic, else false
     */
    @Override
    public boolean isAtomic() {
        //tests for operation labels. If they can be found, then our expression
        //is not atomic
        String copy = expression.toLowerCase();
        if (!copy.contains("max(") && !copy.contains("min(")
                && !copy.contains("and(") && !copy.contains("or(")
                && !copy.contains("add(") && !copy.contains("xor(")
                && !copy.contains("minus(") && !copy.contains("mult(")
                && !copy.contains("diff(")) {
            return true;
        } else {
            return false;
        }
    }


    public String getTerm1() {
        return term1;
    }

    public String getOperator() {
        return operator;
    }

    public String getTerm2() {
        return term2;
    }

    public double getCoef1() {
        coef1 = 1.0;
        if (term1.contains("*")) {
            String split[] = term1.split("\\*");
            try {
                coef1 = Double.parseDouble(split[0]);
                term1 = split[1];
            } catch (Exception e) {
                logger.warn("Error parsing " + term1 + " for coefficient <" + coef1 + ">");
                coef1 = 1;
                //e.printStackTrace();
                //logger.warn(e.getStackTrace()[0].toString());
            }
        }
        return coef1;
    }

    public double getCoef2() {
        coef2 = 1;
        //  System.out.println("Parsing "+term2);
        if (term2.contains("*")) {
            String split[] = term2.split("\\*");
            try {
                coef2 = Double.parseDouble(split[0]);
                term2 = split[1];
            } catch (Exception e) {
                coef2 = 1.0;
                logger.warn("Error parsing " + term2 + " for coefficient");
            }
        }
        return coef2;
    }

    /**
     * Splits the expression into two terms
     *
     * @return
     */
    public void getTerms() {
        if (!isAtomic()) {
            int counter = 1;
            boolean found = false;
            operator = expression.substring(0, expression.indexOf("("));
            String noOpExpression = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")"));
            //get terms
            //       System.out.println("Expression stripped from operator = "+noOpExpression);
            for (int i = 0; i < noOpExpression.length(); i++) {

                if (noOpExpression.charAt(i) == '(') {
                    counter++;
                    found = true;
                } else if (noOpExpression.charAt(i) == ')') {
                    counter--;
                    found = true;

                } else if (counter == 1 && found && noOpExpression.charAt(i) == ',') {
                    term1 = noOpExpression.substring(0, i);
                    term2 = noOpExpression.substring(i + 1);
                }
            }

            //        System.out.println("Term 1 = "+term1);
            //        System.out.println("Term 2 = "+term2);
            getCoef1();
            getCoef2();
            //now compute thresholds based on operations
            //first numeric operations
            if (operator.equalsIgnoreCase("MIN") || operator.equalsIgnoreCase("MAX")) {
                setThreshold1(threshold);
                setThreshold2(threshold);
            } else if (operator.equalsIgnoreCase("ADD")) {
                operator = "ADD";
                System.out.println("Coef1 = " + coef1 + ", Coef2 = " + coef2);
                setThreshold1((threshold - coef2) / coef1);
                setThreshold2((threshold - coef1) / coef2);
            } else if (operator.equalsIgnoreCase("MULT")) {
                operator = "MULT";
                setThreshold1(threshold / (coef2 * coef1));
                setThreshold2(getThreshold1());
            } //now set constraints. separator for sets and thresholds is |
            //thus one can write
            // AND(sim(a,b)|0.5,sim(b,d)|0.7)
            // and then set global threshold to the minimal value wanted
            else {
                int index = term1.lastIndexOf("|");
                String t;
                String set1 = term1.substring(0, index);
                //              System.out.println("Term1 filtered = "+set1);
                t = term1.substring(index + 1, term1.length());
                //              System.out.println("Term = "+set1+", filter = "+t);
                setThreshold1(Double.parseDouble(t));
                term1 = set1;

                index = term2.lastIndexOf("|");
                String set2 = term2.substring(0, index);
//                System.out.println("Term2 filtered = "+set2);
                t = term2.substring(index + 1, term2.length());
//                System.out.println("Term = "+set2+", filter = "+t);
                setThreshold2(Double.parseDouble(t));
                term2 = set2;
            }
        }//atomic
        else {
            operator = expression.substring(0, expression.indexOf("("));
            String noOpExpression = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")"));
            String split[] = noOpExpression.split(",");
            term1 = split[0];
            term2 = split[1];
        }
    }

    public static void testParsing(String s, double threshold) {
        Parser p = new Parser(s, threshold);
        if (p.isAtomic()) {
            System.out.println("-->" + s + " with threshold " + threshold + " will be carried out.");
        } else {
            testParsing(p.term1, p.getThreshold1());
            testParsing(p.term2, p.getThreshold2());
//            System.out.println("--> <" + p.op + "> will be carried out on " + p.term1 + " and " + p.term2 + " with "
//                    + "threshold " + threshold);
        }
    }


	public double getThreshold2() {
		return threshold2;
	}


	public void setThreshold2(double threshold2) {
		this.threshold2 = threshold2;
	}


	public double getThreshold1() {
		return threshold1;
	}


	public void setThreshold1(double threshold1) {
		this.threshold1 = threshold1;
	}

//    public static void main(String args[]) {
//        //Parser p = new Parser("max(add(0.3*sim(a,b),0.3*sim(c,d)), min(sim3(a,b), sim2(c,d)))", 0.9);
////        Parser.testParsing("MULT(0.3*sim(c,d), 0.7*sim(a,b))", 0.9);
//        //Parser.testParsing("max(add(0.5*sim(a,b),0.5*sim(c,d)), min(sim3(a,b), sim2(c,d)))", 0.9);
//        //Parser p = new Parser("sim(a.c, b.d)", 0.9);
////        System.out.println(p.getTerm1());
////        System.out.println(p.getTerm2());
////        System.out.println(p.op);
//        //Parser.testParsing("AND(sim1(a,b)|0.7, sim2(b,d)|0.9)", 0.9);
////        System.out.println(Double.parseDouble("1.0"));
//        String m = "levenshtein(x.http://www.okkam.org/ontology_person1.owl#surname, y.http://www.okkam.org/ontology_person2.owl#surname)";
//        m = "OR(A|0.8,B|0.7)";
//        m = "MAX(trigrams(x.skos:prefLabel,y.rdfs:label),trigrams(x.osnp:valueLabel, y.rdfs:label))";
//        Parser p = new Parser(m, 0.5);
//
//        System.out.println(p.isAtomic());
//        Parser.testParsing(m, 0.5d);
//
////    	System.out.println(m.contains("."));
//        System.out.println(p.op);
//        System.out.println(p.term1);
//        System.out.println(p.term2);
//    }

}