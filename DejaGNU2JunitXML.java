/* Convert DejaGNU test result *.sum file to junit-xml style output, so
   that CI tools, such jenkins, can read them in.

   java -cp . DejaGNU2JunitXML your/dir/tool.sum

   it will save results in junit.xml.  */

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DejaGNU2JunitXML {

    BufferedReader bufferedReader;

    public DejaGNU2JunitXML (String sumFileName) throws Exception {
	bufferedReader = new BufferedReader(new FileReader(sumFileName));
    }

    public void convertToJunitXML () throws Exception {
	String line = null;

	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	// root elements
	Document doc = docBuilder.newDocument();
	Element eTestSuite = doc.createElement("testsuite");
	doc.appendChild (eTestSuite);

	while ((line = bufferedReader.readLine()) != null) {
	    StringTokenizer st = new StringTokenizer (line, ": ");

	    if (!st.hasMoreElements())
		continue;
	    String result = st.nextToken();

	    if (result == null)
		continue;

	    if (!result.equals ("PASS") && !result.equals ("XPASS") && !result.equals ("FAIL")
		&& !result.equals ("XFAIL") && !result.equals ("KFAIL"))
		continue;

	    if (!st.hasMoreElements())
		continue;
	    String testCase = st.nextToken();

	    if (testCase == null)
		continue;

	    String testMethod = line.substring (result.length () + testCase.length () + 4);


	    if (!result.equals ("PASS") && !result.equals ("KFAIL") && !result.equals ("XFAIL")) {
		Element eTestCase = doc.createElement("testcase");
		eTestSuite.appendChild(eTestCase);


		eTestCase.setAttribute("classname", testCase);
		eTestCase.setAttribute("name", testMethod);

		Element eFailure = doc.createElement("failure");

		eFailure.setAttribute("type", result);
		eFailure.appendChild(doc.createTextNode(""));
		eTestCase.appendChild(eFailure);
	    }
	}

	// write the content into xml file
	TransformerFactory transformerFactory = TransformerFactory.newInstance();

	try {
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new File("junit.xml"));

	    transformer.transform(source, result);

	} catch (TransformerException e) {
	    e.printStackTrace ();
	}


	bufferedReader.close ();
    }

    public static void main (String[] args) {

	System.out.println (args[0]);

	try {
	    DejaGNU2JunitXML d2jxml = new DejaGNU2JunitXML (args[0]);

	    d2jxml.convertToJunitXML ();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}