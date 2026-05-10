package com.firstclub.membership.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TierType {
    SILVER("Silver", 1),
    GOLD("Gold", 2),
    PLATINUM("Platinum", 3);

    private final String displayName;
    private final int rank;

    //function to get TierType by name
    public static TierType getByDisplayName(String displayName) {
        for (TierType tier : values()) {
            if (tier.getDisplayName().equalsIgnoreCase(displayName)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("No TierType with display name: " + displayName);
    }
}
