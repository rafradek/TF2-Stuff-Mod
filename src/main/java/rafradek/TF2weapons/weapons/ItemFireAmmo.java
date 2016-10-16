package rafradek.TF2weapons.weapons;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.characters.EntityTF2Character;

public class ItemFireAmmo extends ItemAmmo {
	
	int uses;
	int type;
	public ItemFireAmmo(int type, int uses){
		this.type=type;
		this.uses=uses;
		this.setHasSubtypes(false);
	}
	public int getTypeInt(ItemStack stack){
		return type;
	}
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
		par3List.add(new ItemStack(this));
    }
	public int getItemStackLimit(ItemStack stack){
		return 1;
	}
	public int getMaxDamage(ItemStack stack){
		return uses;
	}
	public void consumeAmmo(EntityLivingBase living,ItemStack stack,int amount){
		if(stack==STACK_FILL) return;
		if(amount>0){
			stack.damageItem(amount, living);
			
			if(stack.stackSize<=0&&living instanceof EntityPlayer){
				IInventory invAmmo=living.getCapability(TF2weapons.INVENTORY_CAP, null);
				if(invAmmo.getStackInSlot(3)!=null){
					for(int i=4;i<invAmmo.getSizeInventory();i++){
						ItemStack stackInv=invAmmo.getStackInSlot(i);
						if(stack==stackInv){
							invAmmo.setInventorySlotContents(i, null);
							return;
						}
					}
				}
				((EntityPlayer)living).inventory.deleteStack(stack);
				
			}
		}
	}
}
