package net.blay09.mods.cookingforblockheads.menu;

import net.blay09.mods.cookingforblockheads.api.Kitchen;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface KitchenMenuAccess {
    KitchenMenuAccess NULL = new KitchenMenuAccess() {
        public <T> Optional<T> evaluate(BiFunction<Kitchen, ServerKitchenMenuState, T> action) {
            return Optional.empty();
        }
    };

    static KitchenMenuAccess create(Kitchen kitchen, ServerKitchenMenuState state) {
        return create(kitchen, state, _ -> true);
    }

    static KitchenMenuAccess create(Kitchen kitchen, ServerKitchenMenuState state, Predicate<Player> validityCheck) {
        return new KitchenMenuAccess() {
            public <T> Optional<T> evaluate(BiFunction<Kitchen, ServerKitchenMenuState, @Nullable T> action) {
                return Optional.ofNullable(action.apply(kitchen, state));
            }

            @Override
            public boolean stillValid(Player player) {
                return validityCheck.test(player);
            }
        };
    }

    default boolean stillValid(Player player) {
        return true;
    }

    <T> Optional<T> evaluate(BiFunction<Kitchen, ServerKitchenMenuState, T> evaluator);

    default <T> T evaluate(BiFunction<Kitchen, ServerKitchenMenuState, T> action, T defaultValue) {
        return this.evaluate(action).orElse(defaultValue);
    }

    default void execute(BiConsumer<Kitchen, ServerKitchenMenuState> action) {
        this.evaluate((kitchen, state) -> {
            action.accept(kitchen, state);
            return Optional.empty();
        });
    }
}
