package rafradek.TF2weapons.weapons;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.projectiles.EntityFlame;
import rafradek.TF2weapons.projectiles.EntityFlameEffect;
import rafradek.TF2weapons.projectiles.EntityProjectileBase;
import rafradek.TF2weapons.projectiles.EntityStickybomb;
import rafradek.TF2weapons.projectiles.EntitySyringe;

public class ItemFlameThrower extends ItemProjectileWeapon {
	
	public boolean canAltFire(World worldObj, EntityLivingBase player,
			ItemStack item) {
		return super.canAltFire(worldObj, player, item)&&item.getTagCompound().getShort("reload") <= 0;
	}
	public boolean canFire(World world, EntityLivingBase living,
			ItemStack stack) {
		return super.canFire(world, living, stack);
	}
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return 750;
	}
	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		if(world.isRemote&&(newState&1)-(action&1)==1&&this.canFire(world, living, stack)){
			SoundEvent playSound=ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
			ClientProxy.playWeaponSound(living, playSound,false,2,stack);
		}
		return false;
	}
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		if((action&1)==1){
			if(world.isRemote){
				//System.out.println("called"+ClientProxy.fireSounds.get(living));
				if(ClientProxy.fireSounds.get(living)!=null){
					//System.out.println("called2"+ClientProxy.fireSounds.get(living).type);
					ClientProxy.fireSounds.get(living).setDone();
					//Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
				}
			}
			living.playSound(ItemFromData.getSound(stack, PropertyType.FIRE_STOP_SOUND), 1f, 1f);
		}
		return false;
	}
	
	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if(world.isRemote&&living.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool<=50&&this.canFire(world, living, stack)){
			if(living.getCapability(TF2weapons.WEAPONS_CAP, null).startedPress()){
				SoundEvent playSound=ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
				ClientProxy.playWeaponSound(living, playSound,false,2,stack);
			}
			if(living.isInsideOfMaterial(Material.WATER)){
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, living.posX, living.posY+living.getEyeHeight()-0.1, living.posZ, living.motionX, 0.2D+living.motionY, living.motionZ, new int[0]);
			}
			else{
				ClientProxy.spawnFlameParticle(world, living,0f);
				ClientProxy.spawnFlameParticle(world, living,0.5f);
			}
			//System.out.println("to: "+ClientProxy.fireSounds.containsKey(living));
			/*if(ClientProxy.fireSounds.containsKey(living)){
				System.out.println("to2: "+Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))+" "+ClientProxy.fireSounds.get(living).type);
			}*/
			if(living.getCapability(TF2weapons.WEAPONS_CAP, null).critTime<=0&&(!ClientProxy.fireSounds.containsKey(living)||!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))||(ClientProxy.fireSounds.get(living).type!=0&&ClientProxy.fireSounds.get(living).type!=2))){
				//new ResourceLocation(ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND));
				
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack,PropertyType.FIRE_LOOP_SOUND),true,0,stack);
			}
			else if(living.getCapability(TF2weapons.WEAPONS_CAP, null).critTime>0&&(!ClientProxy.fireSounds.containsKey(living)||!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))||(ClientProxy.fireSounds.get(living).type!=1))){
				ResourceLocation playSoundCrit=new ResourceLocation(ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND)+".crit");
				
				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit),true,1,stack);
			}
		}
		//System.out.println("nie");
		return false;
	}
	
	public static boolean isPushable(EntityLivingBase living, Entity target){
		return !(target instanceof EntitySyringe)&&!(target instanceof EntityBuilding)&&!(target instanceof EntityFlame) && !(target instanceof EntityArrow && target.onGround) && !(target instanceof IThrowableEntity&& ((IThrowableEntity)target).getThrower()==living) && !TF2weapons.isOnSameTeam(living, target);
	}
	public float getProjectileSpeed(ItemStack stack,EntityLivingBase living){
		return super.getProjectileSpeed(stack, living)*0.6f+TF2Attribute.getModifier("Flame Range", stack, super.getProjectileSpeed(stack, living)*0.4f, living);
	}
	public void altUse(ItemStack stack, EntityLivingBase living,
			World world) {
		living.getCapability(TF2weapons.WEAPONS_CAP, null).fire1Cool=750;
		if(world.isRemote){
			if(ClientProxy.fireSounds.get(living)!=null){
				ClientProxy.fireSounds.get(living).setDone();
				//Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
			}
			return;
		}
		//String airblastSound=getData(stack).get("Airblast Sound").getString();
		TF2weapons.playSound(living,ItemFromData.getSound(stack, PropertyType.AIRBLAST_SOUND), 1f, 1f);
    	
		Vec3d lookVec=living.getLookVec();
		Vec3d eyeVec=new Vec3d(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ);
		eyeVec.add(lookVec);
		float size=TF2Attribute.getModifier("Flame Range", stack, 5, living);
		List<Entity> list=world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(eyeVec.xCoord-size, eyeVec.yCoord-size, eyeVec.zCoord-size,
				eyeVec.xCoord+size, eyeVec.yCoord+size, eyeVec.zCoord+size));
		//System.out.println("aiming: "+lookVec+" "+eyeVec+" "+centerVec);
		for (Entity entity : list) {
			//System.out.println("dist: "+entity.getDistanceSq(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ));
			if(!isPushable(living,entity) || entity.getDistanceSq(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ)>size*size || !TF2weapons.lookingAt(living, 45, entity.posX, entity.posY+entity.height/2, entity.posZ)){
				continue;
			}
			if(entity instanceof IThrowableEntity && !(entity instanceof EntityStickybomb)){
				((IThrowableEntity)entity).setThrower(living);
			}
			else if(entity instanceof EntityArrow){
				((EntityArrow)entity).shootingEntity=living;
				((EntityArrow)entity).setDamage(((EntityArrow)entity).getDamage()*1.35);
			}
			if(entity instanceof IProjectile){
				IProjectile proj=(IProjectile)entity;
				float speed=(float) Math.sqrt(entity.motionX*entity.motionX+entity.motionY*entity.motionY+entity.motionZ*entity.motionZ)*(0.65f+TF2Attribute.getModifier("Flame Range", stack, 0.5f, living));
				List<RayTraceResult> rayTraces=TF2weapons.pierce(world, living, eyeVec.xCoord, eyeVec.yCoord, eyeVec.zCoord, eyeVec.xCoord+lookVec.xCoord*256, eyeVec.yCoord+lookVec.yCoord*256, eyeVec.zCoord+lookVec.zCoord*256, false, 0.08f,false);
				if(!rayTraces.isEmpty() && rayTraces.get(0).hitVec != null){
					//System.out.println("hit: "+mop.hitVec);
					proj.setThrowableHeading(rayTraces.get(0).hitVec.xCoord-entity.posX, rayTraces.get(0).hitVec.yCoord-entity.posY,rayTraces.get(0).hitVec.zCoord-entity.posZ, speed, 0);
				}
				else
				{
					proj.setThrowableHeading(eyeVec.xCoord+lookVec.xCoord*256-entity.posX, eyeVec.yCoord+lookVec.yCoord*256-entity.posY, eyeVec.zCoord+lookVec.zCoord*256-entity.posZ, speed, 0);
				}
			}
			else{
				double mult=(entity instanceof EntityLivingBase?1:0.2)+TF2Attribute.getModifier("Flame Range", stack, 0.8f, living);
				entity.motionX=lookVec.xCoord*0.6*mult;
				entity.motionY=(lookVec.yCoord*0.2+0.36)*mult;
				entity.motionZ=lookVec.zCoord*0.6*mult;
			}
			if(entity instanceof EntityProjectileBase){
				((EntityProjectileBase)entity).setCritical(Math.max(((EntityProjectileBase)entity).getCritical(),1));
			}
			if(!(entity instanceof EntityLivingBase)){
				//String throwObjectSound=getData(stack).get("Airblast Rocket Sound").getString();
		    	entity.playSound(ItemFromData.getSound(stack, PropertyType.AIRBLAST_ROCKET_SOUND), 1.5f, 1f);
		    	
			}
			EntityTracker tracker=((WorldServer)world).getEntityTracker();
			tracker.sendToAllTrackingEntity(entity, new SPacketEntityVelocity(entity));
			tracker.sendToAllTrackingEntity(entity, new SPacketEntityTeleport(entity));
		}
	}
}
