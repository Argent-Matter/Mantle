package slimeknights.mantle.transfer.item.wrapper;

import javax.annotation.Nonnull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes the player inventory WITHOUT the armor inventory as IItemHandler.
 * Also takes core of inserting/extracting having the same logic as picking up items.
 */
public class PlayerMainInvWrapper extends RangedWrapper {
	private final Inventory inventoryPlayer;

	public PlayerMainInvWrapper(Inventory inv) {
		super(new InvWrapper(inv), 0, inv.items.size());
		inventoryPlayer = inv;
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		ItemStack rest = super.insertItem(slot, stack, simulate);
		if (rest.getCount() != stack.getCount()) {
			// the stack in the slot changed, animate it
			ItemStack inSlot = getStackInSlot(slot);
			if (!inSlot.isEmpty()) {
				if (getInventoryPlayer().player.level.isClientSide) {
					inSlot.setPopTime(5);
				} else if (getInventoryPlayer().player instanceof ServerPlayer) {
					getInventoryPlayer().player.containerMenu.broadcastChanges();
				}
			}
		}
		return rest;
	}

	public Inventory getInventoryPlayer() {
		return inventoryPlayer;
	}
}
