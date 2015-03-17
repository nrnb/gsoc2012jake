package org.pathvisio.facets.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.facets.gui.BiomartDataServicePanel;
import org.pathvisio.facets.main.Utility;
import org.pathvisio.gui.ProgressDialog;
import org.pathvisio.gui.SwingEngine;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.csvreader.CsvReader;
/**
 * a singleton DataService for accessing biomart
 * @author jakefried
 *
 */
public class BiomartDataService extends AbstractDataService {
	private static BiomartDataService instance = null;
	private static Collection<Xref> filter = new ArrayList<Xref>();
	private static Map<String, String> descriptions;
	private static String attributesSite = "http://www.biomart.org/biomart/martservice?type=attributes&dataset=";
	private static String dataset;
	private static String biomart = "http://www.biomart.org/biomart/martservice/result?query=" ;    

	protected BiomartDataService() {
		super();
		panel = BiomartDataServicePanel.getInstance(this);
		facets = new Facet[0];
		descriptions = new HashMap<String, String>();
		InputStreamReader isr = new InputStreamReader(Utility.class.getClassLoader().getResourceAsStream("resources/filtered_ensembl_attributes.csv"));
		CsvReader reader = new CsvReader(isr);
		try {
			reader.readHeaders();
			while(reader.readRecord()) {
				String attr = reader.get(0);
				String desc = reader.get(1);
				descriptions.put(attr, desc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Called when the dataservice is switched from inactive mode to active mode.
	 * returns false if cannot connect to biomart (else true)
	 */
	public boolean populateAttributes() {
		if(Utility.isInternetReachable(attributesSite + dataset)) {
			attributes = new String[descriptions.keySet().size()];
			facets = new Facet[attributes.length];
			descriptions.keySet().toArray(attributes);
			for(int i = 0; i < attributes.length; i++) {
				facets[i] =  new Facet(this, attributes[i]);
				facets[i].setDescription(getDescription(attributes[i]));
			}
			return true;
		}
		return false; // if internet is not available
	}

	/**
	 * helper method for generating proper xml queries for biomart
	 * 
	 * @param set - the dataset to use in the query
	 * @param attrs - the attributes to return in the tsv
	 * @param identifierFilters - the identifiers to get the attributes for
	 * @return a XMLDocument formattted correctly to send to biomart 
	 */
	public static Document createQuery(String set, Collection<String> attrs, Collection<String> identifierFilters ) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			// create doc
			Document query = docBuilder.newDocument();
			DOMImplementation domImpl = query.getImplementation();
			DocumentType doctype = domImpl.createDocumentType("Query", "", "");
			query.appendChild(doctype);
			Element root = query.createElement("Query");
			root.setAttribute("client", "true");
			root.setAttribute("processor", "TSV");
			root.setAttribute("limit", "-1");
			root.setAttribute("header", "1");
			query.appendChild(root);
			/* specify the dataset to use */
			Element dataset = query.createElement("Dataset");
			dataset.setAttribute("name", set);
			//dataset.setAttribute("config", "gene_ensembl_config");
			root.appendChild(dataset);
			/* filter to only the geneIDs we care about -- must be ensembl */
			Element filter = query.createElement("Filter");
			filter.setAttribute("name", "ensembl_gene_id");
			String identifiers = identifierFilters.toString().replaceAll("[\\[\\] ]", "");
			filter.setAttribute("value", identifiers);
			dataset.appendChild(filter);
			/* add attributes specified in app */
			Element gene_id = query.createElement("Attribute");
			gene_id.setAttribute("name", "ensembl_gene_id");
			dataset.appendChild(gene_id);
			for(String attr : attrs) {
				Element a = query.createElement("Attribute");
				a.setAttribute("name", attr);
				dataset.appendChild(a);
			}
			return query;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null; 
	}
	/**
	 * @param xml - the xml document to convert into a string
	 * @return the xml in String Format
	 */
	public static String docToString(Document xml) {
		LSSerializer ls =  ((DOMImplementationLS) xml.getImplementation()).createLSSerializer();
		LSOutput lsOut = ((DOMImplementationLS) xml.getImplementation()).createLSOutput();
		lsOut.setEncoding("UTF-8");
		StringWriter stringWriter = new StringWriter();
		lsOut.setCharacterStream(stringWriter);			
		ls.write(xml, lsOut);
		return stringWriter.toString();
	}
	/**
	 * @returns an input stream capable of being read by CSVDataService
	 * @param xml - the query to send to biomart
	 */
	public static InputStream getDataStream(Document xml) {
		try {
			String encodedXml = docToString(xml);
			encodedXml = URLEncoder.encode(encodedXml, "UTF-8"); // encode to url
			URL url = new URL(biomart + encodedXml);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			int code = urlc.getResponseCode();
			if(code != 200) {
				System.out.println(urlc.getResponseCode());
			}
			return urlc.getInputStream();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param f - The facet to set the model of
	 */
	@Override
	public void setFacetModel(final Facet f) {
		SwingEngine sengine = Utility.getFSP().getEngine();
		Organism org = sengine.getCurrentOrganism();
		DataSource.getByFullName("Ensembl " + org.shortName());
		final ProgressKeeper pk = new ProgressKeeper(filter.size());
		final ProgressDialog d = new ProgressDialog(JOptionPane.getFrameForComponent(sengine.getApplicationPanel()),
				"", pk, false, true);

		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				pk.setTaskName("Downloading Biomart Data");
				int i = 0;
				HashMap<String, HashSet<Xref>> map = new HashMap<String, HashSet<Xref>>();
				for(Xref xref : filter) {
						String attr = BiomartDataService.get(xref, f.getAttribute());
						Utility.add(map, attr, xref);
						pk.setProgress(i++);
				}
				f.setMap(map);
				pk.finished();
				return null;
			}
		};
		sw.execute();
		d.setVisible(true);
	}
	public static String get(String id, String attr) {
		return getAttribute(id, attr);
	}
	public static String get(Xref xref, String attr) {
		if(xref.getDataSource() == null) {
			return "blank";
		}
		return getAttribute(xref.getId(), attr);
	}
	public static String getAttribute(String id, String attr) {
		if(id.equals("") || id.equals("blank")) {
			return "blank";
		}
		ArrayList<String> attrs = new ArrayList<String>();
		ArrayList<String> filter = new ArrayList<String>();
		attrs.add(attr);
		filter.add(id);
		Document xml = createQuery(dataset, attrs, filter);
		InputStreamReader isr = new InputStreamReader(getDataStream(xml));
		CsvReader reader  = new CsvReader(isr, '\t');
		try {
			reader.readHeaders();
			reader.readRecord();
			return reader.get(1);
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		}
	}
	/**
	 * @returns false because it should not be removed
	 */
	public boolean isRemoveable() {
		return false;
	}
	/**
	 * @return the singleton instance
	 */
	public static BiomartDataService getInstance() {
		if(instance == null) {
			instance = new BiomartDataService();
		}
		return instance;
	}
	/**
	 * @param attr - the attribute to get the description for
	 * @return the biomart description of the attribute
	 */
	public String getDescription(String attr) {
		return descriptions.get(attr);
	}
	public String getDataset() {
		return dataset;
	}
	public File getFile() {
		return new File(getServiceName());
	}
	@Override
	public String getServiceName() {
		return toString();
	}
	@Override
	public String[] getAttributes() {
		if(attributes != null) {
			return attributes;
		}
		else {
			return new String[0];
		}
	}
	public void setFilter(Collection<Xref> f) {
		filter = f;
	}
	
	public void setDataset(String d) {
		dataset = d;
	}
	/* just picked a number. -- since its a singleton no point in generating a meaningful hashcode methods*/
	@Override
	public int hashCode() {
		return Integer.MAX_VALUE;
	}
	 //can only be equal if its the same instance because its a singleton
	public boolean equals(Object o) {
		return o == this;
	}
	
	@Override
	public void setIdentifierDataSource(DataSource d) {
		super.setIdentifierDataSource(d);
		setDataset(Utility.dataSourceToEnsemblDataSet(d));
	}
	
	public String toString() {
		return "Biomart Webservice";
	}

}
