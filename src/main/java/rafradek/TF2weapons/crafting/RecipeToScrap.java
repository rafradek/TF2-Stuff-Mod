package rafradek.TF2weapons.crafting;

import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemDisguiseKit;

public class RecipeToScrap implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ArrayList<ItemStack> stacks=new ArrayList<>();
		
		for(int x=0;x<inv.getSizeInventory();x++){
			ItemStack stack=inv.getStackInSlot(x);
			if(stack!=null){
				if(stacks.size()<2&&stack.getItem() instanceof ItemFromData){
					stacks.add(stack);
				}
				else{
					return false;
				}
			}
		}
		//System.out.println("matches "+(australium&&stack2!=null));
		return stacks.size()==2;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		// TODO Auto-generated method stub
		return new ItemStack(TF2weapons.itemTF2,1,3);
	}

	@Override
	public int getRecipeSize() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		// TODO Auto-generated method stu
		return new ItemStack(TF2weapons.itemTF2,1,3);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		// TODO Auto-generated method stub
		ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            aitemstack[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack);
        }

        return aitemstack;
	}

}
