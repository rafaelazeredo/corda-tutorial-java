package com.tutorial.writingcontract;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.finance.contracts.CommercialPaper;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class State implements OwnableState {
    private PartyAndReference issuance;
    private AbstractParty owner;
    private Amount<Issued<Currency>> faceValue;
    private Instant maturityDate;


    public State() {
    }

    public State(PartyAndReference issuance, AbstractParty owner, Amount<Issued<Currency>> faceValue, Instant maturityDate) {
        this.issuance = issuance;
        this.owner = owner;
        this.faceValue = faceValue;
        this.maturityDate = maturityDate;
    }

    public State copy() {
        return new State(this.getIssuance(), this.getOwner(), this.getFaceValue(), this.getMaturityDate());
    }

    public State withoutOwner() {
        return new State(this.getIssuance(), new AnonymousParty(NullKeys.NullPublicKey.INSTANCE), this.getFaceValue(), this.getMaturityDate());
    }

    @NotNull
    public CommandAndState withNewOwner(@NotNull AbstractParty newOwner) {
        return new CommandAndState(new CommercialPaper.Commands.Move(), new State(this.getIssuance(), newOwner, this.getFaceValue(), this.getMaturityDate()));
    }


    public PartyAndReference getIssuance() {
        return issuance;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public AbstractParty getOwner() {
        return owner;
    }

    public Amount<Issued<Currency>> getFaceValue() {
        return faceValue;
    }

    public Instant getMaturityDate() {
        return maturityDate;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (Objects.isNull(obj) || getClass() != obj.getClass()) return false;


        State state = (State) obj;
        if (Objects.nonNull(this.issuance) ? !this.issuance.equals(state.issuance) : Objects.nonNull(state.issuance))
            return false;
        if (Objects.nonNull(this.owner) ? !this.owner.equals(state.owner) : Objects.nonNull(state.owner)) return false;
        if (Objects.nonNull(this.faceValue) ? !this.faceValue.equals(state.faceValue) : Objects.nonNull(state.faceValue))
            return false;
        return !(Objects.nonNull(this.maturityDate) ? !this.maturityDate.equals(state.maturityDate) : Objects.nonNull(state.maturityDate));
    }

    @Override
    public int hashCode() {
        int result = Objects.nonNull(this.issuance) ? this.issuance.hashCode() : 0;
        result = (31 * result) + (Objects.nonNull(this.owner) ? this.owner.hashCode() : 0);
        result = (31 * result) + (Objects.nonNull(this.faceValue) ? this.faceValue.hashCode() : 0);
        result = (31 * result) + (Objects.nonNull(this.maturityDate) ? this.maturityDate.hashCode() : 0);
        return result;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(this.owner);
    }
}

