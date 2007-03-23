package enginuity.NewGUI.etable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import enginuity.NewGUI.data.ApplicationStateManager;
import enginuity.NewGUI.data.TableNodeMetaData;

public class EInternalFrame extends JInternalFrame implements InternalFrameListener, ActionListener{
	private Stack<ETableSaveState> savedData = new Stack<ETableSaveState>();
	//private Stack<Double[][]> savedData = new Stack<Double[][]>();
	
	private ETable eTable;
	private TableNodeMetaData tableMetaData;
	
	public EInternalFrame(TableNodeMetaData tableMetaData, Double[][] data, Dimension tableDimensions){
		super(tableMetaData.getTableName(), true, true, true, true);
		this.tableMetaData = tableMetaData;
		
		// Save initial data
		this.savedData.push(new ETableSaveState(data));
		//this.savedData.push(data);
		
		eTable = new ETable(tableMetaData, data);
		
		ETableToolBar toolBar = new ETableToolBar(tableMetaData, eTable);
		
		JScrollPane scrollPane = new JScrollPane(eTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
    	// Add internal frame
		this.setLayout(new BorderLayout());
		this.setJMenuBar(new ETableMenuBar(this));
		this.add(toolBar, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setSize(tableDimensions);
		this.setVisible(true);
		this.addInternalFrameListener(this);
	}
	
	public boolean dataChanged(){
		return false;
	}
	
	public Double[][] getTableData(){
		Double[][] data = this.eTable.getTheModel().getData();
		return data;
	}
	
	public void saveDataState(){
		this.savedData.push(new ETableSaveState(this.getTableData()));
		//this.savedData.push(this.getTableData());
	}
	
	public void revertDataState(){
		if(!this.savedData.isEmpty()){
			this.setTableData(this.savedData.pop().getData());
			//this.setTableData(this.savedData.pop());
		}
	}
	
	
	public void setTableData(Double[][] data){
		this.eTable.getTheModel().replaceData(data);
	}
	
	public void internalFrameOpened(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameClosing(InternalFrameEvent arg0) {
		System.out.println("Start to close frame.");
		
		
	}

	public void internalFrameClosed(InternalFrameEvent arg0) {
		System.out.println("Internal Frame closed, complete cleaning up");
		ApplicationStateManager.getEnginuityInstance().removeInternalFrame(this);
		
	}

	public void internalFrameIconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameActivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public TableNodeMetaData getTableMetaData() {
		return tableMetaData;
	}
}
