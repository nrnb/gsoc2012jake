PathVisio Faceted Search Plugin
-------

The PathVisio Faceted Search plugin aims to 
help biologists understand complex pathways by providing a
filtering mechanism based on queries of imported data, and data from webservices


-----
Licensing information: This Software is licensed under the Apache Public License 
Libraries Used:
	1. csvreader from http://www.csvreader.com/java_csv.php
		License = LGPL
	2. alphanumeric compareTo from http://sanjaal.com/java/206/java-data-structure/alphanumeric-string-sorting-in-java-implementation/
		License = None
-----
FILES
-----

Here is an explanation of the directories in this project:
lib: contains the dependencies
src: contains the src code
	com.csvreader -> csvreader library
	org.pathvisio.facets.gui   -> gui code
		FacetedSearchPanel       -> the sidepanel that is always visible.  Contains facetpanels and the buttons for opening up the other dialogs
		DataServicePanel         -> the view for the DataServices (in in model) in ManageDataServiceDialog
		ManageDataServicesDialog -> the dialog that pops up when you click "Manage DataServices".  Displays all added DataServices and has the interface for adding/removing them 
		AddFacetDialog           -> the dialog that pops up when people click "Add Facet".  Lists the available facets to add and allows you to add them
		FacetPanel               -> the view for an individual facet that goes in the main panel
	org.pathvisio.facets.main  -> plugin initialisation code and static Utility class
		Activator 			-> Nothing to see here.  Regular activator
		FacetedSearchPlugin -> Not much to see here.  Sets up the help menu and starts everything.
		Utility				-> Singleton class full of mostly static methods and references to some 
	org.pathvisio.facets.model -> all of the dataservice classes and the facetmanager
		DataService         -> Interface that all of the dataservices must adhere to. Defines methods that must be defined
		AbstractDataService -> Implementation of many of the methods in dataservice
		Facet				-> The model for an individual facet. i.e. the map with attributes->qualifying xrefs etc
		FacetManager        -> organizer for the Facets.  Its main job though is to manages all of the conversions between xrefs
	


AUTHORS
-------

Jake Fried
Augustin Luna
Martijn van Iersel

CONTACT
-------

Email: 
augustin@mail.nih.gov or jakeyfried@gmail.com

Our official website: http://pathvisio.org/wiki/PathVisioFacetedSearchHelp
