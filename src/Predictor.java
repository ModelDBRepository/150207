/*******************************************************************************
 *
 *  A Machine Learning Method for the Prediction of Receptor
 *  Activation in the Simulation of Synapses
 *  Copyright (C) 2013 J. Montes, E. Gomez, A. Merchan-Perez, J. DeFelipe,
 *                     J. M. Pe–a
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSink;

public class Predictor {

  public static void main(String[] args) {

    if (args.length != 6) {
      System.out.println("Arguments: <ML model file> <[AMPA]> <[T]> <Ls> <Hc> <E>");
      System.exit(1);
    }

    String modelFile = args[0];
    double cAMPA = Double.parseDouble(args[1]);
    double cT = Double.parseDouble(args[2]);
    double Ls = Double.parseDouble(args[3]);
    double Hc = Double.parseDouble(args[4]);
    double E = Double.parseDouble(args[5]);

    try {
      System.out.println("Loading the model...");

      Classifier cls = (Classifier) SerializationHelper.read(modelFile);

      System.out.println("Generating new data...");

      Attribute cAMPAatt = new Attribute("[AMPA]");
      Attribute cTatt = new Attribute("[T]");
      Attribute Lsatt = new Attribute("Ls");
      Attribute Hcatt = new Attribute("Hc");
      Attribute Eatt = new Attribute("E");
      Attribute timeatt = new Attribute("time");
      Attribute AMPAoatt = new Attribute("AMPA.O-ML");
      Attribute AMPAoFinalatt = new Attribute("AMPA.O-Final");

      FastVector attributes = new FastVector();
      attributes.addElement(cAMPAatt);
      attributes.addElement(cTatt);
      attributes.addElement(Lsatt);
      attributes.addElement(Hcatt);
      attributes.addElement(Eatt);
      attributes.addElement(timeatt);
      attributes.addElement(AMPAoatt);
      Instances targetData = new Instances("Target Dataset", attributes, 0);
      targetData.setClass(AMPAoatt);

      for (double i = 0.0; i <= 10000.0; i = i + 10) {
        double[] values = { cAMPA, cT, Ls, Hc, E, i, 0.0 };
        Instance row = new Instance(1.0, values);
        targetData.add(row);
      }

      System.out.println("Applying model...");

      FastVector rattributes = (FastVector) attributes.copy();
      rattributes.addElement(AMPAoFinalatt);
      Instances resultData = new Instances("Result Dataset", rattributes, 0);

      for (int i = 0; i < targetData.numInstances(); i++) {
        Instance row = targetData.instance(i);
        double rcAMPA = row.value(cAMPAatt);
        double rcT = row.value(cTatt);
        double rLs = row.value(Lsatt);
        double rHc = row.value(Hcatt);
        double rE = row.value(Eatt);
        double rtime = row.value(timeatt);
        double clsLabel = cls.classifyInstance(targetData.instance(i));
        double[] values = { rcAMPA, rcT, rLs, rHc, rE, rtime, clsLabel, 0.0 };
        Instance rrow = new Instance(1.0, values);
        resultData.add(rrow);
      }

      System.out.println("Fitting curve...");

      connectToR();
      String equation = "(p1*time^4+p2*time^3+p3*time^2+p4*time+p5)/(time^4+q1*time^3+q2*time^2+q3*time+q4)";
      Vector<String> constants = new Vector<String>();
      constants.add("p1");
      constants.add("p2");
      constants.add("p3");
      constants.add("p4");
      constants.add("p5");
      constants.add("q1");
      constants.add("q2");
      constants.add("q3");
      constants.add("q4");
      double[] parameters = fitCurveModel(equation, constants, resultData, AMPAoatt.name());
      disconnectFromR();

      if (parameters == null) {
        System.out.println("ERROR: Curve fitting failed!");
        System.exit(2);
      }

      for (int i = 0; i < resultData.numInstances(); i++) {

        Instance row = resultData.instance(i);

        double time = row.value(timeatt);
        double AMPAoPred = polRationalFunc(time / 1000.0, parameters);

        row.setValue(AMPAoFinalatt, AMPAoPred);

      }

      System.out.println("Saving results...");

      DataSink.write("result.csv", resultData);

      System.out.println("Done");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ---------------------------------------------------------------------------------
  // R RELATED METHODS
  // ---------------------------------------------------------------------------------

  private static Rengine re = null;

  private static String adaptToR(String str) {
    String res = str;
    if (str.equals("[AMPA]"))
      res = "cAMPA";
    else if (str.equals("[T]")) res = "cT";

    res = res.replace("-", "");
    res = res.replace(".", "");

    return res;
  }

  private static void connectToR() {

    if (re == null) {

      // Creating the R instance
      if (!Rengine.versionCheck()) {
        System.err.println("** Version mismatch - Java files don't match library version.");
        System.exit(1);
      }
      String[] rengineArgs = { "--no-save" };
      // re = new Rengine(rengineArgs, false, new RTextConsole());
      re = new Rengine(rengineArgs, false, null);

      if (!re.waitForR()) {
        System.out.println("Cannot load R");
        return;
      }
    }
  }

  private static void disconnectFromR() {
    re.end();
    re = null;
  }

  private static REXP runInR(String command) {
    try {
      // System.out.println("R> "+command);
      return re.eval(command);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String varList = null;

  private static Hashtable<String, double[]> instancesToTable(Instances data) {

    Hashtable<Attribute, ArrayList<Double>> result = new Hashtable<Attribute, ArrayList<Double>>();

    @SuppressWarnings("unchecked")
    Enumeration<Attribute> attributes = data.enumerateAttributes();
    while (attributes.hasMoreElements()) {
      result.put(attributes.nextElement(), new ArrayList<Double>());
    }

    for (int i = 0; i < data.numInstances(); i++) {
      Instance row = data.instance(i);
      Enumeration<Attribute> keys = result.keys();
      while (keys.hasMoreElements()) {
        Attribute key = keys.nextElement();
        ((ArrayList<Double>) result.get(key)).add(new Double(row.value(key)));
      }
    }

    Hashtable<String, double[]> finalResult = new Hashtable<String, double[]>();
    Enumeration<Attribute> keys = result.keys();
    while (keys.hasMoreElements()) {
      Attribute key = keys.nextElement();
      ArrayList<Double> values = result.get(key);
      double[] finalValues = new double[values.size()];
      for (int i = 0; i < values.size(); i++)
        finalValues[i] = values.get(i).doubleValue();
      finalResult.put(key.name(), finalValues);
    }

    return finalResult;
  }

  private static void loadDataIntoR(Instances data, String targetVar) {

    if (varList == null) {

      varList = "";

      String dataInit = "";
      Hashtable<String, double[]> problemData = instancesToTable(data);

      String[] keys = { "time", "AMPA.O-ML" };
      for (String key : keys) {
        double[] values = null;
        if (key.equals("time")) {
          double[] timeVals = problemData.get(key);
          values = new double[timeVals.length];
          for (int i = 0; i < timeVals.length; i++)
            values[i] = timeVals[i] / 1000.0;
        } else values = problemData.get(key);
        re.assign(adaptToR(key), values);
        dataInit = dataInit + adaptToR(key) + "=" + adaptToR(key) + ",";
        if (!key.equals(targetVar)) varList = varList + adaptToR(key) + ",";

      }
      varList = varList.substring(0, varList.length() - 1);
      dataInit = dataInit.substring(0, dataInit.length() - 1);
      runInR("data <- list(" + dataInit + ")");
    }
  }

  private static String fixEquation(String equation, Instances problemData) {

    String res = equation;

    for (int i = 0; i < problemData.numAttributes(); i++) {
      Attribute att = problemData.attribute(i);
      String key = att.name();
      res = res.replace(key, adaptToR(key));
    }

    return res;
  }

  private static double[] fitCurveModel(String equation, Vector<String> constants, Instances problemData, String targetVar) {

    loadDataIntoR(problemData, targetVar);

    equation = fixEquation(equation, problemData);

    double[] params = null;
    String modelVarList = varList;

    String constantList = "";
    String constantInit = "";
    for (int i = 0; i < constants.size(); i++) {
      constantList = constantList + constants.elementAt(i) + ",";
      constantInit = constantInit + constants.elementAt(i) + "=runif(1),";
    }
    if (constantList.length() > 0) {
      constantList = constantList.substring(0, constantList.length() - 1);
      constantInit = constantInit.substring(0, constantInit.length() - 1);
      modelVarList = varList + "," + constantList;
      runInR("ffit <- function (" + modelVarList + ") { " + equation + " }");
      REXP res = null;
      int attempt = 0;
      while ((res == null) && (attempt != 5)) {
        res = runInR("res <- nls(" + adaptToR(targetVar) + "~ffit(" + modelVarList + "), data, start=list(" + constantInit + "), control=list(minFactor=1e-8,tol=1e-6,maxiter=1000), algorithm = 'port')");
        attempt++;
      }
      if (res != null) {
        res = runInR("coef(res)");
        params = res.asDoubleArray();
      }
    }

    return params;
  }

  private static double polRationalFunc(double x, double[] params) {

    double p1 = params[0];
    double p2 = params[1];
    double p3 = params[2];
    double p4 = params[3];
    double p5 = params[4];
    double q1 = params[5];
    double q2 = params[6];
    double q3 = params[7];
    double q4 = params[8];

    return ((p1 * Math.pow(x, 4) + p2 * Math.pow(x, 3) + p3 * Math.pow(x, 2) + p4 * x + p5) / (Math.pow(x, 4) + q1 * Math.pow(x, 3) + q2 * Math.pow(x, 2) + q3 * x + q4));
  }
}
