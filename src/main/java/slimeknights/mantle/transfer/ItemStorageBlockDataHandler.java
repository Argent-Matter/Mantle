package slimeknights.mantle.transfer;

import io.github.fabricators_of_create.porting_lib.PortingLib;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTransferable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.transfer.item.IItemHandler;

import java.util.HashMap;
import java.util.Map;

public class ItemStorageBlockDataHandler {
  public static final ResourceLocation PACKET_ID = PortingLib.id("fluid_tile_handler_data");

  @Environment(EnvType.CLIENT)
  private static final Map<BlockPos, ItemStack[]> CACHED_DATA = new HashMap<>();

  public static void sendDataToClients(BlockEntity be) {
    ((ServerLevel)be.getLevel()).getPlayers(player -> {
      IItemHandler handler = TransferUtil.getItemHandler(be).orElse(null);
      ItemStack[] itemData = new ItemStack[handler.getSlots()];
      for (int i = 0; i < itemData.length; i++) {
        itemData[i] = handler.getStackInSlot(i);
      }
      ServerPlayNetworking.send(player, PACKET_ID, createPacket(itemData, be));
      return true;
    });
  }

  public static FriendlyByteBuf createPacket(ItemStack[] data, BlockEntity be) {
    FriendlyByteBuf buf = PacketByteBufs.create();
    buf.writeInt(data.length);
    for (ItemStack item : data) {
      buf.writeItem(item);
    }
    buf.writeBlockPos(be.getBlockPos());
    return buf;
  }

  public static ItemStack[] readPacket(FriendlyByteBuf buf) {
    ItemStack[] data = new ItemStack[buf.readInt()];
    for (int i = 0; i < data.length; i++) {
      data[i] = buf.readItem();
    }
    return data;
  }

  public static IItemHandler getCachedHandler(BlockEntity be) {
    ItemStack[] data = CACHED_DATA.get(be.getBlockPos());
    return new IItemHandler() {

      @Override
      public int getSlots() {
        return data.length;
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
        return data[slot];
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
        return null;
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean sim) {
        return null;
      }

      @Override
      public int getSlotLimit(int slot) {
        return data[slot].getMaxStackSize();
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return false;
      }
    };
  }

  @Environment(EnvType.CLIENT)
  public static void initClient() {
    ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, (client, handler, buf, sender) -> {
      ItemStack[] data = readPacket(buf);
      BlockPos pos = buf.readBlockPos();
      client.execute(() -> CACHED_DATA.put(pos, data));
    });
  }

  public static void init() {
    ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
      if (!blockEntity.getLevel().isClientSide())
        sendDataToClients(blockEntity);
      return null;
    });
  }
}
