package org.mudebug.mufl;

public abstract class ProgramElement {
    protected Mutation[] mutations;
    
    public abstract double getOldSusp(); 
    
    public abstract double getNewSusp(); 
    
    protected ProgramElement() {
        this.mutations = new Mutation[0];
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
}
