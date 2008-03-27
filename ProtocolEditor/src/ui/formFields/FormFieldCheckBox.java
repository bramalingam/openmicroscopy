package ui.formFields;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FormFieldCheckBox extends FormField {
	
	boolean checkedValue;
	JCheckBox checkBox;
	
	public FormFieldCheckBox (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		
		checkBox = new JCheckBox(" ", checkedValue);
		checkBox.addActionListener(new CheckBoxListener());
		checkBox.addFocusListener(componentFocusListener);
		checkBox.setBackground(null);
		
		horizontalBox.add(checkBox);
		
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);	
		
		if (checkBox != null)	// just in case!
			checkBox.setEnabled(enabled);
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		checkedValue = dataField.isAttributeTrue(DataFieldConstants.VALUE);
		checkBox.setSelected(checkedValue);
	} 
	
	
	
	
	public class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			 checkedValue = checkBox.isSelected();
			 
			 String value = Boolean.toString(checkedValue);
			 dataField.setAttribute(DataFieldConstants.VALUE, value, true);
		}
		
	}
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the checkBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!checkBox.hasFocus()))
			checkBox.requestFocusInWindow();
	}
	
}
