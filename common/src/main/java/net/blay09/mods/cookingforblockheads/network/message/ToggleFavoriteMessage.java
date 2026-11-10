package net.blay09.mods.cookingforblockheads.network.message;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.HashSet;

public record ToggleFavoriteMessage(Holder<Item> item, boolean favorite) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleFavoriteMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(
            CookingForBlockheads.MOD_ID,
            "toggle_favorite"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleFavoriteMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            ToggleFavoriteMessage::item,
            ByteBufCodecs.BOOL,
            ToggleFavoriteMessage::favorite,
            ToggleFavoriteMessage::new
    );

    public static void handle(ServerPlayer player, ToggleFavoriteMessage message) {
        final var itemId = message.item.unwrapKey().orElseThrow().identifier().toString();
        final var data = Balm.hooks().getPersistentData(player);
        final var cfbData = data.getCompoundOrEmpty("CookingForBlockheads");
        final var favoriteItems = cfbData.getCompoundOrEmpty("FavoriteItemIds");
        if (message.favorite) {
            favoriteItems.putBoolean(itemId, true);
        } else {
            favoriteItems.remove(itemId);
        }
        cfbData.put("FavoriteItemIds", favoriteItems);
        data.put("CookingForBlockheads", cfbData);

        final var favoriteItemIds = new HashSet<Identifier>();
        for (final var favoriteItemId : favoriteItems.keySet()) {
            favoriteItemIds.add(Identifier.parse(favoriteItemId));
        }
        Balm.networking().sendTo(player, new FavoriteListMessage(favoriteItemIds));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
