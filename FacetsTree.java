package org.pathvisio.facets.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.EventObject;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

/**
 * Custom tree for rendering Facets
 * Needed to change the renderer, editor and UI
 * @author jakefried
 */
public class FacetsTree extends JTree {

	public FacetsTree() {
		super(new DefaultTreeModel(new DefaultMutableTreeNode("DataServices")));
		BasicTreeUI ui = new CustomTreeUI();
		TreeCellRenderer renderer = new MyRenderer();
		TreeCellEditor editor = new MyTreeCellEditor(this, (DefaultTreeCellRenderer) renderer);

		setUI( ui );    
		setCellRenderer(renderer);
		setCellEditor(editor);
		
		setEditable(true);
		setRootVisible(false);
		setShowsRootHandles(true);
		setRowHeight(-1);
	}
	
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}
	public void resetUI() {
		BasicTreeUI ui = new CustomTreeUI();
		setUI( ui );
		ui.setRightChildIndent(5);
	}
	
	/**
	 * Custom renderer for displaying Facets and Dataservice names. 
	 * Dataservice names are JLabel
	 * Facet is JPanel
	 * @author jakefried
	 */
	class MyRenderer extends DefaultTreeCellRenderer  {

		public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if(node.getUserObject() instanceof FacetPanel) {
				FacetPanel fp = (FacetPanel) node.getUserObject();
				return fp;
			}
			return new JLabel(node.getUserObject().toString());
		}
		//make the background transparent
		@Override
		public Color getBackground() {
			return null;
		}
		@Override
		public Color getBackgroundNonSelectionColor() {
			return null;
		}
	}
	//Needed to be able to click on the buttons
	/**
	 * @author jakefried
	 */
	class MyTreeCellEditor extends DefaultTreeCellEditor {
		public MyTreeCellEditor ( JTree tree, DefaultTreeCellRenderer renderer ) {
			super(tree, renderer);
		}

		public Component getTreeCellEditorComponent ( JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row ) {
			return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}
		public boolean isCellEditable ( EventObject anEvent ) {
			return true;
		}
	}

	/**
	 * Custom UI in order to make the tree fill all available space
	 * @author jakefried
	 */
	public class CustomTreeUI extends BasicTreeUI {

		public CustomTreeUI() {
			super();
		}
		@Override
		protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
			return new NodeDimensionsHandler() {
				@Override
				public Rectangle getNodeDimensions( Object value, int row, int depth, boolean expanded, Rectangle size) {
					Rectangle dimensions = super.getNodeDimensions(value, row, depth, expanded, size);
					if( getParent() != null ) {
						dimensions.width = (int) getParent().getWidth() - 50;
					}
					return dimensions;
				}
			};
		}
	}
}
