package rafradek.TF2weapons.characters;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.characters.ai.EntityAIFollowTrader;
import rafradek.TF2weapons.characters.ai.EntityAINearestChecked;
import rafradek.TF2weapons.characters.ai.EntityAISeek;
import rafradek.TF2weapons.characters.ai.EntityAIUseRangedWeapon;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.weapons.ItemWeapon;
import rafradek.TF2weapons.weapons.ItemUsable;

public class EntityTF2Character extends EntityCreature implements IMob , IMerchant {
	
	public float[] lastRotation;
	public boolean jump;
	public boolean friendly;
	public boolean ranged;
	public EntityAIUseRangedWeapon attack;
	public EntityAINearestChecked findplayer =new EntityAINearestChecked(this, EntityLivingBase.class, true,false,this.getEntitySelector(), true);
	protected EntityAIAttackMelee attackMeele = new EntityAIAttackMelee(this, 1.1F, false);
	public EntityAIWander wander;
	public int ammoLeft;
	public boolean unlimitedAmmo;
	public boolean natural;
	private boolean noAmmo;
	public boolean alert;
	public static int nextEntTeam=-1;
	public EntityPlayer trader;
	public EntityPlayer lastTrader;
	public Map<EntityPlayer, Integer> tradeCount;
	
	public ItemStack[] loadout;
	//public int heldWeaponSlot;
	
