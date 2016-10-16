package rafradek.TF2weapons.characters;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;

public class EntityDemoman extends EntityTF2Character{
	
	public ItemStack stickyBombLauncher=ItemFromData.getNewStack("stickybomblauncher");
	
	public EntityDemoman(World par1World) {
		super(par1World);
		if(this.attack !=null){
			this.attack.setRange(20F);
			this.attack.projSpeed=1.16205f;
			this.attack.gravity=0.0381f;
			this.attack.setDodge(true,false);
		}
		this.rotation=10;
		this.ammoLeft=12;
		this.experienceValue=15;
		
		//this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemUsable.getNewStack("Minigun"));
		
	}
	/*protected ResourceLocation getLootTable()
    {
        return TF2weapons.lootDemoman;
    }*/
	/*protected void addWeapons()
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemFromData.getNewStack("grenadelauncher"));
    }*/
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.5D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.302D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }
	/*public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(this.ammoLeft>0&&this.getAttackTarget()!=null&&this.getDistanceSqToEntity(this.getAttackTarget())<=400&&(!TF2ActionHandler.playerAction.get(this.worldObj.isRemote).containsKey(this)||(TF2ActionHandler.playerAction.get(this.worldObj.isRemote).get(this)&3)==0)){
    		TF2ActionHandler.playerAction.get(this.worldObj.isRemote).put(this, TF2ActionHandler.playerAction.get(this.worldObj.isRemote).containsKey(this)?TF2ActionHandler.playerAction.get(this.worldObj.isRemote).get(this)+2:2);
    	}
    }*/
	protected SoundEvent getAmbientSound()
    {
        return TF2Sounds.MOB_DEMOMAN_SAY;
    }
	

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected SoundEvent getHurtSound()
    {
        return TF2Sounds.MOB_DEMOMAN_HURT;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected SoundEvent getDeathSound()
    {
        return TF2Sounds.MOB_DEMOMAN_DEATH;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    	if(this.rand.nextFloat()<0.075f+p_70628_2_*0.0375f){
    		this.entityDropItem(ItemFromData.getNewStack("grenadelauncher"), 0);
    	}
    	if(this.rand.nextFloat()<0.06f+p_70628_2_*0.03f){
    		this.entityDropItem(ItemFromData.getNewStack("stickybomblauncher"), 0);
    	}
    }
	/*@Override
	public float getAttributeModifier(String attribute) {
		if(attribute.equals("Minigun Spinup")){
			return super.getAttributeModifier(attribute)*1.5f;
		}
		return super.getAttributeModifier(attribute);
	}*/
}
