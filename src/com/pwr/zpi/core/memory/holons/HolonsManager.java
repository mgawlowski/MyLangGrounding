package com.pwr.zpi.core.memory.holons;

import com.pwr.zpi.core.Agent;
import com.pwr.zpi.core.memory.episodic.BPCollection;
import com.pwr.zpi.core.memory.episodic.BaseProfile;
import com.pwr.zpi.exceptions.InvalidFormulaException;
import com.pwr.zpi.language.Formula;
import com.pwr.zpi.language.SimpleFormula;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mateusz Gawlowski
 */

public class HolonsManager {

    private BPCollection episodicMemory;
    private Set<BaseProfile> baseProfilesRepository;
    private int currTimestamp;
    private Set<Holon> holons;

    public HolonsManager(Agent agent) {
        episodicMemory = agent.getKnowledgeBase();
        holons = new TreeSet<>();
    }

    private void updateRepository(int timestamp) {
        if (currTimestamp != timestamp) {
            currTimestamp = timestamp;
            baseProfilesRepository = episodicMemory.getBaseProfiles(timestamp, BPCollection.MemoryType.WM);
        }
    }

    private Holon createHolon(Formula formula) {
        Holon holon = null;
        try {
            if (formula instanceof SimpleFormula)
                holon = new BinaryHolon(formula, baseProfilesRepository, currTimestamp);
            else holon = new NonBinaryHolon(formula, baseProfilesRepository, currTimestamp);
        } catch (InvalidFormulaException e) {
            e.printStackTrace();
        }
        if (holon != null) {
            holons.add(holon);
        }
        return holon;
    }

    private Holon getHolon(Formula formula) {
        for (Holon h : holons) {
            if (h.getAffectedFormulas().get(0).isFormulaSimilar(formula)) {
                return h;
            }
        }
        return null;
    }

    private boolean isHolonUpToDate(Holon holon) {
        return holon.getTimestamp() == currTimestamp;
    }

    private void updateHolon(Holon holon) {
        Set<BaseProfile> newBaseProfiles = baseProfilesRepository.stream().
                filter(h -> h.getTimestamp() > holon.getTimestamp()).
                collect(Collectors.toCollection(TreeSet::new));
        holon.update(newBaseProfiles, currTimestamp);
    }

    public void updateEveryHolon(int timestamp) {
        updateRepository(timestamp);
        for (Holon h : holons) {
            updateHolon(h);
        }
    }

    public Map<Formula, Double> getSummaries(Formula formula, int timestamp) {
        updateRepository(timestamp);

        Holon holon = getHolon(formula);
        if (holon == null) {
            holon = createHolon(formula);
            return holon.getSummaries();
        }
        if (!isHolonUpToDate(holon)) {
            updateHolon(holon);
        }
        return holon.getSummaries();
    }

}