	public int followTicks;
	public MerchantRecipeList tradeOffers;
	public double[] targetPrevPos=new double[9];
	private static final DataParameter<Byte> VIS_TEAM = EntityDataManager.createKey(EntityTF2Character.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> DIFFICULTY = EntityDataManager.createKey(EntityTF2Character.class, DataSerializers.BYTE);
	public float rotation;
	public int traderFollowTicks;
	
	public EntityTF2Character(World p_i1738_1_) {
		super(p_i1738_1_);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(6, new EntityAIFollowTrader(this));
		this.tasks.addTask(6, wander=new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityTF2Character.class, 8.0F));
		this.tasks.addTask(7, new EntityAISeek(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, findplayer);
		//this.lookHelper=new 
		//this.motionSensitivity=4;
		this.rotation=17;
		this.lastRotation=new float[20];
		this.loadout=new ItemStack[5];
		this.inventoryHandsDropChances[0]=0;
		if(p_i1738_1_ != null){
		//this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setTag("Attributes", (NBTTagCompound) ((ItemUsable)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).buildInAttributes.copy());
			
			this.attack =new EntityAIUseRangedWeapon(this, 1.0F, 20.0F);
			this.setCombatTask(true);
		}
		this.tradeCount=new HashMap<>();
		
		/*for (int i = 0; i < this.e.length; ++i)
        {
            this.equipmentDropChances[i] = 0.0F;
        }*/
		// TODO Auto-generated constructor stub
	}
	/*public EntityLookHelper getLookHelper()
    {
        return this.lookHelper;
    }*/
	public ItemStack getPickedResult(RayTraceResult target)
    {
		return new ItemStack(TF2weapons.itemPlacer,1,1);
    }
	protected void addWeapons()
    {
		String className=this.getClass().getSimpleName().substring(6).toLowerCase();
		//System.out.println("Class name: "+className);
		this.loadout[0]=ItemFromData.getRandomWeaponOfSlotMob(className, 0, this.rand, false);
		this.loadout[1]=ItemFromData.getRandomWeaponOfSlotMob(className, 1, this.rand, false);
		this.loadout[2]=ItemFromData.getRandomWeaponOfSlotMob(className, 2, this.rand, false);
		if(this.worldObj.getWorldTime()>720000&&this.rand.nextInt(8)==0){
			TF2Attribute.upgradeItemStack(this.loadout[0], (int) (this.worldObj.getWorldTime()/6000), rand);
		}
    }
    /*public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn){
    	if(slotIn==EntityEquipmentSlot.MAINHAND){
    		//System.out.println("Held item: "+this.loadout[this.heldWeaponSlot]);
    		return this.loadout[this.heldWeaponSlot];
    	}
    	else{
    		return super.getItemStackFromSlot(slotIn);
    	}
    }*/
    /*public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nullable ItemStack stack)
    {
    	if(slotIn==EntityEquipmentSlot.MAINHAND){
    		this.loadout[this.heldWeaponSlot]=stack;
    	}
    	else{
    		super.setItemStackToSlot(slotIn, stack);
    	}
    }*/
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(VIS_TEAM, (byte)this.rand.nextInt(2));
		this.dataManager.register(DIFFICULTY, (byte)0);
	}
	public int getEntTeam(){
		return (int) this.dataManager.get(VIS_TEAM);
	}
	public int getDiff(){
		return (int) this.dataManager.get(DIFFICULTY);
	}
	public void setEntTeam(int team){
		this.dataManager.set(VIS_TEAM, (byte)team);
	}
	public void setDiff(int diff){
		this.dataManager.set(DIFFICULTY, (byte)diff);
	}
	public int getAmmo() {
		// TODO Auto-generated method stub
		return ammoLeft;
	}
	public void setAttackTarget(EntityLivingBase target){
		
		super.setAttackTarget(target);
		if(this.isTrading()){
			this.setCustomer(null);
		}
		if(!this.alert){
			for(EntityTF2Character ent:this.worldObj.getEntitiesWithinAABB(EntityTF2Character.class, new AxisAlignedBB(this.posX-15, this.posY-6, this.posZ-15, this.posX+15, this.posY+6, this.posZ+15))){
				if(TF2weapons.isOnSameTeam(this,ent)&&!TF2weapons.isOnSameTeam(this, target)&&(ent.getAttackTarget()==null||ent.getAttackTarget().isDead)){
					ent.alert=true;
					ent.setAttackTarget(target);
					ent.alert=false;
				}
			}
		}
	}
	public void useAmmo(int amount) {
		if(!this.unlimitedAmmo)
			this.ammoLeft -= amount;
		
	}
	public float getAttributeModifier(String attribute) {
		if(this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityPlayer){
			if(attribute.equals("Knockback"))
				return this.getDiff()==1 ? 0.4f : (this.getDiff()==3 ? 0.75f : 0.55f);
			else if(attribute.equals("Fire Rate"))
				return this.getDiff()==1 ? 1.9f : (this.getDiff()==3 ? 1.2f : 1.55f);
			else if(attribute.equals("Spread")){
				float base=this.getDiff()==1 ? 1.9f : (this.getDiff()==3 ? 1.2f : 1.55f);
				return base;
			}
		}
		return 1f;
	}
	@Override
	public void onLivingUpdate()
    {
		
		super.onLivingUpdate();
		this.updateArmSwingProgress();
		if(this.getAttackTarget()!=null&&!this.getAttackTarget().isEntityAlive()){
			this.setAttackTarget(null);
		}
		if(!this.friendly&&this.getAttackTarget() instanceof EntityTF2Character&&TF2weapons.isOnSameTeam(this, this.getAttackTarget())){
			this.setAttackTarget(null);
		}
		if(this.jump&&this.onGround){
			this.jump();
		}
		if((this.getCapability(TF2weapons.WEAPONS_CAP, null).state&4)==0){
			this.getCapability(TF2weapons.WEAPONS_CAP, null).state+=4;
    	}
		if(!this.noAmmo&&this.getAttackTarget()!=null&&Math.abs(this.rotationYaw-this.rotationYawHead)>60/*TF2ActionHandler.playerAction.get().get(this)!=null&&(TF2ActionHandler.playerAction.get().get(this)&3)>0*/){
			if(this.rotationYawHead-this.rotationYaw>60){
				this.rotationYaw=this.rotationYawHead+60;
			}
			else{
				this.rotationYaw=this.rotationYawHead-60;
			}
		}
		if(!this.worldObj.isRemote){
			this.setDiff(this.worldObj.getDifficulty().getDifficultyId());
			if(this.isTrading()&&(this.trader.getDistanceSqToEntity(trader)>100||!this.isEntityAlive())){
				this.setCustomer(null);
			}
			if(this.ammoLeft<=0&&!this.noAmmo){
				this.setCombatTask(false);
				this.noAmmo=true;
			}
		}
		if(this.getHeldItem(EnumHand.MAIN_HAND)!=null&&this.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable){
			this.getHeldItem(EnumHand.MAIN_HAND).getItem().onUpdate(getHeldItem(EnumHand.MAIN_HAND), worldObj, this, 0, true);
		}
		
		for(int i=19;i>0;i--){
			this.lastRotation[i]=this.lastRotation[i-1];
		}
		this.lastRotation[0]=(float) Math.sqrt((this.rotationYawHead-this.prevRotationYawHead)*(this.rotationYawHead-this.prevRotationYawHead)+
				(this.rotationPitch-this.prevRotationPitch)*(this.rotationPitch-this.prevRotationPitch));
    }
	public IEntityLivingData onInitialSpawn(DifficultyInstance p_180482_1_,IEntityLivingData p_110161_1_)
    {
		super.onInitialSpawn(p_180482_1_,p_110161_1_);
		if(p_110161_1_==null){
			p_110161_1_=new TF2CharacterAdditionalData();
			((TF2CharacterAdditionalData)p_110161_1_).natural=true;
			if(nextEntTeam>=0){
				((TF2CharacterAdditionalData)p_110161_1_).team=nextEntTeam;
				nextEntTeam=-1;
			}
			List<EntityTF2Character> list = this.worldObj.getEntitiesWithinAABB(EntityTF2Character.class, this.getEntityBoundingBox().expand(40, 4.0D, 40), null);
			if(list.isEmpty()){
				((TF2CharacterAdditionalData)p_110161_1_).team=this.rand.nextInt(2);
			}
			else{
				((TF2CharacterAdditionalData)p_110161_1_).team=list.get(0).getEntTeam();
			}
		}
		if(p_110161_1_ instanceof TF2CharacterAdditionalData){
			this.natural=(((TF2CharacterAdditionalData)p_110161_1_).natural);
			this.setEntTeam(((TF2CharacterAdditionalData)p_110161_1_).team);
		}
		if(this.natural){
			
		}
		this.addWeapons();
		this.switchSlot(this.getDefaultSlot());
		return p_110161_1_;
    }
	public int getDefaultSlot() {
		// TODO Auto-generated method stub
		return 0;
	}
	public void setCombatTask(boolean ranged)
    {
		this.ranged=ranged; 
        this.tasks.removeTask(this.attack);
        this.tasks.removeTask(this.attackMeele);
        this.getCapability(TF2weapons.WEAPONS_CAP, null).state=0;
        //System.out.println(TF2ActionHandler.playerAction.get(this.worldObj.isRemote).size());

        if (ranged)
        {
            this.tasks.addTask(4, this.attack);
        }
        else
        {
            this.tasks.addTask(4, this.attackMeele);
        }
    }
	public Predicate<EntityLivingBase> getEntitySelector(){
		return new Predicate<EntityLivingBase>(){
			public boolean apply(EntityLivingBase p_82704_1_)
	        {
				return ((p_82704_1_.getTeam()!=null)&&!TF2weapons.isOnSameTeam(EntityTF2Character.this, p_82704_1_))&&(!(p_82704_1_ instanceof EntityTF2Character&&TF2weapons.naturalCheck.equals("Never")) ||(!((EntityTF2Character)p_82704_1_).natural||!natural));
				
	        }
		};
	}
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);

        this.ammoLeft=par1NBTTagCompound.getShort("Ammo");
        this.unlimitedAmmo=par1NBTTagCompound.getBoolean("UnlimitedAmmo");
        this.setEntTeam(par1NBTTagCompound.getByte("Team"));
        this.natural=par1NBTTagCompound.getBoolean("Natural");
        
        NBTTagList list=(NBTTagList) par1NBTTagCompound.getTag("Loadout");
		
        for (int i = 0; i < list.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");
            this.loadout[j]= ItemStack.loadItemStackFromNBT(nbttagcompound);
        }
        if(par1NBTTagCompound.hasKey("Offers")){
        	this.tradeOffers=new MerchantRecipeList();
        	this.tradeOffers.readRecipiesFromTags(par1NBTTagCompound.getCompoundTag("Offers"));
        }
        	
    }
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack)
    {
        if (!(player.getHeldItemMainhand() !=null && player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacerPlus)&& ( this.getAttackTarget() == null || this.friendly) &&this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking())
        {
        	if (this.worldObj.isRemote && player.getTeam() == null && ((this.getCapability(TF2weapons.WEAPONS_CAP, null).state&1)==0 || this.friendly) && !player.isCreative()){
        		ClientProxy.displayScreenConfirm("Choose a team to interact", "Visit the Mann Co. Store located in a village");
        	}
            if (!this.worldObj.isRemote && (TF2weapons.isOnSameTeam(this, player)||player.isCreative()) &&(this.tradeOffers == null || !this.tradeOffers.isEmpty()))
            {
                this.setCustomer(player);
                player.displayVillagerTradeGui(this);
            }

            player.addStat(StatList.TALKED_TO_VILLAGER);
            return true;
        }
        else
        {
            return super.processInteract(player, hand, stack);
        }
    }
	public boolean isTrading() {
		// TODO Auto-generated method stub
		return this.trader!=null;
	}
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setShort("Ammo", (short) this.ammoLeft);
        par1NBTTagCompound.setBoolean("UnlimitedAmmo", this.unlimitedAmmo);
        par1NBTTagCompound.setByte("Team", (byte) this.getEntTeam());
        par1NBTTagCompound.setBoolean("Natural", this.natural);
        NBTTagList list=new NBTTagList();
       
        for(int i=0;i<this.loadout.length;i++){
			ItemStack itemstack = this.loadout[i];

            if (itemstack != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                itemstack.writeToNBT(nbttagcompound);
                list.appendTag(nbttagcompound);
            }
		}
        par1NBTTagCompound.setTag("Loadout", list);
        if(this.tradeOffers!=null)
        	par1NBTTagCompound.setTag("Offers",this.tradeOffers.getRecipiesAsTags());
    }
    public boolean getCanSpawnHere()
    {
    	if(TF2EventBusListener.isSpawnEvent(worldObj))
    		return this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL&&super.getCanSpawnHere();
    	
    	boolean validLight=this.isValidLightLevel();
    	Chunk chunk = this.worldObj.getChunkFromBlockCoords(new BlockPos(MathHelper.floor_double(this.posX),0, MathHelper.floor_double(this.posZ)));
    	boolean spawnDay=this.rand.nextInt(32)==0&&chunk.getRandomWithSeed(987234911L).nextInt(10)==0;

    	if(!spawnDay&&!validLight) return false;
    	int time=(int) Math.min((this.worldObj.getWorldInfo().getWorldTime()/24000),4);

        return (time == 4 || this.rand.nextInt(4) < time) && this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL&&super.getCanSpawnHere();
    }
    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (!this.worldObj.isRemote && this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL)
        {
            this.setDead();
        }
    }
    public Team getTeam(){
    	return this.getEntTeam()==0?this.worldObj.getScoreboard().getTeam("RED"):this.worldObj.getScoreboard().getTeam("BLU");
    }
    protected SoundEvent getSwimSound()
    {
        return SoundEvents.ENTITY_HOSTILE_SWIM;
    }

    protected SoundEvent getSplashSound()
    {
        return SoundEvents.ENTITY_HOSTILE_SPLASH;
    }
    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
    }
    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (super.attackEntityFrom(source, amount))
        {
            Entity entity = source.getEntity();
            return this.getRidingEntity() != entity && this.getRidingEntity() != entity ? true : true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected SoundEvent getHurtSound()
    {
        return SoundEvents.ENTITY_HOSTILE_HURT;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_HOSTILE_DEATH;
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
    	float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
        int i = 0;

        if (entityIn instanceof EntityLivingBase)
        {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag)
        {
            if (i > 0 && entityIn instanceof EntityLivingBase)
            {
                ((EntityLivingBase)entityIn).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0)
            {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                ItemStack itemstack = this.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

                if (itemstack != null && itemstack1 != null && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD)
                {
                    float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

                    if (this.rand.nextFloat() < f1)
                    {
                        entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                        this.worldObj.setEntityState(entityplayer, (byte)30);
                    }
                }
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    protected boolean isValidLightLevel()
    {
    	BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

        if (this.worldObj.getLightFor(EnumSkyBlock.SKY, blockpos) > this.rand.nextInt(32))
        {
            return false;
        }
        else
        {
            int i = this.worldObj.getLightFromNeighbors(blockpos);

            if (this.worldObj.isThundering())
            {
                int j = this.worldObj.getSkylightSubtracted();
                this.worldObj.setSkylightSubtracted(10);
                i = this.worldObj.getLightFromNeighbors(blockpos);
                this.worldObj.setSkylightSubtracted(j);
            }

            return i <= 4+this.rand.nextInt(4);
        }
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    protected boolean canDropLoot()
    {
        return true;
    }
    public int getTalkInterval()
    {
        return 220;
    }
    protected float getSoundPitch()
    {
        return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.08F + 1.5F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.08F + 1.0F;
    }
    public float getMotionSensitivity(){
    	return this.getDiff()==1 ? 0.18f : (this.getDiff()==3 ? 0.07f : 0.11f);
    }
	public void onShot() {

	}
	protected boolean canDespawn()
    {
        return this.natural && !TF2EventBusListener.isSpawnEvent(worldObj);
    }
	/*@Nullable
    protected ResourceLocation getLootTable()
    {
        return TF2weapons.lootTF2Character;
    }*/
	@Override
	public void setCustomer(EntityPlayer player) {
		this.trader=player;
	}
	@Override
	public EntityPlayer getCustomer() {
		// TODO Auto-generated method stub
		return this.trader;
	}
	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		// TODO Auto-generated method stub
		if(this.tradeOffers==null){
			makeOffers();
		}
		return tradeOffers;
	}
	public void makeOffers(){
		this.tradeOffers=new MerchantRecipeList();
		int weaponCount=1+this.rand.nextInt(2);
		for(int i=0;i<weaponCount;i++){
			
			boolean buyItem=this.rand.nextBoolean();
			int slot=getValidSlots()[this.rand.nextInt(getValidSlots().length)];
			String className=this.getClass().getSimpleName().substring(6).toLowerCase();
			ItemStack item=ItemFromData.getRandomWeaponOfSlotMob(className,slot,this.getRNG(), false);
			ItemStack metal=new ItemStack(TF2weapons.itemTF2,Math.max(1,ItemFromData.getData(item).getInt(PropertyType.COST)/9),3);
			this.tradeOffers.add(new MerchantRecipe(buyItem?item:metal,null,buyItem?metal:item, 0,1));
		}
		int hatCount=this.rand.nextInt(3);
		
		for(int i=0;i<hatCount;i++){
			
			boolean buyItem=this.rand.nextBoolean();
			ItemStack item=ItemFromData.getRandomWeaponOfClass("cosmetic", this.rand, false);
			int cost = Math.max(1,ItemFromData.getData(item).getInt(PropertyType.COST));
			ItemStack metal=new ItemStack(TF2weapons.itemTF2,cost/18,5);
			ItemStack metal2=new ItemStack(TF2weapons.itemTF2,this.rand.nextInt(3),4);
			if(metal2.stackSize==0)
				metal2=null;
			
			this.tradeOffers.add(new MerchantRecipe(buyItem?item:metal,buyItem?null:metal2,buyItem?metal:item, 0,1));
		}
	}
	@Override
	public void setRecipes(MerchantRecipeList recipeList) {
		this.tradeOffers=recipeList;
		
	}
	public int[] getValidSlots(){
		return new int[]{0,1,2};
	}
	@Override
	public void useRecipe(MerchantRecipe recipe) {
		recipe.incrementToolUses();
		if(recipe.getItemToBuy().getItem() instanceof ItemWeapon){
			this.setHeldItem(EnumHand.MAIN_HAND, recipe.getItemToBuy());
		}
		
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        int i = 3 + this.rand.nextInt(4);

        this.lastTrader=this.trader;
        this.tradeCount.put(this.trader, this.tradeCount.containsKey(trader)?this.tradeCount.get(this.trader)+1:1);
        this.traderFollowTicks=Math.min(4800,this.tradeCount.get(this.trader)*250+350);
        if (recipe.getRewardsExp())
        {
            this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY + 0.5D, this.posZ, i));
        }
		
	}
	public void switchSlot(int slot){
    	this.setHeldItem(EnumHand.MAIN_HAND, this.loadout[slot]);
    }
	@Override
	public void verifySellingItem(ItemStack stack) {
		// TODO Auto-generated method stub
		
	}
	public int getMaxSpawnedInChunk()
    {
		return 4;
    }
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier)
    {
		if(this.getAITarget() instanceof EntityPlayer&&this.getAITarget().getTeam()!=null){
			for(ItemStack stack:loadout){
				if( stack != null && stack.getItem() instanceof ItemFromData && this.rand.nextFloat() <= ItemFromData.getData(stack).getFloat(PropertyType.DROP_CHANCE)*(1+lootingModifier*0.4f)){
					stack.stackSize=1;
					if(TF2EventBusListener.isSpawnEvent(worldObj)){
						TF2Attribute.upgradeItemStack(stack,(int) (Math.min(480, worldObj.getWorldTime()/3000)+rand.nextInt((int) Math.min(640, worldObj.getWorldTime()/2250))), rand);
					}
					this.entityDropItem(stack, 0);
				}
			}
		}
		super.dropEquipment(wasRecentlyHit, lootingModifier);
    }
	
	/*@Override
	public void writeSpawnData(ByteBuf buffer) {
		PacketBuffer packet=new PacketBuffer(buffer);
		for(int i=0;i<this.loadout.length;i++){
			packet.writeByte(i);
			packet.writeItemStackToBuffer(this.loadout[i]);
		}
	}
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		PacketBuffer packet=new PacketBuffer(additionalData);
		while(packet.readableBytes()>0){
			try {
				this.loadout[packet.readByte()]=packet.readItemStackFromBuffer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/
}
