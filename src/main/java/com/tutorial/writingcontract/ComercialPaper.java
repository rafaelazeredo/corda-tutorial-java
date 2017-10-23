package com.tutorial.writingcontract;

import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.finance.utils.StateSumming.sumCashBy;

public class ComercialPaper implements Contract {
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        List<LedgerTransaction.InOutGroup<State, State>> groups = tx.groupStates(State.class, State::withoutOwner);
        CommandWithParties<Commands> cmd = requireSingleCommand(tx.getCommands(), Commands.class);

        TimeWindow timeWindow = tx.getTimeWindow();

        for (LedgerTransaction.InOutGroup group : groups) {
            List inputs = group.getInputs();
            List outputs = group.getOutputs();
            if (cmd.getValue() instanceof Commands.Move) {
                State input = (State) inputs.get(0);
                requireThat(require -> {
                    require.using("the transaction is signed by the owner of the CP", cmd.getSigners().contains(input.getOwner().getOwningKey()));
                    require.using("The state is propagated.", outputs.size() == 1);
                    // Don't need to check anything else, as if outputs.size == 1 then the output is equal to
                    // the input ignoring the owner field due to the grouping.
                    return null;
                });
            } else if (cmd.getValue() instanceof Commands.Redeem) {
                // Redemption of the paper requires movement of on-ledger cash.
                State input = (State) inputs.get(0);
                Amount<Issued<Currency>> received = sumCashBy(tx.getOutputStates(), input.getOwner());

                if (Objects.isNull(timeWindow)) {
                    throw new IllegalArgumentException("Redemptions must be timestamped");
                }

                Instant time = timeWindow.getFromTime();
                requireThat(require -> {
                    require.using("the paper must have matured.", time.isAfter(input.getMaturityDate()));
                    require.using("the received amount must be equals the face value", received == input.getFaceValue());
                    require.using("the paper must be destroyed.", outputs.size() == 0);
                    require.using("the transaction is signed by the owner of the CP", cmd.getSigners().contains(input.getOwner().getOwningKey()));
                    return null;
                });
            } else if (cmd.getValue() instanceof Commands.Issue) {
                State output = (State) outputs.get(0);

                if (Objects.isNull(timeWindow)) {
                    throw new IllegalArgumentException("Redemptions must be timestamped");
                }

                Instant time = timeWindow.getUntilTime();
                requireThat(require -> {
                    // Don't allow people to issue commercial paper under other entities identities.
                    require.using("Output states are issued by a command signer", cmd.getSigners().contains(output.getIssuance().getParty().getOwningKey()));
                    require.using("Output values to some more than the input.", output.getFaceValue().getQuantity() > 0);
                    require.using("The maturity date is not in the past.", time.isBefore(output.getMaturityDate()));
                    // Don't allow an existing CP state to be replaced by this issuance.
                    require.using("Can't reissue an existing state", inputs.isEmpty());
                    return null;
                });
            } else {
                throw new IllegalArgumentException("Unrecognised command");
            }

        }
    }
}
