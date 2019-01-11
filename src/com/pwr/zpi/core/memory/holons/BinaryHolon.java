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

    private Pair<Double, Double> tao;
    private List<Formula> formula;
    private int retrospectiveCard;
    private int timestamp;

    public BinaryHolon(Formula formula, Set<BaseProfile> baseProfiles, int timestamp) throws InvalidFormulaException {
        this.timestamp = 0;
        retrospectiveCard = 0;
        tao = new Pair<>(0.0, 0.0);
        this.formula = formula.getComplementaryFormulas();
        update(baseProfiles, timestamp);
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean update(Set<BaseProfile> newBaseProfiles, int newTimestamp) {
        double sumPositive = 0;
        double sumNegative = 0;
        int groundSetCard = 0;
        try {
            Set<BaseProfile> groundingSetsMap;

            groundingSetsMap = Grounder.getGroundingSet(formula.get(0), BPCollection.asBaseProfilesSet(newBaseProfiles));
            sumPositive += Grounder.simpleFormulaFinalGrounder(formula.get(0), groundingSetsMap, newBaseProfiles);
            groundSetCard += groundingSetsMap.size();

            groundingSetsMap = Grounder.getGroundingSet(formula.get(1), BPCollection.asBaseProfilesSet(newBaseProfiles));
            sumNegative += Grounder.simpleFormulaFinalGrounder(formula.get(1), groundingSetsMap, newBaseProfiles);
            groundSetCard += groundingSetsMap.size();

            if (((SimpleFormula) formula.get(0)).isNegated()) {
                double temp = sumPositive;
                sumPositive = sumNegative;
                sumNegative = temp;
            }
        } catch (InvalidFormulaException | NotApplicableException e) {
            e.printStackTrace();
            return false;
        }
        timestamp = newTimestamp;
        updateTao(sumPositive, sumNegative, groundSetCard);
        return true;
    }

    private void updateTao(double sumPositive, double sumNegative, int groundSetCard) {
        if (((SimpleFormula) formula.get(0)).isNegated()) {
            sumPositive = sumPositive * groundSetCard + tao.getV() * retrospectiveCard;
            sumNegative = sumNegative * groundSetCard + tao.getK() * retrospectiveCard;
            retrospectiveCard += groundSetCard;
            tao.setV(sumPositive / retrospectiveCard);
            tao.setK(sumNegative / retrospectiveCard);
        }
        else {
            sumPositive = sumPositive * groundSetCard + tao.getK() * retrospectiveCard;
            sumNegative = sumNegative * groundSetCard + tao.getV() * retrospectiveCard;
            retrospectiveCard += groundSetCard;
            tao.setK(sumPositive / retrospectiveCard);
            tao.setV(sumNegative / retrospectiveCard);
        }
    }

    @Override
    public Map<Formula, Double> getSummaries() {
        Map<Formula, Double> out = new HashMap<>();

        if (((SimpleFormula) formula.get(0)).isNegated()) {
            out.put(formula.get(0), tao.getV());
            out.put(formula.get(1), tao.getK());
        } else {
            out.put(formula.get(0), tao.getK());
            out.put(formula.get(1), tao.getV());
        }
        return out;
    }

    @Override
    public List<Formula> getAffectedFormulas() {
        return formula;
    }

    @Override
    public String toString() {
        return "BinaryHolon{" +
                "summaries=" + tao.getK() + " " + tao.getV() +
                '}';
    }
}
