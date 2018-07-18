package org.mudebug.mufl;

public abstract class ProgramElement {
    protected Mutation[] mutations;
    protected final int[] rank;
    
    public abstract double getSusp(SuspTech which); 
    
    public abstract double getTestDistinguishingOchiaiSusp(); 
    
    protected ProgramElement() {
        this.mutations = new Mutation[0];
        this.rank = new int[SuspTech.TECH_COUNT];
    }
    
    public void addMutation(final Mutation mutation) {
        Mutation[] mutationsExt = new Mutation[mutations.length + 1];
        System.arraycopy(mutations, 0, mutationsExt, 0, mutations.length);
        mutationsExt[mutations.length] = mutation;
        this.mutations = mutationsExt;
    }

    public Mutation[] getMutations() {
        return mutations;
    }
    

    public int getRank(final SuspTech which) {
        return rank[which.ordinal()];
    }

    public void setRank(final SuspTech which, final int rank) {
        this.rank[which.ordinal()] = rank;
    }
}
