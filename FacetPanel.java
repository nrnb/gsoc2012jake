package org.pathvisio.facets.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bridgedb.Xref;
import org.pathvisio.facets.main.Utility;
import org.pathvisio.facets.model.Facet;


public class FacetPanel extends JPanel {
	private static String[] OperatorModel = {"=", ">","<"};
	private static final int MAX_HEIGHT_STRING = 240;
	private static final int MAX_HEIGHT_NUMERIC = 120;
	
	private JComboBox operator;
	private FacetedSearchPanel facetedSearchPanel;
	private Facet facet;
	private JLabel propertyLbl;
	private JTextField value;
	private JScrollPane valuesScrollPane;
	private JPanel valuesPanel;
	private JPanel propertyHeader;
	private JButton delete;
	private HashSet<Xref> selectedGeneIds;
	private HashSet<Xref> left;


	public FacetPanel(FacetedSearchPanel facetedSearchPanel, Facet facet) {
		this.facetedSearchPanel = facetedSearchPanel;
		this.facet = facet;
		selectedGeneIds = new HashSet<Xref>();
		initGUI();
		initValues();
	}
	private void initValues() {
		if(facet.numeric()) {
			facet.updateNumericData((String) operator.getSelectedItem(), Double.parseDouble(value.getText()));
			JToggleButton trues = new JToggleButton("True    - (" + facet.getTrueNum() + ")");
			JToggleButton falses = new JToggleButton("False   - (" + facet.getFalseNum() + ")");
			JToggleButton errors = new JToggleButton("Error - (" + facet.getOtherNum() + ")");
			trues.addActionListener(new ToggleButtonActionListeners());
			trues.setName("trues");
			falses.addActionListener(new ToggleButtonActionListeners());
			falses.setName("falses");
			errors.addActionListener(new ToggleButtonActionListeners());
			errors.setName("errors");
			valuesPanel.add(trues);
			valuesPanel.add(falses);
			valuesPanel.add(errors);
		}
		else {
			Map<String, HashSet<Xref>> map = facet.getMap();
			for(String property : map.keySet()) {
				JToggleButton btn = new JToggleButton(property + " - (" + map.get(property).size() + ")");
				btn.addActionListener(new ToggleButtonActionListeners());
				btn.setName(property);
				valuesPanel.add(btn);
			}
		}
		propertyLbl.setText(facet.getAttribute() + ": " + valuesPanel.getComponents().length + " choices");
	}
	/**
	 * called whenever a recalculation of the numeric counts is neccessary because of changed input
	 */;
	 public void updateValues() {
		 //clear current geneIds
		 selectedGeneIds.clear();
		 JToggleButton trues = (JToggleButton) valuesPanel.getComponent(0);
		 JToggleButton falses = (JToggleButton) valuesPanel.getComponent(1);
		 JToggleButton errors = (JToggleButton) valuesPanel.getComponent(2);
		 trues.setText( "True   - (" + Utility.getNumberOfSameElements(Utility.toPathway(facet.getTrues()),  left) + ")");
		 falses.setText("False  - (" + Utility.getNumberOfSameElements(Utility.toPathway(facet.getFalses()), left) + ")");
		 errors.setText("Error  - (" + Utility.getNumberOfSameElements(Utility.toPathway(facet.getErrors()), left) + ")");
		 
		 revalidate();
		 repaint();
	 }
	/**
	 * deselects all of the toggelbuttons
	 */
	 public void deselectAllButtons() {
		 for(Component c : valuesPanel.getComponents()) {
			 ((JToggleButton) c).setSelected(false);
		 }
	 }
	 /**
	  * called whenever a recalculation of the counts is neccessary because of other filter modifications
	  */
	 public void updateCounts(HashSet<Xref> left) {
		 this.left = left;
		 int visibleCount;
		 if(facet.numeric()) {
			 visibleCount = 0;
			 JToggleButton trues = (JToggleButton) valuesPanel.getComponent(0);
			 JToggleButton falses = (JToggleButton) valuesPanel.getComponent(1);
			 JToggleButton errors = (JToggleButton) valuesPanel.getComponent(2);
			 int t = Utility.getNumberOfSameElements(Utility.toPathway(facet.getTrues()),  left);
			 int f = Utility.getNumberOfSameElements(Utility.toPathway(facet.getFalses()), left);
			 int e = Utility.getNumberOfSameElements(Utility.toPathway(facet.getErrors()), left);
			 trues.setText( "True   - (" + t + ")");
			 falses.setText("False  - (" + f + ")");
			 errors.setText("Error  - (" + e + ")");
			 if(t > 0) { visibleCount++; }
			 if(f > 0) { visibleCount++; }
			 if(e > 0) { visibleCount++; }
		 } 
		 else {
			 visibleCount = 0;
			 for(Component c : valuesPanel.getComponents()) {
				 JToggleButton btn = (JToggleButton) c;
				 int count = Utility.getNumberOfSameElements(Utility.toPathway(facet.getMap().get(btn.getName())),left);
				 btn.setText(btn.getName() + " - (" + count + ")");
				 boolean visible = shouldBeVisible(btn);
				 if(visible) {
					 visibleCount++;
				 }
				 else {
					 if(btn.isSelected()) {
						 btn.setSelected(false);
					 }
				 }
				 btn.setVisible(visible);
			 }
		 }
		 propertyLbl.setText(facet.getAttribute() + ": " + visibleCount + " choices");
		 revalidate();
		 repaint();
	 }

