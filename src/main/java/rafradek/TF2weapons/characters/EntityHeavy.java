package rafradek.TF2weapons.characters;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.weapons.ItemWeapon;

public class EntityHeavy extends EntityTF2Character{
	
	public EntityHeavy(World par1World) {
		super(par1World);
		if(this.attack !=null){
			this.attack.setDodge(true,true);
			this.attack.dodgeSpeed=1.25f;
		}
		this.rotation=10;
		this.ammoLeft=133;
		this.experienceValue=15;
		//this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemUsable.getNewStack("Minigun"));
		
	}
	protected ResourceLocation getLootTable()
    {
        return TF2weapons.lootHeavy;
    }
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }
	public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(this.ammoLeft>0&&this.getAttackTarget()!=null&&this.getDistanceSqToEntity(this.getAttackTarget())<=350&&(this.getCapability(TF2weapons.WEAPONS_CAP, null).state&2)==0){
        	this.getCapability(TF2weapons.WEAPONS_CAP, null).state+=2;
    	}
    }
	protected SoundEvent getAmbientSound()
    {
        return TF2Sounds.MOB_HEAVY_SAY;
    }
	

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected SoundEvent getHurtSound()
    {
        return TF2Sounds.MOB_HEAVY_HURT;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected SoundEvent getDeathSound()
    {
        return TF2Sounds.MOB_HEAVY_DEATH;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    	if(this.rand.nextFloat()<0.15f+p_70628_2_*0.075f){
    		this.entityDropItem(ItemFromData.getNewStack("shotgun"), 0);
    	}
    	if(this.rand.nextFloat()<0.05f+p_70628_2_*0.025f){
    		this.entityDropItem(ItemFromData.getNewStack("minigun"), 0);
    	}
    }
	@Override
	public float getAttributeModifier(String attribute) {
		if(this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityPlayer){
			if(attribute.equals("Minigun Spinup")){
				return this.getDiff()==1 ? 2f : (this.getDiff()==3 ? 1.2f : 1.55f);
			}
		}
		return super.getAttributeModifier(attribute);
	}
}
