package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.fluid.DefaultFluidTank;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.math.BigInteger;

public class FluidUnits {
    public static final int BUCKET = Balm.platform().name().equals("fabric") ? 81000 : 1000;
    public static final int BOTTLE = BUCKET / 3;
    public static final int MILLIBUCKET = BUCKET / 1000;

    public static int toMillibuckets(int amount) {
        return amount / MILLIBUCKET;
    }

    public static void serialize(DefaultFluidTank tank, ValueOutput output) {
        tank.serialize(output);
        output.putString("Unit", BUCKET == 81000 ? "droplets" : "millibuckets");
    }

    public static void deserialize(DefaultFluidTank tank, ValueInput input, int capacity) {
        int savedBucketVolume = switch (input.getStringOr("Unit", "")) {
            case "buckets" -> 1;
            case "droplets" -> 81000;
            case "", "millibuckets" -> 1000;
            default -> BUCKET;
        };
        BigInteger amount = BigInteger.valueOf(input.getLongOr("Amount", 0))
                .multiply(BigInteger.valueOf(BUCKET))
                .divide(BigInteger.valueOf(savedBucketVolume));
        tank.deserialize(input);
        tank.setAmount(0, amount.max(BigInteger.ZERO).min(BigInteger.valueOf(capacity)).intValueExact());
    }
}
