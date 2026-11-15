package net.blay09.mods.cookingforblockheads.network.message;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.HashSet;

public class ToggleFavoriteMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleFavoriteMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
            CookingForBlockheads.MOD_ID,
            "toggle_favorite"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

    private final Holder<Item> item;
    private final boolean favorite;

    public ToggleFavoriteMessage(Holder<Item> item, boolean favorite) {
        this.item = item;
        this.favorite = favorite;
    }

    public static void encode(RegistryFriendlyByteBuf buf, ToggleFavoriteMessage message) {
        ITEM_STREAM_CODEC.encode(buf, message.item);
        buf.writeBoolean(message.favorite);
    }

    public static ToggleFavoriteMessage decode(RegistryFriendlyByteBuf buf) {
        final var item = ITEM_STREAM_CODEC.decode(buf);
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
            favoriteItemIds.add(ResourceLocation.parse(favoriteItemId));
        }
        Balm.getNetworking().sendTo(player, new FavoriteListMessage(favoriteItemIds));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
