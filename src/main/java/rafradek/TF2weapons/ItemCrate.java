package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.decoration.ItemWearable;

public class ItemCrate extends ItemFromData {
	public ItemCrate(){
		this.setMaxStackSize(64);
	}
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
		if(itemStackIn.getTagCompound().getBoolean("Open")){
			ArrayList<String> list=new ArrayList<String>();
			for(Entry<String, Integer> entry: getData(itemStackIn).crateContent.entrySet()){
				for(int i=0; i<entry.getValue();i++){
					list.add(entry.getKey());
				}
			}
			ItemStack stack=ItemCrate.getNewStack(list.get(playerIn.getRNG().nextInt(list.size())));
			if(!(stack.getItem() instanceof ItemWearable)){
				stack.getTagCompound().setBoolean("Strange", true);
			}
			if(!playerIn.inventory.addItemStackToInventory(stack)){
				playerIn.dropItem(stack, true);
			}
			itemStackIn.stackSize--;
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS,itemStackIn);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL,itemStackIn);
    }
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par2List, boolean par4)
    {	
		/*if (!par1ItemStack.hasTagCompound()) {
			par1ItemStack.getTagCompound()=new NBTTagCompound();
			par1ItemStack.getTagCompound().setTag("Attributes", (NBTTagCompound) ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy());
		}*/
        if (par1ItemStack.hasTagCompound())
        {
        	super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
        	
        	if(par1ItemStack.getTagCompound().getBoolean("Open")){
        		par2List.add("The crate is open now");
        		par2List.add("Right click to get the item");
        	}
        	
        	par2List.add("Possible content:");
        	for(String name: getData(par1ItemStack).crateContent.keySet()){
        		WeaponData data=MapList.nameToData.get(name);
        		if(data != null){
        			par2List.add(data.getString(PropertyType.NAME));
        		}
        	}
        }
    }
}
