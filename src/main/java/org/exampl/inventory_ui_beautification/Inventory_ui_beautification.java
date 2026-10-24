package org.exampl.inventory_ui_beautification;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Inventory_ui_beautification implements ModInitializer {

    public static final String MOD_ID = "inventory_ui_beautification";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public record SortRequestPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SortRequestPayload> TYPE =
                new CustomPacketPayload.Type<>(id("sort_request"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SortRequestPayload> CODEC =
                StreamCodec.unit(new SortRequestPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TakeAllPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TakeAllPayload> TYPE =
                new CustomPacketPayload.Type<>(id("take_all"));

        public static final StreamCodec<RegistryFriendlyByteBuf, TakeAllPayload> CODEC =
                StreamCodec.unit(new TakeAllPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DepositAllPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<DepositAllPayload> TYPE =
                new CustomPacketPayload.Type<>(id("deposit_all"));

        public static final StreamCodec<RegistryFriendlyByteBuf, DepositAllPayload> CODEC =
                StreamCodec.unit(new DepositAllPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record RestockPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RestockPayload> TYPE =
                new CustomPacketPayload.Type<>(id("restock"));

        public static final StreamCodec<RegistryFriendlyByteBuf, RestockPayload> CODEC =
                StreamCodec.unit(new RestockPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TakeStackPayload(int slotIndex) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TakeStackPayload> TYPE =
                new CustomPacketPayload.Type<>(id("take_stack"));

        public static final StreamCodec<RegistryFriendlyByteBuf, TakeStackPayload> CODEC =
                StreamCodec.of((buf, payload) -> buf.writeInt(payload.slotIndex()),
                        buf -> new TakeStackPayload(buf.readInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StructureRequestPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StructureRequestPayload> TYPE =
                new CustomPacketPayload.Type<>(id("structure_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StructureRequestPayload> CODEC =
                StreamCodec.unit(new StructureRequestPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record StructureInfoPayload(String line1, String line2) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<StructureInfoPayload> TYPE =
                new CustomPacketPayload.Type<>(id("structure_info"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StructureInfoPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, StructureInfoPayload::line1,
                        ByteBufCodecs.STRING_UTF8, StructureInfoPayload::line2,
                        StructureInfoPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(SortRequestPayload.TYPE, SortRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TakeAllPayload.TYPE, TakeAllPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DepositAllPayload.TYPE, DepositAllPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RestockPayload.TYPE, RestockPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TakeStackPayload.TYPE, TakeStackPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StructureRequestPayload.TYPE, StructureRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StructureInfoPayload.TYPE, StructureInfoPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE, (payload, context) ->
                sortCurrentContainer(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(TakeAllPayload.TYPE, (payload, context) ->
                takeAll(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(DepositAllPayload.TYPE, (payload, context) ->
                depositAll(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(RestockPayload.TYPE, (payload, context) ->
                restock(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(TakeStackPayload.TYPE, (payload, context) ->
                takeStack(context.player(), payload.slotIndex()));
        ServerPlayNetworking.registerGlobalReceiver(StructureRequestPayload.TYPE, (payload, context) ->
                handleStructureRequest(context.player()));
    }

    private void handleStructureRequest(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        Registry<Structure> reg = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        BlockPos pos = player.blockPosition();
        ResourceKey<Level> dim = level.dimension();

        String l1 = "", l2 = "";
        if (dim == Level.OVERWORLD) {
            BlockPos rp = findNearest(level, reg, pos,
                    BuiltinStructures.RUINED_PORTAL_STANDARD, BuiltinStructures.RUINED_PORTAL_DESERT,
                    BuiltinStructures.RUINED_PORTAL_JUNGLE, BuiltinStructures.RUINED_PORTAL_SWAMP,
                    BuiltinStructures.RUINED_PORTAL_MOUNTAIN, BuiltinStructures.RUINED_PORTAL_OCEAN,
                    BuiltinStructures.RUINED_PORTAL_NETHER);
            l1 = formatLine("废弃传送门", rp, pos);
            BlockPos sh = findNearest(level, reg, pos, BuiltinStructures.STRONGHOLD);
            l2 = formatLine("末地要塞", sh, pos);
        } else if (dim == Level.NETHER) {
            BlockPos ft = findNearest(level, reg, pos, BuiltinStructures.FORTRESS);
            l1 = formatLine("下界要塞", ft, pos);
            BlockPos bs = findNearest(level, reg, pos, BuiltinStructures.BASTION_REMNANT);
            l2 = formatLine("堡垒遗迹", bs, pos);
        } else if (dim == Level.END) {
            BlockPos ec = findNearest(level, reg, pos, BuiltinStructures.END_CITY);
            l1 = formatLine("末地城", ec, pos);
            l2 = "";
        }

        ServerPlayNetworking.send(player, new StructureInfoPayload(l1, l2));
    }

    @SafeVarargs
    private final BlockPos findNearest(ServerLevel level, Registry<Structure> reg, BlockPos pos,
                                       ResourceKey<Structure>... keys) {
        List<Holder<Structure>> holders = new ArrayList<>();
        for (ResourceKey<Structure> k : keys) {
            Structure s = reg.getOptional(k).orElse(null);
            if (s != null) holders.add(Holder.direct(s));
        }
        if (holders.isEmpty()) return null;
        HolderSet<Structure> hs = HolderSet.direct(holders);
        Pair<BlockPos, ?> result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, hs, pos, 100, false);
        return result == null ? null : result.getFirst();
    }

    private String formatLine(String name, BlockPos target, BlockPos from) {
        if (target == null) return name + ": 未找到";
        int dx = target.getX() - from.getX();
        int dz = target.getZ() - from.getZ();
        int dist = (int) Math.sqrt((double) dx * dx + dz * dz);
        return name + ": " + target.getX() + ", " + target.getZ() + " (" + dist + "m)";
    }

    private void sortCurrentContainer(ServerPlayer player) {
        var inv = player.getInventory();
        AbstractContainerMenu menu = player.containerMenu;

        List<ItemStack> main = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) main.add(s.copy());
        }
        List<ItemStack> sortedMain = mergeAndSort(main);
        for (int i = 9; i < 36; i++) inv.setItem(i, ItemStack.EMPTY);
        int idx = 0;
        for (ItemStack s : sortedMain) {
            if (idx >= 27) break;
            inv.setItem(9 + idx, s);
            idx++;
        }

        if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu
                || menu instanceof HopperMenu || menu instanceof DispenserMenu) {
            int containerCount = menu.slots.size() - 36;
            List<Slot> containerSlots = new ArrayList<>();
            for (int i = 0; i < containerCount; i++) {
                containerSlots.add(menu.slots.get(i));
            }
            List<ItemStack> box = new ArrayList<>();
            for (Slot slot : containerSlots) {
                ItemStack s = slot.getItem();
                if (!s.isEmpty()) box.add(s.copy());
            }
            List<ItemStack> sortedBox = mergeAndSort(box);
            for (Slot slot : containerSlots) slot.set(ItemStack.EMPTY);
            idx = 0;
            for (ItemStack s : sortedBox) {
                if (idx >= containerSlots.size()) break;
                containerSlots.get(idx).set(s);
                idx++;
            }
        }

        menu.broadcastChanges();
        inv.setChanged();
    }

    private List<ItemStack> mergeAndSort(List<ItemStack> raw) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack s : raw) {
            ItemStack src = s.copy();
            for (ItemStack m : merged) {
                if (src.isEmpty()) break;
                if (ItemStack.isSameItemSameComponents(m, src)) {
                    int can = m.getMaxStackSize() - m.getCount();
                    int move = Math.min(can, src.getCount());
                    if (move > 0) {
                        m.grow(move);
                        src.shrink(move);
                    }
                }
            }
            if (!src.isEmpty()) merged.add(src);
        }
        merged.sort(Comparator
                .comparing((ItemStack s) -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString())
                .thenComparing(ItemStack::getCount, Comparator.reverseOrder()));
        return merged;
    }

    private List<Slot> containerSlotsOf(AbstractContainerMenu menu) {
        if (!(menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu || menu instanceof HopperMenu
                || menu instanceof DispenserMenu || menu instanceof FurnaceMenu || menu instanceof SmokerMenu
                || menu instanceof BlastFurnaceMenu || menu instanceof BrewingStandMenu)) {
            return List.of();
        }
        int count = menu.slots.size() - 36;
        if (count <= 0) return List.of();
        List<Slot> out = new ArrayList<>();
        for (int i = 0; i < count; i++) out.add(menu.slots.get(i));
        return out;
    }

    private List<Slot> playerSlotsOf(AbstractContainerMenu menu) {
        int count = menu.slots.size() - 36;
        List<Slot> out = new ArrayList<>();
        for (int i = count; i < menu.slots.size(); i++) out.add(menu.slots.get(i));
        return out;
    }

    private ItemStack mergeInto(List<Slot> dest, ItemStack src) {
        ItemStack left = src.copy();
        if (left.isEmpty()) return left;
        for (Slot slot : dest) {
            if (left.isEmpty()) break;
            ItemStack cur = slot.getItem();
            if (!cur.isEmpty() && ItemStack.isSameItemSameComponents(cur, left)
                    && cur.getCount() < cur.getMaxStackSize()) {
                int move = Math.min(cur.getMaxStackSize() - cur.getCount(), left.getCount());
                cur.grow(move);
                left.shrink(move);
                slot.setChanged();
            }
        }
        for (Slot slot : dest) {
            if (left.isEmpty()) break;
            if (slot.getItem().isEmpty() && slot.mayPlace(left)) {
                slot.set(left.copy());
                left = ItemStack.EMPTY;
            }
        }
        return left;
    }

    private void takeAll(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> container = containerSlotsOf(menu);
        List<Slot> playerSlots = playerSlotsOf(menu);
        if (container.isEmpty()) return;
        for (Slot slot : container) {
            if (slot.hasItem()) {
                slot.set(mergeInto(playerSlots, slot.getItem()));
            }
        }
        menu.broadcastChanges();
        player.getInventory().setChanged();
    }

    private void depositAll(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> container = containerSlotsOf(menu);
        List<Slot> playerSlots = playerSlotsOf(menu);
        if (container.isEmpty()) return;
        for (Slot slot : playerSlots) {
            if (slot.hasItem()) {
                slot.set(mergeInto(container, slot.getItem()));
            }
        }
        menu.broadcastChanges();
        player.getInventory().setChanged();
    }

    private void restock(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> container = containerSlotsOf(menu);
        List<Slot> playerSlots = playerSlotsOf(menu);
        if (container.isEmpty()) return;
        int hotbarStart = playerSlots.size() - 9;
        for (int i = hotbarStart; i < playerSlots.size(); i++) {
            Slot hot = playerSlots.get(i);
            ItemStack cur = hot.getItem();
            if (cur.isEmpty()) continue;
            int need = cur.getMaxStackSize() - cur.getCount();
            if (need <= 0) continue;
            for (Slot slot : container) {
                if (need <= 0) break;
                ItemStack cs = slot.getItem();
                if (!cs.isEmpty() && ItemStack.isSameItemSameComponents(cur, cs)) {
                    int move = Math.min(need, cs.getCount());
                    cs.shrink(move);
                    cur.grow(move);
                    slot.setChanged();
                    need -= move;
                }
            }
        }
        menu.broadcastChanges();
        player.getInventory().setChanged();
    }

    private void takeStack(ServerPlayer player, int slotIndex) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> container = containerSlotsOf(menu);
        if (container.isEmpty() || slotIndex < 0 || slotIndex >= container.size()) return;
        Slot hovered = container.get(slotIndex);
        ItemStack base = hovered.getItem();
        if (base.isEmpty()) return;
        ItemStack stack = base.copy();
        stack.setCount(0);
        for (Slot slot : container) {
            if (stack.getCount() >= stack.getMaxStackSize()) break;
            ItemStack cs = slot.getItem();
            if (!cs.isEmpty() && ItemStack.isSameItemSameComponents(base, cs)) {
                int move = Math.min(stack.getMaxStackSize() - stack.getCount(), cs.getCount());
                cs.shrink(move);
                slot.setChanged();
                stack.grow(move);
            }
        }
        ItemStack left = mergeInto(playerSlotsOf(menu), stack);
        if (!left.isEmpty()) {
            mergeInto(container, left);
        }
        menu.broadcastChanges();
        player.getInventory().setChanged();
    }
}
