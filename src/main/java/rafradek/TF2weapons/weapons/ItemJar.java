package rafradek.TF2weapons.weapons;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

public class ItemJar extends ItemProjectileWeapon {

	public ItemJar(){
		super();
		this.setMaxStackSize(64);
	}
	@SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab()
    {
        return TF2weapons.tabutilitytf2;
    }
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack){
		return !stack.getTagCompound().getBoolean("IsEmpty")&&super.canFire(world, living, stack);
	}
	public String getItemStackDisplayName(ItemStack stack)
    {
		String string=super.getItemStackDisplayName(stack);
		if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsEmpty")){
			string="Empty Jar - ".concat(string);
		}
		return string;
    }
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par2List, boolean par4)
    {
		if(par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().getBoolean("IsEmpty")){
			par2List.add("Right click to fill the container");
		}
		super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
    }
	@Override
    public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand, PredictionMessage message)
    {
		if(super.use(stack, living, world, hand, message)&&!world.isRemote){
			stack.stackSize--;
			if(stack.stackSize<=0&&living instanceof EntityPlayer){
				((EntityPlayer)living).inventory.deleteStack(stack);
				
			}
		}
		return true;
	}
	@SideOnly(Side.CLIENT)
    public boolean showDurabilityBar(ItemStack stack)
    {
    	Integer value=Minecraft.getMinecraft().thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.get(getData(stack).getName());
    	return stack.getTagCompound().getBoolean("IsEmpty")&&value!=null&&value>0;
    }
    @SideOnly(Side.CLIENT)
    public double getDurabilityForDisplay(ItemStack stack)
    {
    	Integer value=Minecraft.getMinecraft().thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.get(getData(stack).getName());
        return (double)(value!=null?value:0) / (double)1200;
    }
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
    {
        return 40;
    }
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }
	@Nullable
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
		if(!(entityLiving instanceof EntityPlayer&&((EntityPlayer)entityLiving).capabilities.isCreativeMode))
    		stack.stackSize--;

        if (entityLiving instanceof EntityPlayer)
        {
        	entityLiving.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.put(getData(stack).getName(), 1500);
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            ItemStack newStack=stack.copy();
            newStack.stackSize=1;
            newStack.getTagCompound().setBoolean("IsEmpty", false);
            if(!entityplayer.inventory.addItemStackToInventory(newStack)){
            	entityplayer.dropItem(newStack, true);
            }
        }

        return stack;
    }
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		Integer value=playerIn.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.get(getData(itemStackIn).getName());
    	if(itemStackIn.getTagCompound().getBoolean("IsEmpty")&&(value==null||value<=0)){
    		playerIn.setActiveHand(hand);
    		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    	}
    	return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStackIn);
	}
	public boolean doMuzzleFlash(ItemStack stack, EntityLivingBase attacker){
		return false;
	}
}
