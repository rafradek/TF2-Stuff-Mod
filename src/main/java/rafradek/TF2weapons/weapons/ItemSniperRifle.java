package rafradek.TF2weapons.weapons;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.characters.EntitySniper;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

public class ItemSniperRifle extends ItemBulletWeapon {
	public static UUID slowdownUUID=UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4D8C65A9");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "sniper slowdown", -0.73D, 2);
	public boolean canAltFire(World worldObj, EntityLivingBase player,
			ItemStack item) {
		return super.canAltFire(worldObj, player, item)&&player.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool <= 0;
	}
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand, PredictionMessage message){
		if(living instanceof EntityPlayer||stack.getTagCompound().getBoolean("WaitProper")){
			super.use(stack, living, world, hand, message);
			this.disableZoom(stack, living);
			stack.getTagCompound().setBoolean("WaitProper", false);
			return true;
		}
		else{
			stack.getTagCompound().setBoolean("WaitProper", true);
			this.altUse(stack, living, world);
			living.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool=2500;
		}
		return false;
	}
	public void altUse(ItemStack stack, EntityLivingBase living,
			World world) {
		WeaponsCapability cap=living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if(!cap.charging){
			cap.charging=true;
			if(world.isRemote&& living==Minecraft.getMinecraft().thePlayer){
				Minecraft.getMinecraft().gameSettings.mouseSensitivity*=0.4f;
			}
			if(living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(slowdownUUID)==null)
				living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(slowdown);
		}
		else{
			this.disableZoom(stack, living);
		}
		
	}
	public void disableZoom(ItemStack stack,EntityLivingBase living){
		WeaponsCapability cap=living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if(living.worldObj.isRemote&& living==Minecraft.getMinecraft().thePlayer&&cap.charging){
			Minecraft.getMinecraft().gameSettings.mouseSensitivity*=2.5f;
		}
		cap.chargeTicks=0;
		cap.charging=false;
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
	}
	public boolean canHeadshot(EntityLivingBase living,ItemStack stack) {
		// TODO Auto-generated method stub
		return living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks>4;
	}
	public boolean showTracer(ItemStack stack){
		return false;
	}
	public float getWeaponDamage(ItemStack stack,EntityLivingBase living, Entity target){
		return super.getWeaponDamage(stack, living, target)*(living!=null?this.getZoomBonus(stack,living):1);
	}
	
	public float getWeaponMaxDamage(ItemStack stack,EntityLivingBase living) {
		return super.getWeaponMaxDamage(stack, living);
	}
	
	public float getWeaponMinDamage(ItemStack stack,EntityLivingBase living){
		return super.getWeaponMinDamage(stack, living);
	}
	public float getZoomBonus(ItemStack stack,EntityLivingBase living){
		return 1+Math.max(0,(living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks-20)/((getChargeTime(stack,living)-20)/2));
	}
	public static float getChargeTime(ItemStack stack,EntityLivingBase living){
		return 66 / TF2Attribute.getModifier("Charge", stack, 1,living);
	}
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return 400;
	}
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		WeaponsCapability cap=par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
		
		if(cap.charging&&par5){
			if(cap.chargeTicks<getChargeTime(par1ItemStack, (EntityLivingBase) par3Entity)){
				cap.chargeTicks+=1;
				//System.out.println("Charging: "+cap.chargeTicks);
			}
		}
		
		if(par3Entity instanceof EntitySniper&&((EntitySniper) par3Entity).getAttackTarget()!=null&&par1ItemStack.getTagCompound().getBoolean("WaitProper")){
			if(((EntitySniper) par3Entity).getHealth()<8&&cap.fire1Cool>250){
				par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool=250;
			}
			/*else if(par1ItemStack.getTagCompound().getInteger("reload")<=100&&!((EntitySniper)par3Entity).attack.lookingAt(1)){
				par1ItemStack.getTagCompound().setInteger("reload", 100);
			}*/
			//par1ItemStack.getTagCompound().setBoolean("WaitProper", true);
		}
	}
	/*public double getDiff(EntityTF2Character mob){
		 if(mob.getAttackTarget()!=null){
			mob.attack.lookingAt(mob.getAttackTarget(),2)
			double mX=mob.getAttackTarget().posX-mob.getAttackTarget().lastTickPosX;
			double mY=mob.getAttackTarget().posY-mob.getAttackTarget().lastTickPosY;
			double mZ=mob.getAttackTarget().posZ-mob.getAttackTarget().lastTickPosZ;
			double totalMotion=Math.sqrt(mX*mX+mY*mY+mZ*mZ);
			System.out.println("Odskok: "+totalMotion);
			return totalMotion;
		}
		 return 0;
	}*/
	public void holster(WeaponsCapability cap,ItemStack stack, EntityLivingBase living, World world) {
		super.holster(cap, stack, living, world);
		this.disableZoom(stack,living);
	}
}
