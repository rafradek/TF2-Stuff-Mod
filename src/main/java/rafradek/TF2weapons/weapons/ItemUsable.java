package rafradek.TF2weapons.weapons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.MapList;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2Attribute.AttributeState;
import rafradek.TF2weapons.TF2Attribute.AttributeType;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigCategory;

public abstract class ItemUsable extends ItemFromData
{
    //public ConfigCategory data;
    //public String render;
    public static int sps;
    //public static int tickleft;
    //public static boolean addedIcons;
    //public static ThreadLocalMap<EntityLivingBase, NBTTagCompound> itemProperties=new ThreadLocalMap<EntityLivingBase, NBTTagCompound>();
    public static HashMap<EntityLivingBase,float[]> lastDamage= new HashMap<EntityLivingBase,float[]>();
    
    public ItemUsable(){
    	super();
    	this.setCreativeTab(TF2weapons.tabweapontf2);
    }
    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab()
    {
        return TF2weapons.tabweapontf2;
    }
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        return new ActionResult<ItemStack>(this.canAltFire(worldIn, playerIn, itemStackIn)&&this.getAltFiringSpeed(itemStackIn, playerIn)!=Short.MAX_VALUE?EnumActionResult.SUCCESS:EnumActionResult.PASS, itemStackIn);
    }
    public abstract boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand, PredictionMessage message);
    
    public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
    	living.getCapability(TF2weapons.WEAPONS_CAP, null).pressedStart=true;
		return false;
	}
    
    public int getAmmoType(ItemStack stack){
    	return getData(stack).getInt(PropertyType.AMMO_TYPE);
    }
    
    public int getActualAmmoUse(ItemStack stack,EntityLivingBase living, int amount){
    	if(this.getAmmoType(stack)==0) return 0;
    	
    	stack.getTagCompound().setFloat("UsedAmmo", stack.getTagCompound().getFloat("UsedAmmo")+amount*TF2Attribute.getModifier("Ammo Eff", stack, 1, living));
		amount=0;
		while(stack.getTagCompound().getFloat("UsedAmmo")>=1){
			stack.getTagCompound().setFloat("UsedAmmo",stack.getTagCompound().getFloat("UsedAmmo")-1);
			amount++;
		}
		return amount;
    }
    
    public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		return false;
	}
    
    public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5)
   	{
    	super.onUpdate(stack, par2World, par3Entity, par4, par5);
    	/*if(itemProperties.get(par2World.isRemote).get(par3Entity)==null){
			itemProperties.get(par2World.isRemote).put((EntityLivingBase) par3Entity, new NBTTagCompound());
		}*/
    	WeaponsCapability cap=par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
    	
    	if(stack.getTagCompound().getByte("active")==0&&par5){
			stack.getTagCompound().setByte("active", (byte) 1);
			//itemProperties.get(par2World.isRemote).get(par3Entity).setShort("reloadd", (short) 800);
			
			cap.fire1Cool=750;
			cap.fire2Cool=750;
		}
		else if(stack.getTagCompound().getByte("active")>0 && stack!=((EntityLivingBase) par3Entity).getHeldItemOffhand() && !par5){
			stack.getTagCompound().setByte("active", (byte) 0);
			this.holster(cap,stack,(EntityLivingBase) par3Entity,par2World);
			
			if(stack.getTagCompound().getByte("active")==2&&(cap.state&3)>0)
				this.endUse(stack,(EntityLivingBase) par3Entity,par2World,cap.state,0);
			
		}
    	if(par3Entity.ticksExisted%5==0&&stack.getTagCompound().getByte("active")==2 && TF2Attribute.getModifier("Mark Death", stack, 0, (EntityLivingBase) par3Entity)>0){
    		((EntityLivingBase) par3Entity).addPotionEffect(new PotionEffect(TF2weapons.markDeath,(int) TF2Attribute.getModifier("Mark Death", stack, 0, (EntityLivingBase) par3Entity)*20));
    	}
   		/*if(par1ItemStack.hasTagCompound()&&par1ItemStack.getTagCompound().getShort("lastfire")>0){
   			par1ItemStack.getTagCompound().setShort("lastfire", (short) (par1ItemStack.getTagCompound().getShort("lastfire") - 50));
   		}*/
   		/*if(!par5&&!(par3Entity instanceof EntityPlayer&&((EntityPlayer)par3Entity).capabilities.isCreativeMode)){
   			par1ItemStack.getTagCompound().setShort("reload", (short) (800));
   		}*/
   	}

    public void draw(WeaponsCapability weaponsCapability,ItemStack stack, EntityLivingBase living, World world) {

	}
    public void holster(WeaponsCapability cap,ItemStack stack, EntityLivingBase living, World world) {
    	cap.chargeTicks=0;
    	cap.charging=false;

	}
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
    	item.getTagCompound().removeTag("active");
    	player.getCapability(TF2weapons.WEAPONS_CAP, null).state=0;
    	this.holster(player.getCapability(TF2weapons.WEAPONS_CAP, null),item,(EntityLivingBase) player,player.worldObj);
        return true;
    }
	
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack){
		return stack.getTagCompound().getByte("active")>0&& living.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks==0&&(living.getActiveItemStack()==null || this.getDoubleWieldBonus(stack, living)!=1);
	}
	public abstract boolean fireTick(ItemStack stack, EntityLivingBase living, World world);
	public abstract boolean altFireTick(ItemStack stack, EntityLivingBase living, World world);
	
	
	
	/*public void registerIcons(IIconRegister par1IconRegister)
    {
		this.itemIcon = par1IconRegister.registerIcon(this.getIconString());
		if(addedIcons) return;
		Iterator<String> iterator=MapList.nameToCC.keySet().iterator();
		addedIcons=true;
		while(iterator.hasNext()){
			String name=iterator.next();
			//System.out.println(MapList.nameToCC.get(name).get("Render").getString()+" "+name);
			MapList.nameToIcon.put(name, par1IconRegister.registerIcon(MapList.nameToCC.get(name).get("Render").getString()));
		}
        //this.itemIcon = par1IconRegister.registerIcon(getData(stack).get("Render").getString());
    }*/
	/*@SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
	public int getRenderPasses(int metadata)
    {
        return 1;
    }*/
	
	
	
	public int getFiringSpeed(ItemStack stack,EntityLivingBase living){
		return (int) ( TF2Attribute.getModifier("Fire Rate", stack, ItemFromData.getData(stack).getInt(PropertyType.FIRE_SPEED),living) *(living !=null&& this.isDoubleWielding(living)?this.getDoubleWieldBonus(stack, living):1f));
	}
	
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
    {
        return true;
    }
	public boolean isDoubleWielding(EntityLivingBase living){
		return ItemFromData.getData(living.getHeldItemMainhand()) != null && ItemFromData.getData(living.getHeldItemOffhand())==ItemFromData.getData(living.getHeldItemMainhand())&& this.getDoubleWieldBonus(living.getHeldItemMainhand(),living)!=1;
	}
	public float getDoubleWieldBonus(ItemStack stack, EntityLivingBase living){
		//System.out.println("Double wield type: "+ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED)+" "+ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED));
		return !ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED)? 1f: ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED);
	}
	public boolean canAltFire(World worldObj, EntityLivingBase player,
			ItemStack item) {
		// TODO Auto-generated method stub
		return item.getTagCompound().getByte("active")>0&&player.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks==0&&(player.getActiveItemStack()==null || this.getDoubleWieldBonus(item, player)!=1);
	}
	public void altUse(ItemStack stack, EntityLivingBase living,
			World world) {
		
	}
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return Short.MAX_VALUE;
	}
}
