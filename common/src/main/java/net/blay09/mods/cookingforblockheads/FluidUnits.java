package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.fluid.FluidTank;
import net.minecraft.nbt.CompoundTag;

import java.math.BigInteger;

public class FluidUnits {
    public static final int BUCKET = Balm.getPlatform().equals("fabric") ? 81000 : 1000;
    public static final int BOTTLE = BUCKET / 3;
    public static final int MILLIBUCKET = BUCKET / 1000;

    public static int toMillibuckets(int amount) {
        return amount / MILLIBUCKET;
    }

    public static CompoundTag serialize(FluidTank tank) {
        CompoundTag tag = tank.serialize();
        tag.putLong("Amount", tag.getInt("Amount"));
        tag.putString("Unit", BUCKET == 81000 ? "droplets" : "millibuckets");
        return tag;
    }

    public static void deserialize(FluidTank tank, CompoundTag tag, int capacity) {
        CompoundTag converted = tag.copy();
        int savedBucketVolume = switch (tag.getString("Unit")) {
            case "buckets" -> 1;
            case "droplets" -> 81000;
            case "", "millibuckets" -> 1000;
            default -> BUCKET;
        };
        BigInteger amount = BigInteger.valueOf(tag.getLong("Amount"))
                .multiply(BigInteger.valueOf(BUCKET))
                .divide(BigInteger.valueOf(savedBucketVolume));
        converted.putInt("Amount", amount.max(BigInteger.ZERO).min(BigInteger.valueOf(capacity)).intValueExact());
        tank.deserialize(converted);
    }
}
