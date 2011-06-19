package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.RealParameter;
import beast.evolution.datatype.DataType;
import beast.evolution.tree.Node;

@Description("Mutation Death substitution model, can be used as Stochastic Dollo model.")
public class MutationDeathModel extends SubstitutionModel.Base {

    public Input<RealParameter> delParameter = new Input<RealParameter>("deathprob", "rate of death, used to calculate death probability", Validate.REQUIRED);
    public Input<RealParameter> mutationRate = new Input<RealParameter>("mu", "mutation rate, default 1");
    public Input<SubstitutionModel.Base> m_CTMCModel = new Input<SubstitutionModel.Base>("substmodel","CTMC Model for the life states, so should have " +
    		"a state-space one less than this model. If not specified, ...");
    // TODO: figure out the end of the last sentence
    
    /** transition matrix for live states **/
    protected double[] m_trMatrix;
    /** number of states **/
    int m_nStates;
    
    @Override
    public void initAndValidate() throws Exception {
    	super.initAndValidate();
		double [] freqs = getFrequencies();
		m_nStates = freqs.length;
        m_trMatrix = new double[(m_nStates - 1) * (m_nStates - 1)];
        if (m_CTMCModel.get() != null) {
        	if (m_CTMCModel.get().frequenciesInput.get().m_fFreqs.length != m_nStates - 1) {
        		throw new Exception("substmodel does not have the correct state space: should be " + (m_nStates-1));
        	}
        }
    }
    
    @Override
	public EigenDecomposition getEigenDecomposition(Node node) {
		return null;
	}

	@Override
    public void getTransitionProbabilities(Node node, double fStartTime, double fEndTime, double fRate, double[] matrix) {
      	double distance = (fStartTime - fEndTime) * fRate;
        int i, j;
        // assuming that expected number of changes in CTMCModel is 1 per unit time
        // we are contributing s*deathRate number of changes per unit of time
        double deathProb = Math.exp(-distance * delParameter.get().getValue());
        double mutationR = 2;
        if (mutationRate.get() != null) {
            mutationR *= mutationRate.get().getValue();
        }
        double freqs[] = getFrequencies();

        for (i = 0; i < freqs.length - 1; ++i) {
            mutationR *= freqs[i];
        }
        SubstitutionModel.Base CTMCModel = m_CTMCModel.get();
        if (CTMCModel != null) {
            CTMCModel.getTransitionProbabilities(node, fStartTime, fEndTime, mutationR * fRate, m_trMatrix);
        } else {
            m_trMatrix[0] = 1.0;
        }

        for (i = 0; i < m_nStates - 1; ++i) {
            for (j = 0; j < m_nStates - 1; j++) {
                matrix[i * (m_nStates) + j] = m_trMatrix[i * (m_nStates - 1) + j] * deathProb;
            }
            matrix[i * (m_nStates) + j] = (1.0 - deathProb);
        }

        for (j = 0; j < m_nStates - 1; ++j) {
            matrix[m_nStates * (m_nStates - 1) + j] = 0.0;
        }

        matrix[m_nStates * m_nStates - 1] = 1.0;
    } // getTransitionProbabilities
	
	/** CalculationNode implementation **/
	@Override
	protected boolean requiresRecalculation() {
	   	// we only get here if delParameter or mutationRate is dirty
	   	return true;
	}
	
    @Override
    public boolean canHandleDataType(DataType dataType) throws Exception {
    	if (dataType.getStateCount() == Integer.MAX_VALUE) {
    		return false;
    	}
	   return true;
    }
	
} // class MutationDeathModel
