package rafradek.TF2weapons.characters;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.characters.ai.EntityAIAmbush;
import rafradek.TF2weapons.characters.ai.EntityAIRepair;
import rafradek.TF2weapons.characters.ai.EntityAISetup;

public class EntityEngineer extends EntityTF2Character {
	
	public EntitySentry sentry;
	public EntityDispenser dispenser;
	public int metal=500;
	public EntityEngineer(World p_i1738_1_) {
		super(p_i1738_1_);
		this.ammoLeft=24;
		this.experienceValue=15;
		this.rotation=15;
		this.tasks.addTask(3, new EntityAIRepair(this,1,2f));
		this.tasks.addTask(5, new EntityAISetup(this));
		this.tasks.removeTask(wander);
		if(this.attack !=null){
			attack.setRange(20);
		}
	}
	protected ResourceLocation getLootTable()
    {
        return TF2weapons.lootEngineer;
    }
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }
	protected SoundEvent getAmbientSound()
    {
        return TF2Sounds.MOB_ENGINEER_SAY;
    }
	public int[] getValidSlots(){
		return new int[]{0,1,2};
	}

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected SoundEvent getHurtSound()
    {
        return TF2Sounds.MOB_ENGINEER_HURT;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected SoundEvent getDeathSound()
    {
        return TF2Sounds.MOB_ENGINEER_DEATH;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    	if(this.rand.nextFloat()<0.10f+p_70628_2_*0.05f){
    		this.entityDropItem(ItemFromData.getNewStack("pistol"), 0);
    	}
    	if(this.rand.nextFloat()<0.10f+p_70628_2_*0.05f){
    		this.entityDropItem(ItemFromData.getNewStack("shotgun"), 0);
    	}
    	if(this.rand.nextFloat()<0.10f+p_70628_2_*0.05f){
    		this.entityDropItem(ItemFromData.getNewStack("wrench"), 0);
    	}
    	this.entityDropItem(new ItemStack(TF2weapons.itemBuildingBox,1,18+this.rand.nextInt(3)*2+this.getEntTeam()), 0);
    }
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        if(this.sentry!=null&&this.sentry.isEntityAlive()){
        	NBTTagCompound sentryTag=new NBTTagCompound();
        	this.sentry.writeToNBTAtomically(sentryTag);
        	par1NBTTagCompound.setTag("Sentry", sentryTag);
        }
        if(this.dispenser!=null&&this.dispenser.isEntityAlive()){
        	NBTTagCompound dispenserTag=new NBTTagCompound();
        	this.dispenser.writeToNBTAtomically(dispenserTag);
        	par1NBTTagCompound.setTag("Dispenser", dispenserTag);
        }
        
    }
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        if(par1NBTTagCompound.hasKey("Sentry")){
        	//System.out.println(par1NBTTagCompound.getCompoundTag("Sentry"));
        	this.sentry=(EntitySentry) AnvilChunkLoader.readWorldEntityPos(par1NBTTagCompound.getCompoundTag("Sentry"), this.worldObj,this.posX,this.posY,this.posZ,true);
        	//this.worldObj.spawnEntityInWorld(sentry);
        }
        if(par1NBTTagCompound.hasKey("Dispenser")){
        	this.dispenser=(EntityDispenser) AnvilChunkLoader.readWorldEntityPos(par1NBTTagCompound.getCompoundTag("Dispenser"), this.worldObj,this.posX,this.posY,this.posZ,true);
        	//dispenser.readFromNBT(par1NBTTagCompound.getCompoundTag("Dispenser"));
        	//this.worldObj.spawnEntityInWorld(dispenser);
        }
    }
}
