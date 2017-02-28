/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package frontend.biomodel.gui.util;

import javax.swing.JButton;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class AbstractRunnableNamedButton extends JButton implements NamedObject,
		Runnable {		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractRunnableNamedButton(String name) {
		super(name);
		this.setName(name);
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}
	
	//private String name = "";
}
