package enginuity.NewGUI.etable;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ESelectionListener implements ListSelectionListener{
	private JTable parentTable = null;
	
	public ESelectionListener(JTable parentTable){
}
	public void valueChanged(ListSelectionEvent event) {
		//System.out.println("1: "+ event.getFirstIndex()+"     2: "+event.getLastIndex());
		
		int selRow[] = parentTable.getSelectedRows();
		int selCol[] = parentTable.getSelectedColumns();
		
		
		for(int i = 0; i < selRow.length; i++){
			//System.out.println("Row Value: "+selRow[i]);
		}
		
		for(int i = 0; i < selCol.length; i++){
			//System.out.println("Col Value: "+selCol[i]);
		}
		
		//System.out.println("---------------------------");
		Object[] selectedCells = new Object[selRow.length * selCol.length];
		
		
	}

}
