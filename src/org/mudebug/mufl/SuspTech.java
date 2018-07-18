package org.mudebug.mufl;

import java.util.function.ToDoubleFunction;

public enum SuspTech {
    OCHIAI("Ochiai", Mutation::getOchiaiSusp),
    KULCZYNSKI2("Kulczynski2", Mutation::getKulczynski2Susp),
    ZOLTAR("Zoltar", Mutation::getZoltarSusp),
    M2("M2", Mutation::getM2Susp),
    OCHIAI2("Ohiai", Mutation::getOchiai2Susp),
    MUSE("MUSE", Mutation::getMUSESusp);
    
    
    public static final int TECH_COUNT = values().length;
    public final String techName;
    public final ToDoubleFunction<Mutation> suspFunc;
    
    SuspTech(final String techName, final ToDoubleFunction<Mutation> getSusp) {
        this.techName = techName;
        this.suspFunc = getSusp;
    }
}
