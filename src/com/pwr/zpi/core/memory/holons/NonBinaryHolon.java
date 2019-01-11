package com.pwr.zpi.core.memory.holons;

import com.pwr.zpi.core.memory.episodic.BPCollection;
import com.pwr.zpi.core.memory.episodic.BaseProfile;
import com.pwr.zpi.exceptions.InvalidFormulaException;
import com.pwr.zpi.language.Formula;
import com.pwr.zpi.language.Grounder;

import java.util.*;

/**
 * ...
 *
 * @author Mateusz Gawlowski
 */

public class NonBinaryHolon implements Holon{

    Formula relatedFormula;
    Map<Formula, Double> summaries;
    private int timestamp;

    NonBinaryHolon(Formula formula, Set<BaseProfile> baseProfiles, int timestamp) {
        relatedFormula = formula;
        summaries = new HashMap<>();
        update(baseProfiles, timestamp);
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean update(Set<BaseProfile> baseProfiles, int newTimestamp) {
        try {
            List<Formula> complementaryFormulas = relatedFormula.getComplementaryFormulas();
            Map<Formula, Set<BaseProfile>> groundingSetsMap = Grounder.getGroundingSets(complementaryFormulas, BPCollection.asBaseProfilesSet(baseProfiles));
            summaries = Grounder.relativeCard_(groundingSetsMap);
        } catch (InvalidFormulaException e) {
            e.printStackTrace();
            return false;
        }
        timestamp = newTimestamp;
        return true;
    }

    @Override
    public Map<Formula, Double> getSummaries() {
        return summaries;
    }

    @Override
    public List<Formula> getAffectedFormulas() {
        return new ArrayList<>(summaries.keySet());
    }

    @Override
    public String toString() {
        return "NewNonBinaryHolon{" +
                "summaries=" + summaries +
                '}';
    }
}
