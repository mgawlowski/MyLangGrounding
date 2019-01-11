package com.pwr.zpi.core.memory.holons;

import com.pwr.zpi.core.memory.episodic.BaseProfile;
import com.pwr.zpi.language.Formula;

import java.util.*;

/**
 * Interface for holons which represent summarizations of empirical episodic experiences gathered by agent.
 * Each holon describes different formula, so there are separate holons for every characteristics and every object.
 *
 * @author Mateusz Gawlowski
 */

public interface Holon extends Comparable<Holon> {

    int getTimestamp();

    boolean update(Set<BaseProfile> baseProfiles, int newTimestamp);

    Map<Formula, Double> getSummaries();

    List<Formula> getAffectedFormulas();

    default int compareTo(Holon o) {
        double res = 0, res2 = 0;
        for (Formula f : getSummaries().keySet()) {
            res = f.hashCode() + getSummaries().get(f);
        }

        for (Formula f : o.getSummaries().keySet()) {
            res2 = f.hashCode() + o.getSummaries().get(f);
        }
        return res > res2 ? 1 : (res < res2 ? -1 : 0);
    }
}