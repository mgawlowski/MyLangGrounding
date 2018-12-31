package com.pwr.zpi.core.memory.holons;

import com.pwr.zpi.core.Agent;
import com.pwr.zpi.core.memory.episodic.DistributedKnowledge;
import com.pwr.zpi.exceptions.InvalidFormulaException;
import com.pwr.zpi.exceptions.NotApplicableException;
import com.pwr.zpi.exceptions.NotConsistentDKException;
import com.pwr.zpi.language.Formula;
import com.pwr.zpi.language.SimpleFormula;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of all holons present in agent's memory.
 *
 * @author Mateusz Gawlowski
 */
public class HolonCollection {

    private Set<Holon> holonCollection;
    Agent owner;


    /**
     * Complex constructor.
     * @param holonCollection
     * @param owner Agent which contains this HolonCollection.
     */
    public HolonCollection(Set<Holon> holonCollection, Agent owner) {
        if (owner == null)
            throw new NullPointerException("Some parameter was not specified.");

        this.holonCollection = holonCollection != null ? holonCollection : new TreeSet<>();
        this.owner = owner;
    }

    public HolonCollection(Agent owner) {
            if (owner == null /*|| contextualisation == null*/)
                throw new NullPointerException("Some parameter was not specified.");

            this.holonCollection = new TreeSet<>();
            this.owner = owner;
    }


    /**
     * Method looks for specific holon in holons and returns it. If none is found returns newly created one.
     *
     * @param formula
     * @param timestamp
     * @return desired holon
     */
    public Holon getHolon(Formula formula, int timestamp) {
        for (Holon h : holonCollection) {
            if (h.getAffectedFormulas().get(0).isFormulaSimilar(formula)) {
                try {
                    h.update(owner.distributeKnowledge(formula, timestamp));
                } catch (InvalidFormulaException | NotApplicableException e) {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "Not able to produce holon.", e);
                }
                return h;
            }
        }
        return addHolon(formula, timestamp);
    }

    /**
     * Method adds a new holon to holons based on givn formula, agent and timestamp
     *
     * @param formula
     * @param timestamp
     * @return created holon
     */
    public Holon addHolon(Formula formula, int timestamp) {
        Holon holon = null;
        try {
            if (formula instanceof SimpleFormula)
                holon = new BinaryHolon(new DistributedKnowledge(owner, formula, timestamp, true));
            else holon = new NonBinaryHolon(owner.distributeKnowledge(formula, timestamp, true));

            holonCollection.add(holon);
        } catch (InvalidFormulaException e) {
            e.printStackTrace();
        } catch (NotApplicableException e) {
            e.printStackTrace();
        } catch (NotConsistentDKException e) {
            e.printStackTrace();
        }
        return holon;
    }

    public void updateBeliefs(int timestamp) throws InvalidFormulaException, NotConsistentDKException, NotApplicableException {
        for (Holon h : holonCollection) {
            h.update(owner.distributeKnowledge(h.getAffectedFormulas().get(0), timestamp, true));
        }
    }


    public Holon findHolon(Formula formula, int timestamp) {
        for (Holon holon : holonCollection)
            if (holon.getFormula().equals(formula))
                return holon;
        return addHolon(formula, timestamp);
    }

    public Set<Holon> getHolonCollection() {
        return holonCollection;
    }
}
