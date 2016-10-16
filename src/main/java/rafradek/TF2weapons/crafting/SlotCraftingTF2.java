package rafradek.TF2weapons.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;

public class SlotCraftingTF2 extends SlotCrafting {

	private InventoryCrafting craftMatrix;
	private EntityPlayer thePlayer;
	public SlotCraftingTF2(EntityPlayer player, InventoryCrafting craftingInventory, IInventory inventoryIn,
			int slotIndex, int xPosition, int yPosition) {
		super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
		thePlayer=player;
		craftMatrix=craftingInventory;
	}
	public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
		if(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==9){
			stack=ItemFromData.getRandomWeapon(playerIn.getRNG(),ItemFromData.VISIBLE_WEAPON);
			playerIn.inventory.setItemStack(stack);
	    }
		else if(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==10){
			stack=ItemFromData.getRandomWeaponOfClass("cosmetic",playerIn.getRNG(), false);
			playerIn.inventory.setItemStack(stack);
	    }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(playerIn, stack, craftMatrix);
        this.onCrafting(stack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
        ItemStack[] aitemstack = TF2CraftingManager.INSTANCE.getRemainingItems(this.craftMatrix, playerIn.worldObj);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < aitemstack.length; ++i)
        {
            ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
            ItemStack itemstack1 = aitemstack[i];

            if (itemstack != null)
            {
                this.craftMatrix.decrStackSize(i, 1);
                itemstack = this.craftMatrix.getStackInSlot(i);
            }

            if (itemstack1 != null)
            {
                if (itemstack == null)
                {
                    this.craftMatrix.setInventorySlotContents(i, itemstack1);
                }
                else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
                {
                    itemstack1.stackSize += itemstack.stackSize;
                    this.craftMatrix.setInventorySlotContents(i, itemstack1);
                }
                else if (!this.thePlayer.inventory.addItemStackToInventory(itemstack1))
                {
                    this.thePlayer.dropItem(itemstack1, false);
                }
            }
        }
    }
}
