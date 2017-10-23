package com.writingcontract;

import kotlin.Unit;
import net.corda.core.contracts.PartyAndReference;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.finance.contracts.ICommercialPaperState;
import net.corda.finance.contracts.JavaCommercialPaper;
import net.corda.finance.contracts.asset.Cash;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static net.corda.finance.Currencies.DOLLARS;
import static net.corda.finance.Currencies.issuedBy;
import static net.corda.finance.contracts.JavaCommercialPaper.JCP_PROGRAM_ID;
import static net.corda.testing.CoreTestUtils.*;
import static net.corda.testing.NodeTestUtils.ledger;
import static net.corda.testing.NodeTestUtils.transaction;
import static net.corda.testing.TestConstants.*;
public class CommercialPaperTest {

    private final OpaqueBytes defaultRef = new OpaqueBytes(new byte[]{123});

    private ICommercialPaperState getPaper() {
        return new JavaCommercialPaper.State(getMEGA_CORP().ref(defaultRef),
                getMEGA_CORP(), issuedBy(DOLLARS(100)),
                getMEGA_CORP().ref(defaultRef),
                getTEST_TX_TIME().plus(7, ChronoUnit.DAYS));
    }

    @Test
    public void emptyLedger() {
        ledger(l -> {
            return Unit.INSTANCE; //We need to return this explicitly
        });
    }

//    @Test
//    public void simpleCPDoesntcompile() {
//        ICommercialPaperState inState = getPaper();
//        ledger(ledger -> {
//            ledger.transaction(tx -> {
//                tx.input(inState);
//            });
//
//            return Unit.INSTANCE;
//        });
//    }

    @Test
    public void simpleCP() {
        ICommercialPaperState inputState = getPaper();
        ledger(ledger -> {
            ledger.transaction(tx -> {
                tx.attachment(JCP_PROGRAM_ID);
                tx.input(JCP_PROGRAM_ID, inputState);
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void simpleCPMove() {
        ICommercialPaperState inputState = getPaper();
        ledger(ledger -> {
            ledger.transaction(tx -> {
                tx.input(JCP_PROGRAM_ID, inputState);
                tx.attachment(JCP_PROGRAM_ID);

                tx.command(getMEGA_CORP_PUBKEY(), new JavaCommercialPaper.Commands.Move());
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }


}
