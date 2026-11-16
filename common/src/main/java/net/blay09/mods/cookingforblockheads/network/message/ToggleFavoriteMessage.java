package net.blay09.mods.cookingforblockheads.network.message;

import net.blay09.mods.balm.api.Balm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.HashSet;

public class ToggleFavoriteMessage {

    private final Holder<Item> item;
    private final boolean favorite;

    public ToggleFavoriteMessage(Holder<Item> item, boolean favorite) {
        this.item = item;
        this.favorite = favorite;
    }

    public static void encode(ToggleFavoriteMessage message, FriendlyByteBuf buf) {
        buf.writeId(BuiltInRegistries.ITEM.asHolderIdMap(), message.item);
        buf.writeBoolean(message.favorite);
    }

    public static ToggleFavoriteMessage decode(FriendlyByteBuf buf) {
        final var item = buf.readById(BuiltInRegistries.ITEM.asHolderIdMap());
        final var favorite = buf.readBoolean();
        return new ToggleFavoriteMessage(item, favorite);
    }

    public static void handle(ServerPlayer player, ToggleFavoriteMessage message) {
        final var itemId = message.item.unwrapKey().orElseThrow().location().toString();
        final var data = Balm.getHooks().getPersistentData(player);
        final var cfbData = data.getCompound("CookingForBlockheads");
        final var favoriteItems = cfbData.getCompound("FavoriteItemIds");
        if (message.favorite) {
            favoriteItems.putBoolean(itemId, true);
        } else {
            favoriteItems.remove(itemId);
        }
        cfbData.put("FavoriteItemIds", favoriteItems);
        data.put("CookingForBlockheads", cfbData);

        final var favoriteItemIds = new HashSet<ResourceLocation>();
        for (final var favoriteItemId : favoriteItems.getAllKeys()) {
            favoriteItemIds.add(new ResourceLocation(favoriteItemId));
        }
        Balm.getNetworking().sendTo(player, new FavoriteListMessage(favoriteItemIds));
    }

}
