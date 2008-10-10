package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControlFlowPanel extends JPanel implements ActionListener {

	private String selected = "";

	private PropertyList flowList;

	private JComboBox fromBox, toBox;

	private String[] placeList = null, transitionList = null, flowStringList;

	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;

	private HashMap<String, PropertyField> fields = null;

	public ControlFlowPanel(String selected, PropertyList flowList, LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.flowList = flowList;
		this.lhpn = lhpn;

		placeList = lhpn.getPlaceList();
		transitionList = lhpn.getTransitionList();
		flowStringList = new String[1];
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			flowStringList = new String[placeList.length + transitionList.length];
		}
		placeList = lhpn.getPlaceList();
		transitionList = lhpn.getTransitionList();
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
			System.arraycopy(transitionList, 0, flowStringList, placeList.length,
					transitionList.length);
		}
		else if (placeList.length > 0) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
		}
		else if (transitionList.length > 0) {
			System.arraycopy(transitionList, 0, flowStringList, 0, transitionList.length);
		}

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);
		add(field);

		JPanel tempPanel;
		JLabel tempLabel;
		// From field
		if (placeList.length > 0 && transitionList.length > 0) {
			tempPanel = new JPanel();
			tempLabel = new JLabel("From");
			fromBox = new JComboBox(flowStringList);
			fromBox.setSelectedItem(flowStringList[0]);
			fromBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(fromBox);
			add(tempPanel);
		}

		// To field
		if (placeList.length > 0 && transitionList.length > 0) {
			tempPanel = new JPanel();
			tempLabel = new JLabel("To");
			toBox = new JComboBox(flowStringList);
			toBox.setSelectedItem(flowStringList[0]);
			toBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(toBox);
			add(tempPanel);
		}

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected);
			if (isPlace(fromBox)) {
				fromBox.removeAll();
				for (int i = 0; i < placeList.length; i++) {
					fromBox.addItem(placeList[i]);
				}
				toBox.removeAll();
				for (int i = 0; i < transitionList.length; i++) {
					toBox.addItem(transitionList[i]);
				}
			}
			else {
				fromBox.removeAll();
				for (int i = 0; i < transitionList.length; i++) {
					fromBox.addItem(transitionList[i]);
				}
				toBox.removeAll();
				for (int i = 0; i < placeList.length; i++) {
					toBox.addItem(placeList[i]);
				}
			}
		}

		if (placeList.length > 0 && transitionList.length > 0) {
			boolean display = false;
			while (!display) {
				display = openGui(oldName);
			}
		}
		else {
			JOptionPane.showMessageDialog(this,
					"There must be places and transitions to create control flow.",
					"Control Flow Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Control Flow Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Movement id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (lhpn.getVariables().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Movement id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit

			if (selected != null && !oldName.equals(id)) {
				lhpn.changeVariableName(oldName, id);
			}
			lhpn.addControlFlow(fromBox.getSelectedItem().toString(), toBox.getSelectedItem()
					.toString());
			flowList.removeItem(oldName);
			flowList.addItem(id);
			flowList.setSelectedValue(id, true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	private boolean isPlace(JComboBox box) {
		String test = box.getSelectedItem().toString();
		for (int i = 0; i < placeList.length; i++) {
			if (placeList[i].equals(test)) {
				return true;
			}
		}
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			if (isPlace(fromBox)) {
				fromBox.removeAll();
				for (int i = 0; i < placeList.length; i++) {
					fromBox.addItem(placeList[i]);
				}
				toBox.removeAll();
				for (int i = 0; i < transitionList.length; i++) {
					toBox.addItem(transitionList[i]);
				}
			}
			else {
				fromBox.removeAll();
				for (int i = 0; i < transitionList.length; i++) {
					fromBox.addItem(transitionList[i]);
				}
				toBox.removeAll();
				for (int i = 0; i < placeList.length; i++) {
					toBox.addItem(placeList[i]);
				}
			}
		}
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

}
