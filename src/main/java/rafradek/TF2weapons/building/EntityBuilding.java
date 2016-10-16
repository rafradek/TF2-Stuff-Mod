package rafradek.TF2weapons.building;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemSapper;

public class EntityBuilding extends EntityCreature implements IEntityOwnable{
	
	public EntityLivingBase owner;
	public BuildingSound buildingSound;
	public ItemStack sapper;
	public EntityLivingBase sapperOwner;
	private static final DataParameter<Byte> VIS_TEAM = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> LEVEL = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> SOUND_STATE = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> PROGRESS = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> SAPPED = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.createKey(EntityBuilding.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	public EntityBuilding(World worldIn) {
		super(worldIn);
		this.applyTasks();
		//this.notifyDataManagerChange(LEVEL);
	}
	
	public EntityBuilding(World worldIn, EntityLivingBase owner) {
		this(worldIn);
		this.setOwner(owner);
		this.applyTasks();
	}
	public void applyTasks(){
		
	}
	public boolean canBePushed()
    {
        return false;
    }
	public void applyEntityCollision(Entity entityIn)
    {
		if(entityIn.getEntityBoundingBox().intersectsWith(this.getCollisionBoundingBox())){
			super.applyEntityCollision(entityIn);
		}
    }
	public void notifyDataManagerChange(DataParameter<?> key){
		this.adjustSize();
		//System.out.println("Watcher update: "+data);
		if(this.worldObj.isRemote&&key==SOUND_STATE){
			SoundEvent sound=this.getSoundNameForState(this.getSoundState());
			if(sound!=null){
				//System.out.println("Playing Sound: "+sound);
				if(this.buildingSound!=null){
					this.buildingSound.stopPlaying();
				}
				this.buildingSound=new BuildingSound(this,sound,this.getSoundState());
				ClientProxy.playBuildingSound(buildingSound);
			}
		}
	}
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack)
    {
		if(!this.worldObj.isRemote&&player==this.getOwner()&&hand==EnumHand.MAIN_HAND){
			this.grab();
			return true;
		}
		return false;
    }
	public void grab()
    {
		ItemStack stack=new ItemStack(TF2weapons.itemBuildingBox,1,(this instanceof EntitySentry?18:(this instanceof EntityDispenser?20:22))+this.getEntTeam());
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setTag("SavedEntity", new NBTTagCompound());
		this.writeEntityToNBT(stack.getTagCompound().getCompoundTag("SavedEntity"));
		this.entityDropItem(stack, 0);
		//System.out.println("Saved: "+stack.getTagCompound().getCompoundTag("SavedEntity"));
		this.setDead();
    }
	public void adjustSize(){
		
	}
	public SoundEvent getSoundNameForState(int state){
		return state==50?TF2Sounds.MOB_SAPPER_IDLE:null;
	}
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0D);
    }
	public boolean canBreatheUnderwater()
    {
        return true;
    }
	public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if(source==DamageSource.onFire){
        	return false;
        }
        return super.attackEntityFrom(source, amount);
    }
	public void setFire(int time){
		super.setFire(0);
	}
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(VIS_TEAM, (byte)this.rand.nextInt(2));
		this.dataManager.register(OWNER_UUID, Optional.<UUID>absent());
		this.dataManager.register(LEVEL, (byte)1);
		this.dataManager.register(SOUND_STATE, (byte)0);
		this.dataManager.register(PROGRESS, 0);
		this.dataManager.register(SAPPED, (byte) 0);
		this.adjustSize();
	}
	
	public int getSoundState(){
		return (int) this.dataManager.get(SOUND_STATE);
	}
	public void setSoundState(int state){
		this.dataManager.set(SOUND_STATE, (byte)state);
	}
	@Override
	public UUID getOwnerId() {
		// TODO Auto-generated method stub
		return this.dataManager.get(OWNER_UUID).orNull();
	}

	@Override
	public EntityLivingBase getOwner() {
		// TODO Auto-generated method stub
		if(this.owner!=null&&!(this.owner instanceof EntityPlayer && this.owner.isDead)){
			return this.owner;
		}
		else if(this.getOwnerId()!=null){
			return this.owner=this.worldObj.getPlayerEntityByUUID(this.getOwnerId());
		}
		//System.out.println("owner: "+this.getOwnerId());
		return null;
	}

	public void setOwner(EntityLivingBase owner) {
		// TODO Auto-generated method stub
		this.owner=owner;
		if(owner instanceof EntityPlayer){
			this.dataManager.set(OWNER_UUID,Optional.of(owner.getUniqueID()));
		}
	}
	public void onUpdate(){
		this.motionX=0;
		this.motionZ=0;
		if(this.motionY>0){
			this.motionY=0;
		}
		if(!this.worldObj.isRemote&&this.isSapped()){
			TF2weapons.dealDamage(this, this.worldObj, this.sapperOwner, this.sapper, 0, this.sapper==null?0.14f:((ItemSapper)this.sapper.getItem()).getWeaponDamage(sapper,this.sapperOwner,this), TF2weapons.causeDirectDamage(this.sapper, this.sapperOwner, 0));
		}
		super.onUpdate();
	}
	public void setSapped(EntityLivingBase owner,ItemStack sapper){
		this.sapperOwner=owner;
		this.sapper=sapper;
		this.dataManager.set(SAPPED, (byte)2);
		this.setSoundState(50);
	}
	public boolean isSapped(){
		return this.dataManager.get(SAPPED)>0;
	}
	public boolean isDisabled(){
		return this.isSapped() || this.getActivePotionEffect(TF2weapons.stun)!=null;
	}
	public void removeSapper(){
		dataManager.set(SAPPED,(byte)(dataManager.get(SAPPED)-1));
		if(!isSapped()){
			this.setSoundState(0);
			this.playSound(TF2Sounds.MOB_SAPPER_DEATH, 1.5f, 1f);
			this.dropItem(Items.IRON_INGOT,1);
		}
	}
	public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return this.isEntityAlive()?entityIn.getEntityBoundingBox():null;
    }
    public AxisAlignedBB getCollisionBoundingBox()
    {
    	if(!this.isEntityAlive())
    		return null;
    	/*else if(this.height>this.getCollHeight()){
	    	AxisAlignedBB colBox=this.getEntityBoundingBox();
	    	colBox=colBox.expand((this.getCollWidth()-this.width)/2, (this.getCollHeight()-this.height)/2, (this.getCollWidth()-this.width)/2);
	    	colBox=colBox.offset(0, this.getEntityBoundingBox().minY-colBox.minY, 0);
	    	return colBox;
    	}*/
        return this.getEntityBoundingBox();
    }
	public Team getTeam(){
    	return this.getOwner()!=null?this.getOwner().getTeam():(this.getEntTeam()==0?this.worldObj.getScoreboard().getTeam("RED"):this.worldObj.getScoreboard().getTeam("BLU"));
    }

	public int getProgress(){
		return this.dataManager.get(PROGRESS);
	}
	
	public void setProgress(int progress){
		this.dataManager.set(PROGRESS,progress);
	}
	
	public int getLevel(){
		return (int) this.dataManager.get(LEVEL);
	}
	
	public void setLevel(int level){
		this.dataManager.set(LEVEL,(byte)level);
	}
	
	public void upgrade(){
		this.setLevel(this.getLevel()+1);
		this.setProgress(0);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()*1.2);
		this.setHealth(this.getMaxHealth());
		this.adjustSize();
	}
	
	public int getEntTeam(){
		return (int) this.dataManager.get(VIS_TEAM);
	}
	
	public void setEntTeam(int team){
		this.dataManager.set(VIS_TEAM,(byte)team);
	}
	public boolean writeToNBTOptional(NBTTagCompound tagCompund){
		return this.getOwner() instanceof EntityPlayer?super.writeToNBTOptional(tagCompund):false;
	}
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        
        par1NBTTagCompound.setByte("Team", (byte) this.getEntTeam());
        par1NBTTagCompound.setByte("Level", (byte) this.getLevel());
        par1NBTTagCompound.setShort("Progress", (byte) this.getProgress());
        par1NBTTagCompound.setShort("Sapper", (byte) this.dataManager.get(SAPPED));
        if(this.getOwnerId()!=null)
        	par1NBTTagCompound.setUniqueId("Owner", this.getOwnerId());
    }
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        
        this.setEntTeam(par1NBTTagCompound.getByte("Team"));
        this.setLevel(par1NBTTagCompound.getByte("Level"));
        this.setProgress(par1NBTTagCompound.getByte("Progress"));
        if(par1NBTTagCompound.getByte("Sapper")!=0){
        	this.setSapped(this, null);
        }
        UUID ownerID=par1NBTTagCompound.getUniqueId("Owner");
        if(ownerID !=null){
        	this.dataManager.set(OWNER_UUID, Optional.of(ownerID));
        	this.getOwner();
        }
    }
	public float getCollHeight(){
		return 1f;
	}
	public float getCollWidth(){
		return 0.95f;
	}
	public boolean canUseWrench(){
		return this.getMaxHealth()>this.getHealth()||this.getLevel()<3;
	}
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    	for(int i=0;i<(this.getOwner() instanceof EntityPlayer && !(this instanceof EntityDispenser)?4:3);i++){
    		this.dropItem(Items.IRON_INGOT, 1);
    	}
    }
	protected boolean canDespawn()
    {
        return this.getOwnerId() ==null;
    }
}