	 private void filterChoices() {
		 for(Component btn : valuesPanel.getComponents()) {
			 btn.setVisible(shouldBeVisible(btn));
			 if(!btn.isVisible()) {
				((JToggleButton) btn).setSelected(false);
			 }
		 }
		 repaint();
	 }

	 /**
	  * @param c - component to check
	  * @return true if component should be visible based on the filter and hashset left
	  */
	 private boolean shouldBeVisible(Component c) {
		 String filter = value.getText().toLowerCase();
		 Collection<Xref> xrefs = facet.getMap().get(c.getName());
		 if(Utility.getActivePathway() != null) {
			 xrefs = Utility.toPathway(xrefs);
		 }
		 return (filter.equals("") || c.getName().toLowerCase().contains(filter)) && 
				 (Utility.getNumberOfSameElements(xrefs,left) > 0);
	 }
	 
	 private void initGUI() {
		 //set layout manager to boxlayout to arrange them horizontally (like flowlayout)
		 setLayout(new BorderLayout());
		 //init vars
		 value = new JTextField();
		 operator = new JComboBox(new DefaultComboBoxModel(OperatorModel));
		 propertyLbl = new JLabel();
		 delete = new JButton("X");
		 delete.setForeground(Color.RED);
		 propertyHeader = new JPanel();
		 propertyHeader.setLayout(new BoxLayout(propertyHeader, BoxLayout.LINE_AXIS));
		 if(facet.numeric()) {
			 valuesPanel = new JPanel();
		 }
		 else {
			 valuesPanel = new ValuesPanel();
		 }
		 valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.PAGE_AXIS));
		 valuesScrollPane = new JScrollPane(valuesPanel);

		 //set sizes
		 delete.setPreferredSize(new Dimension(40,20));
		 delete.setMaximumSize(new Dimension(40,20));
		 delete.setMinimumSize(new Dimension(40,20));

		 //propertyLbl.setPreferredSize(new Dimension(150,30));
		// propertyLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		 propertyLbl.setMinimumSize(new Dimension(30,20));	

		 operator.setPreferredSize(new Dimension(60,30));
		 operator.setMaximumSize(new Dimension(60,30));
		 operator.setMinimumSize(new Dimension(60,20));		

		 value.setMaximumSize(new Dimension(200,30));
		 value.setPreferredSize(new Dimension(100,25));
		 value.setMinimumSize(new Dimension(30,20));

		 propertyHeader.add(delete);
		 propertyHeader.add(propertyLbl);
		 if(facet.numeric()) {
			 propertyHeader.add(operator);
		 }
		 propertyHeader.add(value);
		 
		 if(facet.numeric()) {
			 value.setText("0");
			 value.getDocument().addDocumentListener(new NumericListener());
			 valuesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		 }
		 else {
			 value.getDocument().addDocumentListener(new StringListener());
			 valuesScrollPane.setWheelScrollingEnabled(false);
			 valuesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		 }

		 add(propertyHeader, BorderLayout.NORTH);
		 add(valuesScrollPane, BorderLayout.CENTER);

		 addActionListeners();
	 }

	 public void addActionListeners() {
		 //add delete filter functionality
		 delete.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 facetedSearchPanel.removeFacet(FacetPanel.this);
			 }
		 });
		 if(facet.numeric()) {
			 value.addActionListener(new NumericListener());
			 operator.addActionListener(new NumericListener());
		 }
	 }

	 // some getters
	 public JPanel getValuesPanel() {
		 return valuesPanel;
	 }

	 public Facet getFacet() {
		 return facet;
	 }
	 public JButton getDelete() {
		 return delete;
	 }
	 public HashSet<Xref> getSelectedGeneIds() {
		 return selectedGeneIds;
	 }
	 public int getPreferredHeight() {
		 if( facet.numeric() ) {
			 return MAX_HEIGHT_NUMERIC;
		 }
		 //else 
		 return Math.min(MAX_HEIGHT_STRING, valuesPanel.getPreferredSize().height + propertyHeader.getHeight());
	 }
	 /**
	  * @return the textfield in the facetpanel
	  */
	 public JTextField getTextField() {
		 return value;
	 }

	 /**
	  * @param geneIds - geneIds to add to set
	  */
	 public void addGeneIds(Collection<Xref> geneIds) {
		 selectedGeneIds.addAll(geneIds);
	 }
	 /**
	  * @param geneIds - geneIds to remove from set
	  */
	 public void removeGeneIds(Collection<Xref> geneIds) {
		 selectedGeneIds.removeAll(geneIds);
	 }
	 /**
	  * 
	  * @return true if any of the togglebuttons are selected
	  */
	 public boolean active() {
		 for(Component c : valuesPanel.getComponents()) {
			 if(((JToggleButton) c).isSelected()) {
				 return true;
			 }
		 }
		 return false;
	 }
	 /**
	  * when the user switches which gene identifier is being used
	  */
	 public void geneIdentifierChanged() {
		 selectedGeneIds.clear();
		 if(facet.numeric()) {
			 JToggleButton trues = (JToggleButton) valuesPanel.getComponent(0);
			 JToggleButton falses = (JToggleButton) valuesPanel.getComponent(1);
			 JToggleButton errors = (JToggleButton) valuesPanel.getComponent(2);
			 if(trues.isSelected()) {
				 selectedGeneIds.addAll(facet.getTrues());
			 }
			 if(falses.isSelected()) {
				 selectedGeneIds.addAll(facet.getFalses());
			 }
			 if(errors.isSelected()) {
				 selectedGeneIds.addAll(facet.getErrors());
			 }
		 }
		 else {
			 for(Component c : valuesPanel.getComponents()) {
				 if(((JToggleButton) c).isSelected()) {
					 selectedGeneIds.addAll(facet.getMap().get(c.getName()));
				 }
			 }
		 }
	 }
	 /**
	  * listener for all of the toggle buttons - goal is to remove/add geneids
	  * to selectedGeneIds depending on the event
	  */
	 class ToggleButtonActionListeners implements ActionListener {
		 public void actionPerformed(ActionEvent e) {
			 JToggleButton btn = (JToggleButton) e.getSource();
			 Collection<Xref> geneIds;
			 //1- figure out what to remove/add
			 if(facet.numeric()) {
				 if(btn.getName().equals("trues")) {
					 geneIds = facet.getTrues();
				 }
				 else if(btn.getName().equals("falses")) {
					 geneIds = facet.getFalses();
				 }
				 else {
					 geneIds = facet.getErrors();

				 }
			 }
			 else {
				 geneIds = facet.getMap().get(btn.getName());
			 }
			 //remove or add it depending on selection
			 if(btn.isSelected()) {
				 addGeneIds(geneIds);
			 }
			 else {
				 removeGeneIds(geneIds);
			 }
			 facetedSearchPanel.applyFilter(FacetPanel.this);
			 repaint();
		 }
	 }
	 /**
	  * listener for the value box of numeric filters
	  */
	 class NumericListener implements ActionListener, DocumentListener{
		 public void actionPerformed(ActionEvent e) {
			 update();
		 }
		 public void changedUpdate(DocumentEvent e) {
			 update();
		 }
		 public void insertUpdate(DocumentEvent e) {
			 update();
		 }
		 public void removeUpdate(DocumentEvent e) {
			 update();
		 }

		 public void update() {
			 if(! Utility.isNumeric(value.getText())) {
				 return;
			 }
			 facet.updateNumericData((String) operator.getSelectedItem(), Double.parseDouble(value.getText()));
			 updateValues();
		 }
	 }
	 /**
	  * listener for the value box of string filters
	  */
	 class StringListener implements ActionListener, DocumentListener{
		 public void actionPerformed(ActionEvent e) {
			 update();
		 }
		 public void changedUpdate(DocumentEvent e) {
			 update();
		 }
		 public void insertUpdate(DocumentEvent e) {
			 update();
		 }
		 public void removeUpdate(DocumentEvent e) {
			 update();
		 }

		 public void update() {
			 filterChoices();
		 }
	 }

	 /**
	  * JPanel with some modification in order to hold sorted jtogglebuttons
	  */
	 class ValuesPanel extends JPanel {

		 private TreeSet<Component> btns;
		 public ValuesPanel() {
			 btns = new TreeSet<Component>(new Utility.NameComparator());
		 }
		 @Override
		 public Component add(Component comp) {
			 btns.add(comp);
			 Component c = btns.higher(comp);
			 if(c == null) {
				 return super.add(comp);
			 }
			 return super.add(comp, getComponentIndex(c));
		 }

		 @Override
		 public void remove(Component comp) {
			 btns.remove(comp);
			 super.remove(comp);
		 }

		 @Override
		 public void removeAll() {
			 super.removeAll();
			 btns.clear();
		 }
		 public int getComponentIndex(Component comp) {
			 if (comp != null && comp.getParent() != null) {
				 Container c = comp.getParent();
				 for (int i = 0; i < c.getComponentCount(); i++) {
					 if (c.getComponent(i) == comp)
						 return i;
				 }
			 }
			 return -1;
		 }
	 }
}
