package rafradek.TF2weapons.crafting;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemSoldierBackpack;

public class ItemTF2 extends Item {

	public static final String[] NAMES=new String[]{"ingotCopper","ingotLead","ingotAustralium","scrapMetal","reclaimedMetal","refinedMetal","nuggetAustralium","key","crate","randomWeapon","randomHat"};
	
	public ItemTF2(){
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName("tf2item");
	}
	public String getUnlocalizedName(ItemStack stack)
    {
		return "item."+NAMES[stack.getMetadata()];
    }
	public int getItemStackLimit(ItemStack stack){
		return (stack.getMetadata()==9||stack.getMetadata()==10)?1:64;
	}
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
		//System.out.println(this.getCreativeTab());
		for(int i=0;i<8;i++){
			par3List.add(new ItemStack(this,1,i));
		}
    }
	
}
