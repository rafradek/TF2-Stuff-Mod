package rafradek.TF2weapons.weapons;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigCategory;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.message.TF2ActionHandler;

public class ItemMinigun extends ItemBulletWeapon {

	public static UUID slowdownUUID=UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4D8C94C8");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "minigun slowdown", -0.5D, 2);

	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		WeaponsCapability cap=par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
		if(par5&&par1ItemStack.getTagCompound() !=null){
			//System.out.println("EntityTicked" + cap.state+ par3Entity);
			if((cap.state==0||cap.state==4)&&cap.chargeTicks>0){
				//System.out.println("Draining" + cap.chargeTicks);
				cap.chargeTicks-=2;
				((EntityLivingBase)par3Entity).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
			}
		}
	}
	/*public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par2List, boolean par4)
    {
		super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
        if (par1ItemStack.hasTagCompound())
        {
            par2List.add("minigun: "+Integer.toString(par1ItemStack.getTagCompound().getShort("minigunticks")));
        }
    }*/
	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		if(world.isRemote&&oldState==0&&(!ClientProxy.fireSounds.containsKey(living)||ClientProxy.fireSounds.get(living).type!=3)){
			//ResourceLocation playSound=new ResourceLocation(getData(stack).get("Wind Up Sound").getString());
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack,PropertyType.WIND_UP_SOUND),false,3,stack);
		}
		return false;
	}
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world,int action, int newState) {
		if(world.isRemote&&newState==0&&(!ClientProxy.fireSounds.containsKey(living)||ClientProxy.fireSounds.get(living).type!=4)&&living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks>0){
			//ResourceLocation playSound=new ResourceLocation(getData(stack).get("Wind Down Sound").getString());
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack,PropertyType.WIND_DOWN_SOUND),false,4,stack);
		}
		return false;
	}
	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if(world.isRemote&&this.canFire(world, living, stack)){
			WeaponsCapability cap=living.getCapability(TF2weapons.WEAPONS_CAP, null);
			if(cap.critTime<=0&&(!ClientProxy.fireSounds.containsKey(living)||ClientProxy.fireSounds.get(living).type!=0)){
				//ResourceLocation playSound=new ResourceLocation(getData(stack).get("Minigun Sound").getString());
				
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack,PropertyType.FIRE_LOOP_SOUND),true,0,stack);
			}
			else if(cap.critTime>0&&(!ClientProxy.fireSounds.containsKey(living)||ClientProxy.fireSounds.get(living).type!=1)){
				ResourceLocation playSoundCrit=new ResourceLocation(ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND)+".crit");
				
				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit),true,1,stack);
			}
		}
		//System.out.println("nie");
		this.spinMinigun(stack, living, world);
		return false;
	}
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }
	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		if(world.isRemote&&this.canFire(world, living, stack)&&(!ClientProxy.fireSounds.containsKey(living)||ClientProxy.fireSounds.get(living).type>2)){
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack,PropertyType.SPIN_SOUND),true,2,stack);
		}
			/*System.out.println("start");
			ResourceLocation playSound=new ResourceLocation(getData(stack).get("Spin Sound").getString());
			MinigunLoopSound sound=new MinigunLoopSound(playSound, living, false, getData(stack),false);
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
			MapList.spinSounds.put(sound, living);
		}*/
		if((living.getCapability(TF2weapons.WEAPONS_CAP, null).state&1)!=1)
			this.spinMinigun(stack, living, world);
		return false;
	}
	public void spinMinigun(ItemStack stack, EntityLivingBase living, World world) {
		if(super.canFire(world, living, stack)){
			WeaponsCapability cap=living.getCapability(TF2weapons.WEAPONS_CAP, null);
			
			if(living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(slowdownUUID)==null){
				living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(slowdown);
			}
			if(living.isSprinting()){
				living.setSprinting(false);
			}
			
			if(cap.fire1Cool<= 0 && cap.chargeTicks<TF2Attribute.getModifier("Minigun Spinup", stack, 18,living)){
				cap.chargeTicks+=1;
			}
		}
	}
	@Override
    public boolean canFire(World world, EntityLivingBase living, ItemStack stack)
    {
        return super.canFire(world, living, stack)&&((living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks >= TF2Attribute.getModifier("Minigun Spinup", stack, 18,living)) || (living instanceof EntityPlayer && ((EntityPlayer)living).isCreative()));
    }
	public void holster(WeaponsCapability cap,ItemStack stack, EntityLivingBase living, World world) {
		super.holster(cap, stack, living, world);
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
	}
}
