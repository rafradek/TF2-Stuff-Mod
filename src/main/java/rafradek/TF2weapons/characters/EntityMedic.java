package rafradek.TF2weapons.characters;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.characters.ai.EntityAINearestChecked;
import rafradek.TF2weapons.characters.ai.EntityAIUseMedigun;
import rafradek.TF2weapons.message.TF2ActionHandler;

public class EntityMedic extends EntityTF2Character {
	
	public boolean melee;
	public EntityMedic(World par1World) {
		super(par1World);
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, this.findplayer=new EntityAINearestChecked(this, EntityLivingBase.class, true,false,this.getEntitySelector(), true));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(3, new EntityAINearestChecked(this, EntityLivingBase.class, true,false,super.getEntitySelector(), true));
		this.unlimitedAmmo=true;
		this.ammoLeft=1;
		this.experienceValue=15;
		this.rotation=15;
		this.tasks.removeTask(attack);
		
		if(par1World!=null){
			this.tasks.addTask(4, this.attack=new EntityAIUseMedigun(this, 1.0F, 20.0F));
			attack.setRange(7f);
			this.setCombatTask(true);
			this.friendly=true;
		}
		//this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemUsable.getNewStack("Minigun"));
		
	}
	protected ResourceLocation getLootTable()
    {
        return TF2weapons.lootMedic;
    }
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(17D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.325D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }
	public void onLivingUpdate()
    {
		
        super.onLivingUpdate();
        if(this.getEntityData().getInteger("HealTarget")>0){
        	this.ignoreFrustumCheck=true;
        }
        else{
        	this.ignoreFrustumCheck=false;
        }
        
    }
	public void setAttackTarget(EntityLivingBase entity){
		this.alert=true;
		if(TF2weapons.isOnSameTeam(this, entity)){
			//System.out.println("friendly");
			if(!friendly){
				this.friendly=true;
				this.setCombatTask(true);
				
			}
		}
		else if(entity != null&&this.friendly){
			//System.out.println("not friendly");
			this.friendly=false;
			this.setCombatTask(false);
		}
		super.setAttackTarget(entity);
	}
	protected SoundEvent getAmbientSound()
    {
        return TF2Sounds.MOB_MEDIC_SAY;
    }
	public int getDefaultSlot() {
		// TODO Auto-generated method stub
		return 1;
	}

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected SoundEvent getHurtSound()
    {
        return TF2Sounds.MOB_MEDIC_HURT;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected SoundEvent getDeathSound()
    {
        return TF2Sounds.MOB_MEDIC_DEATH;
    }
    /**
     * Plays step sound at given x, y, z for the entity
     */

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    	if(this.rand.nextFloat()<0.15f+p_70628_2_*0.075f){
    		this.entityDropItem(ItemFromData.getNewStack("syringegun"), 0);
    	}
    	if(this.rand.nextFloat()<0.08f+p_70628_2_*0.03f){
    		this.entityDropItem(ItemFromData.getNewStack("medigun"), 0);
    	}
    }
    @Override
	public float getAttributeModifier(String attribute) {
    	if(attribute.equals("Heal")){
			return this.getDiff()==1 ? 0.75f : (this.getDiff()==3 ? 1f : 0.9f);
		}
    	if(attribute.equals("Overheal")){
			return this.getDiff()==1 ? 0.55f : (this.getDiff()==3 ? 0.85f : 0.7f);
		}
		return super.getAttributeModifier(attribute);
	}
    
    public float getMotionSensitivity(){
    	return 0f;
    }
    public Predicate<EntityLivingBase> getEntitySelector(){
		return new Predicate<EntityLivingBase>(){
			public boolean apply(EntityLivingBase target)
	        {
				//System.out.println("Valid target: "+target+" "+TF2weapons.isOnSameTeam(EntityMedic.this,target)+" "+!(target instanceof EntityMedic||target instanceof EntityBuilding));
				return !(target instanceof EntityMedic||target instanceof EntityBuilding)&&TF2weapons.isOnSameTeam(EntityMedic.this,target);
	        }
		};
	}
}
