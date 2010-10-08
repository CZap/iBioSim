package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.schematic.ListChooser;
import gcm2sbml.gui.schematic.Schematic;
import gcm2sbml.gui.schematic.Utils;
import gcm2sbml.parser.GCMFile;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import parser.TSDParser;

import reb2sac.Reb2Sac;

import biomodelsim.BioSim;

public class MovieContainer extends JPanel implements ActionListener {

	
	public static final int FRAME_DELAY_MILLISECONDS = 20;
	
	private static final long serialVersionUID = 1L;
	private Schematic schematic;
	private Reb2Sac reb2sac;
	
	private GCMFile gcm;
	private BioSim biosim;
	
	TSDParser parser;
	Timer playTimer;
	
	private final String PLAYING = "playing"; 
	private final String PAUSED = "paused"; 
	private String mode = PLAYING;
	
	public MovieContainer(Reb2Sac reb2sac_, GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml){
		super(new BorderLayout());
		schematic = new Schematic(gcm, biosim, gcm2sbml, false);
		this.add(schematic, BorderLayout.CENTER);
		
		this.gcm = gcm;
		this.biosim = biosim;
		this.reb2sac = reb2sac_;
		
		this.playTimer = new Timer(0, playTimerEventHandler);
		mode = PAUSED;
	}
	
	private boolean isUIInitialized;
	public void display(){
		schematic.display();

		if(isUIInitialized == false){
			this.addUI();
			
			isUIInitialized = true;
		}
		
		if(this.parser == null)
			prepareTSDFile();

	}
	
	/**
	 * Allows the user to choose from valid TSD files, then loads and parses the file.
	 * @return
	 * @throws ListChooser.EmptyListException
	 */
	private void prepareTSDFile(){
		pause();
		
		Vector<String> filenames = new Vector<String>();
		for (String s : new File(reb2sac.getSimPath()).list()) {
			if (s.endsWith(".tsd")) {
				filenames.add(s);
			}
		}
		String filename;
		try{
			filename = ListChooser.selectFromList(BioSim.frame, filenames.toArray(), "Please choose a simulation file");
		}catch(ListChooser.EmptyListException e){
			JOptionPane.showMessageDialog(BioSim.frame, "Sorry, there aren't any simulation files. Please simulate then try again.");
			return;
		}
		if(filename == null)
			return;
		String fullFilePath = reb2sac.getSimPath() + File.separator + filename;
		this.parser = new TSDParser(fullFilePath, false);
		
		slider.setMaximum(parser.getNumSamples()-1);
		slider.setValue(0);
		
		biosim.log.addText(fullFilePath + " loaded. " + 
				String.valueOf(parser.getData().size()) +
				" rows of data loaded.");
	}
	
	JButton playPauseButton;
	JButton rewindButton;
	JButton singleStepButton;
	JSlider slider;
	private void addUI(){
		// Add the bottom menu bar
		JToolBar mt = new JToolBar();
		
		rewindButton = Utils.makeToolButton("movie" + File.separator + "rewind.png", "rewind", "Rewind", this);
		mt.add(rewindButton);

		singleStepButton = Utils.makeToolButton("movie" + File.separator + "single_step.png", "singlestep", "Single Step", this);
		mt.add(singleStepButton);
		
		playPauseButton = Utils.makeToolButton("movie" + File.separator + "play.png", "playpause", "Play", this);
		mt.add(playPauseButton);
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		mt.add(slider);

		this.add(mt, BorderLayout.SOUTH);
		
		// add the side menu bar
		JToolBar sb = new JToolBar();
		JButton b = new JButton();
		b.addActionListener(this);
		b.setText("Choose Sim File");
		b.setActionCommand("choose_simulation_file");
		sb.add(b);
		this.add(sb, BorderLayout.NORTH);
	}
	
	/**
	 * Called whenever the play/pause button is pressed, or when the system needs to 
	 * pause the movie (such as at the end)
	 */
	private void playPauseButtonPress(){
		if(mode == PAUSED){
			if(slider.getValue() >= slider.getMaximum()-1)
				slider.setValue(0);
			playTimer.setDelay(FRAME_DELAY_MILLISECONDS);
			//playPauseButton.setText("Pause");
			Utils.setIcon(playPauseButton, "movie" + File.separator + "pause.png");
			playTimer.start();
			mode = PLAYING;
		}else{
			Utils.setIcon(playPauseButton, "movie" + File.separator + "play.png");
			playTimer.stop();
			mode = PAUSED;
		}		
	}
	
	private void pause(){
		if(mode == PLAYING)
			playPauseButtonPress();
	}
	
	/**
	 * event handler for when UI buttons are pressed.
	 */
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String command = event.getActionCommand();
		
		if(command.equals("rewind")){
			slider.setValue(0);
		}else if(command.equals("playpause")){
			playPauseButtonPress();
		}else if(command.equals("singlestep")){
			nextFrame();
		}else if(command.equals("choose_simulation_file")){
			prepareTSDFile();
		}else{
			throw new Error("Unrecognized command!");
		}
	}
	
	/**
	 * event handler for when the timer ticks
	 */
	ActionListener playTimerEventHandler = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			nextFrame();
		}
	};
	
	/**
	 * advances the movie to the next frame
	 */
	private void nextFrame(){
		slider.setValue(slider.getValue()+1);
		if(slider.getValue() >= slider.getMaximum()){
			pause();
		}
		updateVisuals();
	}
	
	/**
	 * Called when the timer ticks and we need to update the colors/sizes/etc.
	 */
	private void updateVisuals(){
		int pos = slider.getValue();
		if(pos < 0 || pos > parser.getNumSamples()-1){
			throw new Error("Invalid slider value! It is outside the data range!");
		}
		
		HashMap<String, ArrayList<Double>> dataHash = parser.getHashMap();
		
		schematic.beginFrame();
		for(String s:gcm.getSpecies().keySet()){
			if(dataHash.containsKey(s)){
				double value = dataHash.get(s).get(pos);
				schematic.setSpeciesAnimationValue(s, value*2.0);
			}
		}
		schematic.endFrame();
		
	}

}
