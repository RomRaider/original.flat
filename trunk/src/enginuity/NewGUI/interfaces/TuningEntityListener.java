package enginuity.NewGUI.interfaces;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import enginuity.NewGUI.tree.ETreeNode;

public interface TuningEntityListener {
	public void TreeStructureChanged(ETreeNode rootNodeOfNewTree);
	
	public void rebuildJMenuBar(Vector<JMenu> items);
	
	public void setNewToolBar(JToolBar theToolBar);
}
