package com.tutorial.writingcontract;

import net.corda.core.contracts.CommandData;

public class Commands implements CommandData {
    public static class Move extends Commands {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Move;
        }
    }

    public static class Redeem extends Commands {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Redeem;
        }
    }

    public static class Issue extends Commands {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Issue;
        }
    }
}
