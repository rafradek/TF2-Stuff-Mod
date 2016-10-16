package rafradek.TF2weapons.weapons;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

import CoroUtil.util.CoroUtilPath;
import atomicstryker.dynamiclights.client.DynamicLights;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.IWeaponItem;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.characters.EntitySniper;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.common.config.ConfigCategory;

public abstract class ItemWeapon extends ItemUsable implements IWeaponItem
{
    /*public float damage;
    public float scatter;
    public int pellets;
    public float maxDamage;
    public int damageFalloff;
    public float minDamage;
    public int reload;
    public int clipSize;
    public boolean hasClip;
    public boolean clipType;
	public boolean randomCrits;
	public float criticalDamage;
	public int firstReload;
	public int knockback;*/
	public static boolean shouldSwing=false;
	public static int critical;
	protected static final UUID HEALTH_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785AAB6");
	protected static final UUID SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE97871BC2");
	protected static final UUID FOLLOW_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE978AD348");
    public ItemWeapon()
    {
    	super();
        
    }
    
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
    	super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
    	if(par5){
    		WeaponsCapability cap=par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
			if(TF2weapons.randomCrits && !par2World.isRemote&&cap.critTimeCool<=0){
				cap.critTimeCool=20;
				if(this.rapidFireCrits(par1ItemStack) && this.hasRandomCrits(par1ItemStack,par3Entity) && ((EntityLivingBase) par3Entity).getRNG().nextFloat()<=this.critChance(par1ItemStack, par3Entity)){
        			cap.critTime=40;
        			TF2weapons.network.sendToAllAround(new TF2Message.CapabilityMessage(par3Entity), TF2weapons.pointFromEntity(par3Entity));
        			//System.out.println("Apply crits rapid");
        		}
			}
			if(cap.critTime>0){
				cap.critTime-=1;
			}
			cap.critTimeCool-=1;
			/*if(par3Entity instanceof EntityTF2Character&&((EntityTF2Character) par3Entity).getAttackTarget()!=null){
				System.out.println(this.getWeaponSpreadBase(par1ItemStack, (EntityLivingBase) par3Entity));
				if(par1ItemStack.getTagCompound().getInteger("reload")<=100&&!((EntityTF2Character)par3Entity).attack.lookingAt(this.getWeaponSpreadBase(par1ItemStack, (EntityLivingBase) par3Entity)*100+1)){
					par1ItemStack.getTagCompound().setInteger("reload", 100);
				}
				//par1ItemStack.getTagCompound().setBoolean("WaitProper", true);
			}*/
    	}
	}
    
    @Override
    public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand, PredictionMessage message)
    {
    	//boolean mainHand=living instanceof EntityPlayer&&living.getEntityData().getCompoundTag("TF2").getBoolean("mainhand");
        if (stack.getItemDamage() != stack.getMaxDamage())
            if (this.hasClip(stack))
            {
                stack.damageItem(1, living);
                if(!world.isRemote && living instanceof EntityPlayerMP)
                	TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), false,hand), (EntityPlayerMP) living);
            }
        if(living instanceof EntityPlayer && hand==EnumHand.MAIN_HAND){
        	((EntityPlayer)living).resetCooldown();
        }
        else if(world.isRemote&&Minecraft.getMinecraft().thePlayer==living){
        	Minecraft.getMinecraft().getItemRenderer().resetEquippedProgress(EnumHand.OFF_HAND);
        }
        
        int thisCritical=TF2weapons.calculateCritPre(stack, living);
        
        critical=thisCritical;
        
        if(/*living instanceof EntityTF2Ch\aracter&&*/this.getAmmoType(stack)!=0/*&&((EntityTF2Character)living).getAmmo()>=0*/){
        	//
        	
        	if(living instanceof EntityTF2Character&&((EntityTF2Character)living).getAmmo()>=0){
        		((EntityTF2Character)living).useAmmo(1);
        	}
        	if(living instanceof EntityPlayer&&!((EntityPlayer)living).capabilities.isCreativeMode&&!this.hasClip(stack)){
        		ItemStack stackAmmo=ItemAmmo.searchForAmmo(living, stack);
        		if(stackAmmo!=null){
        			((ItemAmmo)stackAmmo.getItem()).consumeAmmo(living, stackAmmo, ((ItemWeapon)stack.getItem()).getActualAmmoUse(stack, living, 1));
        		}
        	}
    	}
        
        if(ItemFromData.getData(stack).hasProperty(PropertyType.FIRE_SOUND)){
        	SoundEvent soundToPlay=SoundEvent.REGISTRY.getObject(new ResourceLocation(ItemFromData.getData(stack).getString(PropertyType.FIRE_SOUND)+(thisCritical==2?".crit":"")));
        	living.playSound(soundToPlay, 2f, 1f);
        	if(world.isRemote){
        		ClientProxy.removeReloadSound(living);
        	}
        }
        if(world.isRemote){
        	this.doMuzzleFlash(stack, living);
        }
        	
        if(!world.isRemote && world.getDifficulty()==EnumDifficulty.HARD && living instanceof EntityPlayer && living.getCapability(TF2weapons.WEAPONS_CAP, null).zombieHuntTicks<=0&&(!(this instanceof ItemMeleeWeapon) || getData(stack).getName().equals("fryingpan"))){
        	living.getCapability(TF2weapons.WEAPONS_CAP, null).zombieHuntTicks=15;
        	for(EntityCreature mob:world.getEntitiesWithinAABB(EntityCreature.class, living.getEntityBoundingBox().expand(60, 60, 60),new Predicate<EntityCreature>(){

				@Override
				public boolean apply(EntityCreature input) {
					// TODO Auto-generated method stub
					return input.getAttackTarget() == null && (input instanceof IMob);
				}
        		
        	})){
        		mob.getLookHelper().setLookPositionWithEntity(mob, 60, 30);
        		if(!TF2weapons.isOnSameTeam(living, mob)){
        			if(mob.getEntitySenses().canSee(living))
        				mob.setAttackTarget(living);
        			mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier(FOLLOW_MODIFIER,"Follow Check", 3, 2));
        			//CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)mob, living, 1.1D);
        			mob.getNavigator().tryMoveToEntityLiving(living, 1.1f);
        			mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(FOLLOW_MODIFIER);
        		}
        		
        	}
        }
        for (int x = 0; x < this.getWeaponPelletCount(stack,living); x++)
        {
        	//System.out.println("shoot");
        	/*EntityBullet bullet;
        	if(target==null){
        		bullet = new EntityBullet(world, living, this.scatter);
        	}
        	else{
        		bullet = new EntityBullet(world, living, target, this.scatter);
        	}
            bullet.stack = stack;
            bullet.setDamage(this.damage*damagemult);
            bullet.damageM = this.maxDamage;
            bullet.damageMM = this.damageFalloff;
            bullet.minDamage = this.minDamage;
            if(thisCritical)
            	bullet.critical = true;
            	bullet.setDamage(this.damage*3);
            }
            world.spawnEntityInWorld(bullet);*/
        	this.shoot(stack,living,world,thisCritical, hand);
        }
        return true;
    }
    @Optional.Method(modid="DynamicLights")
    public void doMuzzleFlashLight(ItemStack stack, EntityLivingBase living) {
    	MuzzleFlashLightSource light=new MuzzleFlashLightSource(living);
    	TF2EventBusListener.muzzleFlashes.add(light);
    	DynamicLights.addLightSource(light);
	}
	public abstract void shoot(ItemStack stack, EntityLivingBase living, World world, int thisCritical, EnumHand hand);
    @Override
    public boolean canFire(World world, EntityLivingBase living, ItemStack stack)
    {
    	/*boolean flag=true;
    	if(living instanceof EntityTF2Character&&((EntityTF2Character)living).getAmmo()<=0){
    		flag=false;
    	}*/
        return super.canFire(world, living, stack)&&(((this.hasClip(stack)||ItemAmmo.searchForAmmo(living, stack)!=null)&&(!this.hasClip(stack)||stack.getItemDamage()<stack.getMaxDamage()))
        				|| (living instanceof EntityPlayer && ((EntityPlayer)living).capabilities.isCreativeMode));
    }
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par2List, boolean par4)
    {
    	super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
        /*if (par1ItemStack.hasTagCompound())
        {
            par2List.add("Firing: "+Integer.toString(par1ItemStack.getTagCompound().getShort("reload")));
            par2List.add("Reload: "+Integer.toString(par1ItemStack.getTagCompound().getShort("reloadd")));
            par2List.add("Crit: "+Integer.toString(par1ItemStack.getTagCompound().getShort("crittime")));
        }*/

    	if(this.hasClip(par1ItemStack)){
    		par2List.add("Clip: "+(this.getWeaponClipSize(par1ItemStack, par2EntityPlayer)-par1ItemStack.getItemDamage())+"/"+this.getWeaponClipSize(par1ItemStack, par2EntityPlayer));
    	}
    }
    
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
    {
    	Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
 
        if (slot == EntityEquipmentSlot.MAINHAND&&getData(stack)!=null)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.getWeaponDamage(stack, null, null)*this.getWeaponPelletCount(stack, null)-1, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -4+(1000D/this.getFiringSpeed(stack, null)), 0));
            float addHealth=TF2Attribute.getModifier("Health", stack, 0, null);
            if(addHealth!=0){
            	multimap.put(SharedMonsterAttributes.MAX_HEALTH.getAttributeUnlocalizedName(), new AttributeModifier(HEALTH_MODIFIER, "Weapon modifier", addHealth, 0));
            }
            float addSpeed=TF2Attribute.getModifier("Speed", stack, 1, null);
            if(addSpeed!=1){
            	multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(SPEED_MODIFIER, "Weapon modifier", addSpeed-1, 2));
            }
        }
        return multimap;
    }
    public float critChance(ItemStack stack, Entity entity){
    	float chance=0.025f;
    	if(ItemUsable.lastDamage.containsKey(entity)){
			for(int i=0;i<20;i++){
				chance+=ItemUsable.lastDamage.get(entity)[i]/800;
			}
    	}
    	return Math.min(chance,0.125f);
    }
	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		// TODO Auto-generated method stub
		return false;
	}
	@SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }
	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int getMaxDamage(ItemStack stack)
    {
		return stack.hasTagCompound()?this.getWeaponClipSize(stack,null):0;
    }
	
	public float getWeaponDamage(ItemStack stack,EntityLivingBase living, Entity target){
		float damage=TF2Attribute.getModifier("Damage", stack, ItemFromData.getData(stack).getFloat(PropertyType.DAMAGE),living);
		if(living !=null&&( this.isDoubleWielding(living)||living.isHandActive())){
			damage*=0.85f;
		}
		if(target !=null && !target.isBurning()){
			damage=TF2Attribute.getModifier("Damage Non Burn", stack, damage,living);
		}
		return damage;
	}
	
	public float getWeaponMaxDamage(ItemStack stack,EntityLivingBase living) {
		return ItemFromData.getData(stack).getFloat(PropertyType.MAX_DAMAGE);
	}
	
	public float getWeaponMinDamage(ItemStack stack,EntityLivingBase living){
		return ItemFromData.getData(stack).getFloat(PropertyType.MIN_DAMAGE);
	}
	
	public float getWeaponSpread(ItemStack stack,EntityLivingBase living){
		float base=this.getWeaponSpreadBase(stack, living);
		if(living instanceof EntityTF2Character&&((EntityTF2Character)living).getAttackTarget()!=null){
			float totalRotation=0;
			for(int i=0;i<20;i++){
				totalRotation+=((EntityTF2Character)living).lastRotation[i];
			}
			/*double speed=Math.sqrt((target.posX-shooter.targetPrevPos[1])*(target.posX-shooter.targetPrevPos[1])+(target.posY-shooter.targetPrevPos[3])
					*(target.posY-shooter.targetPrevPos[3])+(target.posZ-shooter.targetPrevPos[5])*(target.posZ-shooter.targetPrevPos[5]));*/
			base+=/*(speed+0.045)*/((EntityTF2Character)living).getMotionSensitivity()*totalRotation*0.01f;
			//System.out.println(target.motionX+" "+target.motionY+" "+target.motionZ+" "+(speed+0.045)*((EntityTF2Character)living).getMotionSensitivity());
			/*shooter.targetPrevPosX=target.posX;
			shooter.targetPrevPosY=target.posY;
			shooter.targetPrevPosZ=target.posZ;*/
		}
		return Math.abs(base);
	}
	public float getWeaponSpreadBase(ItemStack stack,EntityLivingBase living){
		return (float) (living!=null&&ItemFromData.getData(stack).getBoolean(PropertyType.SPREAD_RECOVERY)&&living.getCapability(TF2weapons.WEAPONS_CAP, null).lastFire<=0?0:TF2Attribute.getModifier("Spread", stack, (float) ItemFromData.getData(stack).getFloat(PropertyType.SPREAD),living)/TF2Attribute.getModifier("Accuracy",stack,1,living)*(living !=null&& (this.isDoubleWielding(living)||living.isHandActive())?1.5f:1f));
	}
	public int getWeaponPelletCount(ItemStack stack,EntityLivingBase living){
		return (int) (TF2Attribute.getModifier("Pellet Count", stack, ItemFromData.getData(stack).getInt(PropertyType.PELLETS),living));
	}
	
	public float getWeaponDamageFalloff(ItemStack stack){
		return ItemFromData.getData(stack).getFloat(PropertyType.DAMAGE_FALOFF);
	}
	
	public int getWeaponReloadTime(ItemStack stack,EntityLivingBase living){
		return (int) (TF2Attribute.getModifier("Reload Time", stack, ItemFromData.getData(stack).getInt(PropertyType.RELOAD_TIME),living));
	}
	
	public int getWeaponFirstReloadTime(ItemStack stack,EntityLivingBase living){
		return (int) (TF2Attribute.getModifier("Reload Time", stack, ItemFromData.getData(stack).getInt(PropertyType.RELOAD_TIME_FIRST),living)*(living !=null&& this.isDoubleWielding(living)?2f:1f));
	}
	
	public boolean hasClip(ItemStack stack){
		//System.out.println("Clip:"+stack.getTagCompound());
		return ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_CLIP);
	}
	
	public int getWeaponClipSize(ItemStack stack,EntityLivingBase living){
		//System.out.println("With tag: "+stack.getTagCompound());
		return (int) (TF2Attribute.getModifier("Clip Size", stack, ItemFromData.getData(stack).getInt(PropertyType.CLIP_SIZE),living));
	}
	
	public boolean IsReloadingFullClip(ItemStack stack){
		return ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_FULL_CLIP);
	}
	
	public boolean hasRandomCrits(ItemStack stack,Entity par3Entity){
		return par3Entity instanceof EntityPlayer&&ItemFromData.getData(stack).getBoolean(PropertyType.RANDOM_CRITS)&&TF2Attribute.getModifier("Random Crit", stack, 0, null)==0;
	}

	public double getWeaponKnockback(ItemStack stack,EntityLivingBase living){
		return  TF2Attribute.getModifier("Knockback", stack, ItemFromData.getData(stack).getInt(PropertyType.KNOCKBACK),living);
	}
	public boolean rapidFireCrits(ItemStack stack){
		return ItemFromData.getData(stack).getBoolean(PropertyType.RAPIDFIRE_CRITS);
	}
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        return true;
    }
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
    {
        return !shouldSwing;
    }
	public void draw(WeaponsCapability cap,ItemStack stack, EntityLivingBase living, World world) {
		cap.reloadCool=0;
		cap.critTime=0;
		cap.state=cap.state&7;
		super.draw(cap,stack, living, world);
	}
	public boolean onHit(ItemStack stack,EntityLivingBase attacker, Entity target){
		return true;
	}
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source){
		if(TF2Attribute.getModifier("Burn Hit", stack, 0, attacker)>0){
			target.setFire((int) TF2Attribute.getModifier("Burn Time", stack, TF2Attribute.getModifier("Burn Hit", stack, 0, attacker),attacker)+1);
		}
		if(TF2Attribute.getModifier("Uber Hit", stack, 0, attacker)>0){
			if(attacker instanceof EntityPlayer){
				for(ItemStack medigun:((EntityPlayer) attacker).inventory.mainInventory){
					if(medigun!=null&&medigun.getItem() instanceof ItemMedigun){
						medigun.getTagCompound().setFloat("ubercharge",MathHelper.clamp_float(medigun.getTagCompound().getFloat("ubercharge")+TF2Attribute.getModifier("Uber Hit", stack, 0, attacker)/100, 0, 1));
						if(stack.getTagCompound().getFloat("ubercharge")>=1){
			    			attacker.playSound(ItemFromData.getSound(stack,PropertyType.CHARGED_SOUND), 1.2f, 1);
			    		}
						break;
					}
				}
			}
		}
	}
	public boolean doMuzzleFlash(ItemStack stack, EntityLivingBase attacker){
		
		ClientProxy.spawnFlashParticle(attacker.worldObj,attacker);
		if(TF2weapons.dynamicLights){
			this.doMuzzleFlashLight(stack, attacker);
		}
		return true;
	}
}
