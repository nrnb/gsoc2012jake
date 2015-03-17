package org.pathvisio.facets.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import org.bridgedb.Xref;
import org.pathvisio.facets.gui.CSVDataServicePanel;
import org.pathvisio.facets.main.Utility;

import com.csvreader.CsvReader;

public class CSVDataService extends AbstractDataService implements DataService {
	private CsvReader reader;
	private String identifierHeader;
	private File file;

	public CSVDataService(File f) {
		this(f, null);
		panel = new CSVDataServicePanel(this);
	}
	/**
	 * @param f - file to associate with instance
	 * @param identifier - the header of the *SV file that represents the identifiers
	 */
	public CSVDataService(File f, String identifier) {
		super();
		file = f;
		try {
			reader = new CsvReader(new FileReader(f), Utility.getDelimiter(f));
			reader.readHeaders();
			attributes = reader.getHeaders();
			facets = new Facet[attributes.length];
			for(int i = 0; i < attributes.length; i++) {
				facets[i] = new Facet(this, attributes[i]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setIdentifierHeader(identifier);
	}
	/**
	 * This method assigns the model to a specific facet by reading the values from the file using a 
	 * class I found online called CSVReader
	 * The delimiter is determined by the extension of the file.  Currently supports TSV and CSV
	 * 
	 * @param f - the facet to set the model of
	 */
	public void setFacetModel(Facet f) {
		try {
			HashMap<String, HashSet<Xref>> map = new HashMap<String, HashSet<Xref>>();
			reader = new CsvReader(new FileReader(file), Utility.getDelimiter(file));
			reader.readHeaders();
			while(reader.readRecord()) {
				Xref recordXref = new Xref(reader.get(identifierHeader),f.getDataService().getIdentifierDataSource());
				Xref diagramXref = FacetManager.getInstance().getToPathwayIdentifier().get(recordXref);
				if(diagramXref != null) {
					Utility.add(map, reader.get(f.getAttribute()), diagramXref);
				}
				else {
					Utility.add(map, reader.get(f.getAttribute()), recordXref);
				}
			}
			f.setMap(map);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}  
	}
	/**
	 * This method assigns the model to a specific facet by reading the values from the InputStream using a 
	 * class I found online called CSVReader.  This method is meant to be used when you do not have a whole file
	 * but rather have a stream (like from the internet).
	 * 
	 * @param f - the facet to set the model of
	 * @param stream - an input stream in delimited value format
	 * @param delimeter - the delimiter to use (char not String)
	 */
	public static void setFacetModel(Facet f, InputStream stream, char delimeter  ) {
		try {
			HashMap<String, HashSet<Xref>> map = new HashMap<String, HashSet<Xref>>();
			CsvReader r = new CsvReader(new InputStreamReader(stream), delimeter);
			r.readHeaders();
			while(r.readRecord()) {
				String attr = r.get(1);
				Xref recordXref = new Xref(r.get(0),f.getDataService().getIdentifierDataSource());
				Xref diagramXref = FacetManager.getInstance().getToPathwayIdentifier().get(recordXref);
				if(diagramXref != null ) {
					Utility.add(map, attr, diagramXref);
				}
				else {
					Utility.add(map, attr, recordXref);
				}
			}
			f.setMap(map);
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}  
	}
	/**
	 * @return the CSV file associated with this instance
	 */
	public File getFile() {
		return file;
	}
	/**
	 * @return the headers of the csv
	 */
	public String[] getHeaders() {
		return attributes;
	}
	@Override
	public String getServiceName() {
		return file.getName();
	}

	/**
	 * @return the identifierHeader
	 */
	public String getIdentifierHeader() {
		return identifierHeader;
	}

	/**
	 * @param identifierHeader the identifierHeader to set
	 */
	public void setIdentifierHeader(String identifierHeader) {
		this.identifierHeader = identifierHeader;
	}
	
	@Override
	public boolean equals(Object o) {
		if( !(o instanceof CSVDataService)) {
			return false;
		}
		CSVDataService d = (CSVDataService) o; 
		return d.file.getAbsolutePath().equals(file.getAbsolutePath());
	}


}
