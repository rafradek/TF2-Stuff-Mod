package rafradek.TF2weapons.decoration;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.weapons.ItemAmmo;
import rafradek.TF2weapons.weapons.ItemAmmoBelt;

public class ContainerWearables extends Container
{
    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    /** The crafting matrix inventory. */
    /** Determines if inventory manipulation should be handled. */
    public boolean isLocalWorld;
    private final EntityPlayer thePlayer;
    public IInventory wearables;

    public ContainerWearables(final InventoryPlayer playerInventory, final InventoryWearables wearables, boolean localWorld, EntityPlayer player)
    {
        this.isLocalWorld = localWorld;
        this.thePlayer = player;
        this.wearables=wearables;
        
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(wearables, 4+j + i * 3, 98 + j * 18, 18 + i * 18){
                	
                    public boolean isItemValid(@Nullable ItemStack stack)
                    {
                        if (stack==null || wearables.getStackInSlot(3)==null)
                        {
                            return false;
                        }
                        else
                        {
                            return stack.getItem() instanceof ItemAmmo;
                        }
                    }
                });
            }
        }

        for (int k = 0; k < 4; ++k)
        {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18)
            {
                /**
                 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1
                 * in the case of armor slots)
                 */
                public int getSlotStackLimit()
                {
                    return 1;
                }
                /**
                 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
                 */
                public boolean isItemValid(@Nullable ItemStack stack)
                {
                    if (stack == null)
                    {
                        return false;
                    }
                    else
                    {
                        return stack.getItem().isValidArmor(stack, entityequipmentslot, thePlayer);
                    }
                }
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }
        for(int k=0; k<3; k++){
        	this.addSlotToContainer(new Slot(wearables, k, 77, 8+k*18){
	        	public int getSlotStackLimit()
	            {
	                return 1;
	            }
	        	public void onSlotChanged()
	            {
	        		super.onSlotChanged();
	        		if(!thePlayer.worldObj.isRemote){
	        			//System.out.println("changed");
	        			TF2weapons.network.sendToAllAround(new TF2Message.WearableChangeMessage(thePlayer, this.getSlotIndex(), this.getStack()), new TargetPoint(thePlayer.dimension, thePlayer.posX, thePlayer.posY, thePlayer.posZ, 256));
	        		}
	            }
	            /**
	             * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	             */
	            public boolean isItemValid(@Nullable ItemStack stack)
	            {
	                if (stack == null)
	                {
	                    return false;
	                }
	                else
	                {
	                    return stack.getItem() instanceof ItemWearable;
	                }
	            }
	            @Nullable
	            @SideOnly(Side.CLIENT)
	            public String getSlotTexture()
	            {
	                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.HEAD.getIndex()];
	            }
	        });
        }
        for(int k=0; k<3; k++){
        	this.addSlotToContainer(new Slot(wearables, k, 77, 8+k*18){
	        	public int getSlotStackLimit()
	            {
	                return 1;
	            }
	        	public void onSlotChanged()
	            {
	        		super.onSlotChanged();
	        		if(!thePlayer.worldObj.isRemote){
	        			//System.out.println("changed");
	        			TF2weapons.network.sendToAllAround(new TF2Message.WearableChangeMessage(thePlayer, this.getSlotIndex(), this.getStack()), new TargetPoint(thePlayer.dimension, thePlayer.posX, thePlayer.posY, thePlayer.posZ, 256));
	        		}
	            }
	            /**
	             * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	             */
	            public boolean isItemValid(@Nullable ItemStack stack)
	            {
	                if (stack == null)
	                {
	                    return false;
	                }
	                else
	                {
	                    return stack.getItem() instanceof ItemWearable;
	                }
	            }
	            @Nullable
	            @SideOnly(Side.CLIENT)
	            public String getSlotTexture()
	            {
	                return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.HEAD.getIndex()];
	            }
	        });
        }
        this.addSlotToContainer(new Slot(wearables, 3, 154, 28){
        	public int getSlotStackLimit()
            {
                return 1;
            }
            /**
             * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
             */
            public boolean isItemValid(@Nullable ItemStack stack)
            {
                if (stack == null)
                {
                    return false;
                }
                else
                {
                    return stack.getItem() instanceof ItemAmmoBelt;
                }
            }
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return TF2weapons.MOD_ID+":items/ammo_belt_empty";
            }
        });
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.addSlotToContainer(new Slot(playerInventory, 40, 77, 62)
        {
            /**
             * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
             */
            public boolean isItemValid(@Nullable ItemStack stack)
            {
                return super.isItemValid(stack);
            }
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    /**
     * Called when the container is closed.
     */
    @SideOnly(Side.CLIENT)
    public void putStacksInSlots(ItemStack[] p_75131_1_)
    {
    	//System.out.println("Putting stacks");
    	super.putStacksInSlots(p_75131_1_);
    	
    }
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if(this.wearables.getStackInSlot(3)==null){
	        for (int i = 4; i < 13; ++i)
	        {
	            ItemStack itemstack = this.wearables.removeStackFromSlot(i);
	
	            if (itemstack != null)
	            {
	                playerIn.dropItem(itemstack, false);
	            }
	        }
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Nullable
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        
        
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 1 && index < 5)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return null;
                }
            }
            else if (index >= 5 && index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return null;
                }
            }
            else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !((Slot)this.inventorySlots.get(8 - entityequipmentslot.getIndex())).getHasStack())
            {
                int i = 8 - entityequipmentslot.getIndex();

                if (!this.mergeItemStack(itemstack1, i, i + 1, false))
                {
                    return null;
                }
            }
            else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND && !((Slot)this.inventorySlots.get(45)).getHasStack())
            {
                if (!this.mergeItemStack(itemstack1, 45, 46, false))
                {
                    return null;
                }
            }
            else if (index >= 9 && index < 36)
            {
                if (!this.mergeItemStack(itemstack1, 36, 45, false))
                {
                    return null;
                }
            }
            else if (index >= 36 && index < 45)
            {
                if (!this.mergeItemStack(itemstack1, 9, 36, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 9, 45, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
     * is null for the initial slot that was double-clicked.
     */
}