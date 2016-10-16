package rafradek.TF2weapons.crafting;

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
import rafradek.TF2weapons.weapons.ItemCloak;
import rafradek.TF2weapons.weapons.ItemUsable;

public class AustraliumRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int australium=0;
		ItemStack stack2=null;
		
		for(int x=0;x<inv.getSizeInventory();x++){
			ItemStack stack=inv.getStackInSlot(x);
			if(stack!=null){
				if(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==2){
					if(australium<8)
						australium++;
					else{
						return false;
					}
				}
				else{
					if(stack2==null&&(stack.getItem() instanceof ItemTool ||stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow || stack.getItem() instanceof ItemUsable || stack.getItem() instanceof ItemCloak ))
						stack2=stack;
					else
						return false;
				}
			}
		}
		//System.out.println("matches "+(australium&&stack2!=null));
		return australium==8&&stack2!=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		// TODO Auto-generated method stub
		ItemStack stack2=null;
		
		for(int x=0;x<inv.getSizeInventory();x++){
			ItemStack stack=inv.getStackInSlot(x);
			if(stack!=null){
				if(!(stack.getItem()==TF2weapons.itemTF2&&stack.getMetadata()==2)){
					stack2=stack;
				}
			}
		}
		//System.out.println("OutPut: "+stack2);
		if(stack2!=null){
			stack2=stack2.copy();
			if(!stack2.hasTagCompound()){
				stack2.setTagCompound(new NBTTagCompound());
			}
			stack2.stackSize=1;
			stack2.getTagCompound().setBoolean("Australium", true);
			stack2.getTagCompound().setBoolean("Strange", true);
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
		ItemStack stack=ItemFromData.getNewStack("minigun");
		stack.getTagCompound().setBoolean("Australium", true);
		stack.getTagCompound().setBoolean("Strange", true);
		return stack;
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
