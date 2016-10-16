package rafradek.TF2weapons.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemCrate;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;

public class OpenCrateRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		boolean key=false;
		ItemStack crate=null;
		
		for(int x=0;x<inv.getSizeInventory();x++){
			ItemStack stack=inv.getStackInSlot(x);
			if(stack!=null){
				if(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==7){
					if(!key)
						key=true;
					else{
						return false;
					}
				}
				else{
					if(crate == null && stack.getItem() instanceof ItemCrate)
						crate=stack;
					else
						return false;
				}
			}
		}
		//System.out.println("matches "+(australium&&stack2!=null));
		return key && crate !=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		// TODO Auto-generated method stub
		ItemStack stack2=null;
		
		for(int x=0;x<inv.getSizeInventory();x++){
			ItemStack stack=inv.getStackInSlot(x);
			if(stack!=null){
				if(!(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==7)){
					stack2=stack;
				}
			}
		}
		//System.out.println("OutPut: "+stack2);
		if(stack2!=null){
			stack2=stack2.copy();
			stack2.getTagCompound().setBoolean("Open", true);
			stack2.stackSize=1;
		}
		return stack2;
	}

	@Override
	public int getRecipeSize() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		// TODO Auto-generated method stu
		//ItemStack stack=ItemFromData.getNewStack("crate1");
		//stack.getTagCompound().setBoolean("Open", true);
		return new ItemStack(TF2weapons.itemTF2,1,8);
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
