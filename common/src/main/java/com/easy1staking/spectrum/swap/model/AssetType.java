package com.easy1staking.spectrum.swap.model;

import com.bloxbean.cardano.client.util.HexUtil;

public record AssetType(String policyId, String assetName) {

    private static final AssetType Ada = new AssetType("lovelace", "");

    public String toUnit() {
        return policyId + assetName;
    }

    public boolean isAda() {
        return policyId.isBlank() && assetName.isBlank();
    }

    public String unsafeHumanAssetName() {
        return new String(HexUtil.decodeHexString(assetName));
    }

    public static AssetType fromUnit(String unit) {
        if (unit.equalsIgnoreCase("lovelace")) {
            return Ada;
        } else {
            String sanitizedUnit = unit.replaceAll("\\.", "");
            return new AssetType(sanitizedUnit.substring(0, 56), sanitizedUnit.substring(56));
        }
    }

    public static AssetType ada() {
        return Ada;
    }

}
