package backend.verification.timed_state_exploration.zoneProject;

/**
 * This class is used for indexing a continuous variable in the Zone class. It pairs the index of the LPN
 * with the index of the continuous variables and stores the current rate of the continuous variable.
 * @author Andrew N. Fisher
 *
 */
public class LPNContinuousPair extends LPNTransitionPair {

	int _currentRate;

	/**
	 * Create an LPNContinuousPair where both indecies are 0 and the current rate is zero.
	 */
	public LPNContinuousPair() {
		super();
		_currentRate = 0;
	}

	/**
	 * Creates an LPNContinuousPair with the given indecies and current rate.
	 * @param lpnIndex
	 * 		The index of the LPN associate with this variable.
	 * @param continuousIndex
	 * 		The index of the continuous variable in the LPN.
	 * @param currentRate
	 * 		The current rate of the continuous variable.
	 */
	public LPNContinuousPair(int lpnIndex, int continuousIndex, int currentRate) {
		super(lpnIndex, continuousIndex);
		_currentRate = currentRate;
	}
	
	/**
	 * Creates an LPNContinuousPair with the give indexcies and a current rate of zero.
	 * @param lpnIndex
	 * @param continuousIndex
	 */
	public LPNContinuousPair(int lpnIndex, int continuousIndex) {
		super(lpnIndex, continuousIndex);
		_currentRate = 0;
	}

	@Override
	public int hashCode() {
		//final int prime = 37;
		int result = super.hashCode();
		//result = result * prime + _currentRate;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		
		if(!super.equals(obj)){
			// If they are not equal as 
			// LPNtransitionPairs, they are not equal.
			return false;
		}

		if(!(obj instanceof LPNContinuousPair)){
			return false;
		}
		
		//LPNContinuousPair lcPair = (LPNContinuousPair) obj;
		
		//return this._currentRate == lcPair._currentRate;
		return true;
	}

	@Override
	public String toString() {
		String result = "";

		result += "(LPN Index, Continuous Index) = (";

		result += _lpnIndex + ", " + _transitionIndex + ")";
		
		result += " Current rate = " + _currentRate;

		return result;
	}

	@Override
	public LPNContinuousPair clone() {
		LPNContinuousPair newPair = new LPNContinuousPair();
		
		newPair._lpnIndex = this._lpnIndex;
		newPair._transitionIndex = this._transitionIndex;
		newPair._currentRate = this._currentRate;
		
		return newPair;
	}
	
	/**
	 * Returns the continuous variable's index. This method is simply a wrapper
	 * for the inherited get_transitionIndex provided to give that method a 
	 * more appropriate name.
	 * @return
	 * 		The index of the continuous variable.
	 */
	public int get_ContinuousIndex(){
		return get_transitionIndex();
	}
	
	/**
	 * Sets the continuous variables index. This method is simply a wrapper for the
	 * inherited set_transitionIndex provided to give that method a more approriate
	 * name.
	 * @param continuousIndex
	 * 		The index of the continuous variable in the associated LPN.
	 */
	public void set_ContinuousIndex(int continuousIndex){
		set_transitionIndex(continuousIndex);
	}
	
	/**
	 * Get the current rate of the continuous variable.
	 * @return
	 * 		The current rate of the continuous variable.
	 */
	public int getCurrentRate(){
		return _currentRate;
	}
	
	/**
	 * Sets the current rate of the continuous variable.
	 * @param currentRate
	 * 		The current rate of the continuous variable this index refers to.
	 */
	public void setCurrentRate(int currentRate){
		_currentRate = currentRate;
	}
}