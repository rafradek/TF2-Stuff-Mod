package rafradek.TF2weapons.crafting;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.building.ItemBuildingBox;

public class ContainerTF2Workbench extends Container
{
    /** The crafting matrix inventory (3x3). */
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    private final World worldObj;
    /** Position of the workbench */
    private final BlockPos pos;
    public EntityPlayer player;
	public int currentRecipe=-1;

    public ContainerTF2Workbench(EntityPlayer player,InventoryPlayer playerInventory, World worldIn, BlockPos posIn)
    {
    	this.player=player;
        this.worldObj = worldIn;
        this.pos = posIn;
        this.addSlotToContainer(new SlotCraftingTF2(playerInventory.player, this.craftMatrix, this.craftResult, 0, 148, 41));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 86 + j * 18, 23 + i * 18){
                	
                });
            }
        }

        /*for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k * 18));
            }
        }*/
        
        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 98 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 156));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
    	ItemStack stack=null;
    	List<IRecipe> recipes=TF2CraftingManager.INSTANCE.getRecipeList();
    	if(currentRecipe>=0&&currentRecipe<recipes.size()&&recipes.get(currentRecipe).matches(this.craftMatrix, worldObj)){
    		stack=getReplacement(recipes.get(currentRecipe).getCraftingResult(this.craftMatrix));
    		//?TF2CraftingManager.INSTANCE.getRecipeList().get(currentRecipe)TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix, this.worldObj);
    	}
    	else{
    		stack=getReplacement(TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix, this.worldObj));
    	}
        this.craftResult.setInventorySlotContents(0, stack);
    }

    public ItemStack getReplacement(ItemStack stack){
    	if(stack!=null && stack.getItem() instanceof ItemBuildingBox&&this.player.getTeam()==this.player.worldObj.getScoreboard().getTeam("BLU")){
    		stack.setItemDamage(stack.getItemDamage()+1);
    	}
    	return stack;
    }
    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack != null)
                {
                    playerIn.dropItem(itemstack, false);
                }
            }
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.worldObj.getBlockState(this.pos).getBlock() != TF2weapons.blockCabinet ? false : playerIn.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
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
            
            if(itemstack1.getItem()==TF2weapons.itemTF2&&itemstack1.getMetadata()==9){
        		itemstack1=ItemFromData.getRandomWeapon(playerIn.getRNG(),ItemFromData.VISIBLE_WEAPON);
        	}
        	else if(itemstack1.getItem()==TF2weapons.itemTF2&&itemstack1.getMetadata()==10){
        		itemstack1=ItemFromData.getRandomWeaponOfClass("cosmetic",playerIn.getRNG(), false);
        	}
            
            itemstack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 10 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false))
                {
                    return null;
                }
            }
            else if (index >= 37 && index < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false))
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
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
