package com.pwr.zpi.core.memory.holons;

import com.pwr.zpi.core.memory.episodic.BPCollection;
import com.pwr.zpi.core.memory.episodic.BaseProfile;
import com.pwr.zpi.exceptions.InvalidFormulaException;
import com.pwr.zpi.exceptions.NotApplicableException;
import com.pwr.zpi.language.Formula;
import com.pwr.zpi.language.Grounder;
import com.pwr.zpi.language.SimpleFormula;
import com.pwr.zpi.util.Pair;

import java.util.*;

/**
 * Holon that represents summarizations of simple formula.
 *
 * @author Mateusz Gawlowski
 */

public class BinaryHolon implements Holon {

    protected Pair<Double, Double> Tao;
    protected List<Formula> formula;
    private int timestamp;

    public BinaryHolon(Formula formula, Set<BaseProfile> baseProfiles, int timestamp) throws InvalidFormulaException {
        this.formula = formula.getComplementaryFormulas();
        update(baseProfiles, timestamp);
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean update(Set<BaseProfile> baseProfiles, int newTimestamp) {
        double sumPositive = 0;
        double sumNegative = 0;
        try {
            Set<BaseProfile> groundingSetsMap;
            groundingSetsMap = Grounder.getGroundingSet(formula.get(0), BPCollection.asBaseProfilesSet(baseProfiles));
            sumPositive += Grounder.simpleFormulaFinalGrounder(formula.get(0), groundingSetsMap, baseProfiles);
            groundingSetsMap = Grounder.getGroundingSet(formula.get(1), BPCollection.asBaseProfilesSet(baseProfiles));
            sumNegative += Grounder.simpleFormulaFinalGrounder(formula.get(1), groundingSetsMap, baseProfiles);

            if (((SimpleFormula) formula.get(0)).isNegated()) {
                double temp = sumPositive;
                sumPositive = sumNegative;
                sumNegative = temp;
            }
        } catch (InvalidFormulaException | NotApplicableException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Is: " + sumPositive + " Not: " + sumNegative);
        timestamp = newTimestamp;
        Tao = new Pair<>(sumPositive, sumNegative);
        return true;
    }

    @Override
    public Map<Formula, Double> getSummaries()  {
        Map<Formula, Double> out = new HashMap<>();

            if(((SimpleFormula)formula.get(0)).isNegated()) {
                out.put(formula.get(0),Tao.getV() );
                out.put(formula.get(1),Tao.getK() );
            }
            else {
                out.put(formula.get(0), Tao.getK());
                out.put(formula.get(1), Tao.getV());
            }
        return out;
    }

    @Override
    public List<Formula> getAffectedFormulas() {
        return formula;
    }

}
