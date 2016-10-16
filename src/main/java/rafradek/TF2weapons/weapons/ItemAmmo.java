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
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.characters.EntityTF2Character;

public class ItemAmmo extends Item {
	
	public static final String[] AMMO_TYPES=new String[]{"none","shotgun","minigun","pistol","revolver","smg","sniper","rocket","grenade","syringe","fire","sticky","medigun","flare","ball"};
	public static final int[] AMMO_MAX_STACK=new int[]{64,64,64,64,64,64,16,24,24,64,1,32,1,64,64};
	public static ItemStack STACK_FILL;
	
	public ItemAmmo(){
		this.setHasSubtypes(true);
	}
	public String getType(ItemStack stack){
		return AMMO_TYPES[this.getTypeInt(stack)];
	}
	public int getTypeInt(ItemStack stack){
		return stack.getMetadata();
	}
	public boolean isValidForWeapon(ItemStack ammo,ItemStack weapon){
		return getTypeInt(ammo)==ItemFromData.getData(weapon).getInt(PropertyType.AMMO_TYPE);
	}
	@SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab()
    {
        return TF2weapons.tabsurvivaltf2;
    }
	
	public String getUnlocalizedName(ItemStack stack)
    {
		return "item.tf2ammo."+getType(stack);
    }
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
		//System.out.println(this.getCreativeTab());
		for(int i=1;i<AMMO_TYPES.length;i++){
			if(i!=10&&i!=12){
				par3List.add(new ItemStack(this,1,i));
			}
		}
    }
	public int getItemStackLimit(ItemStack stack){
		return AMMO_MAX_STACK[stack.getMetadata()];
	}
	public void consumeAmmo(EntityLivingBase living,ItemStack stack,int amount){
		if(stack==STACK_FILL) return;
		//if(EntityDispenser.isNearDispenser(living.worldObj, living)) return;
		if(amount>0){
			stack.stackSize-=amount;
			
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
	public static void consumeAmmoGlobal(EntityLivingBase living,ItemStack stack,int amount){
		if(EntityDispenser.isNearDispenser(living.worldObj, living)) return;
		if(!(living instanceof EntityPlayer)) return;
		if(amount>0){
			amount=((ItemWeapon)stack.getItem()).getActualAmmoUse(stack, living, amount);
			//int type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);
			
			//stack.stackSize-=amount;
			ItemStack stackAmmo;
			while(amount>0&&(stackAmmo=searchForAmmo(living,stack))!=null){
				int inStack=stackAmmo.stackSize;
				((ItemAmmo)stackAmmo.getItem()).consumeAmmo(living,stackAmmo,amount);
				amount-=inStack;
			}
		}
	}
	public static ItemStack searchForAmmo(EntityLivingBase owner, ItemStack stack){
		if(EntityDispenser.isNearDispenser(owner.worldObj, owner)) return STACK_FILL;
		
		if(!(owner instanceof EntityPlayer)) return STACK_FILL;
		
		int type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);
		
		if(type == 0) return STACK_FILL;
		
		if(owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)!=null){
			for(int i=4;i<owner.getCapability(TF2weapons.INVENTORY_CAP, null).getSizeInventory();i++){
				ItemStack stackCap=owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(i);
				if(stackCap !=null && stackCap.getItem() instanceof ItemAmmo&&((ItemAmmo)stackCap.getItem()).getTypeInt(stackCap)==type){
					return stackCap;
				}
			}
		}
		
		for(int i=0;i<((EntityPlayer)owner).inventory.mainInventory.length;i++){
			ItemStack stackInv=((EntityPlayer)owner).inventory.mainInventory[i];
			if(stackInv !=null && stackInv.getItem() instanceof ItemAmmo&&((ItemAmmo)stackInv.getItem()).getTypeInt(stackInv)==type){
				return stackInv;
			}
		}
		return null;
	}
	public static int getAmmoAmount(EntityLivingBase owner, ItemStack stack){
		if(EntityDispenser.isNearDispenser(owner.worldObj, owner)) return 900;
		
		if(!(owner instanceof EntityPlayer)) return ((EntityTF2Character)owner).ammoLeft;
		
		int type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);
		
		if(type==0) return 900;
		
		int ammoCount=0;
		
		if(owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)!=null){
			for(int i=4;i<owner.getCapability(TF2weapons.INVENTORY_CAP, null).getSizeInventory();i++){
				ItemStack stackCap=owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(i);
				if(stackCap !=null && stackCap.getItem() instanceof ItemAmmo&&((ItemAmmo)stackCap.getItem()).getTypeInt(stackCap)==type){
					ammoCount+=stackCap.stackSize;
				}
			}
		}
		
		for(int i=0;i<((EntityPlayer)owner).inventory.mainInventory.length;i++){
			ItemStack stackInv=((EntityPlayer)owner).inventory.mainInventory[i];
			if(stackInv !=null && stackInv.getItem() instanceof ItemAmmo&&((ItemAmmo)stackInv.getItem()).getTypeInt(stackInv)==type){
				ammoCount+=stackInv.stackSize;
			}
		}
		return (int) (ammoCount/TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));
	}
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world,EntityPlayer living,EnumHand hand) {
		if(!world.isRemote){
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
