package rafradek.TF2weapons.building;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2DamageSource;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemWrench;

public class EntitySapper extends EntityBuilding {

	public EntityBuilding sappedBuilding;
	public ItemStack sapperItem;
	
	public EntitySapper(World worldIn) {
		super(worldIn);
		this.setSize(1f, 1.1f);
	}
	public EntitySapper(World worldIn,EntityLivingBase owner, EntityBuilding building, ItemStack sapper) {
		super(worldIn,owner);
		this.setSize(building.width+0.1f,building.height+0.1f);
		//building.sapper=this;
		this.sappedBuilding=building;
		this.sapperItem=sapper;
	}
	public void onLivingUpdate(){
		super.onLivingUpdate();
		if(!this.worldObj.isRemote){
			if(this.sappedBuilding==null||!this.sappedBuilding.isEntityAlive()){
				this.setDead();
				return;
			}
			TF2weapons.dealDamage(this.sappedBuilding, this.worldObj, this.getOwner(), sapperItem, 0, 0.25f, TF2weapons.causeBulletDamage(sapperItem, this.getOwner(), 0, this));
		
			if(!this.isEntityAlive()&&this.sappedBuilding!=null){
				this.sappedBuilding.sapper=null;
			}
		}
		
	}
	public SoundEvent getSoundNameForState(int state){
		switch(state){
		case 0:return TF2Sounds.MOB_SAPPER_IDLE;
		default:return null;
		}
	}
	public boolean attackEntityFrom(DamageSource source, float amount){
		if((source instanceof TF2DamageSource)&&((TF2DamageSource)source).getWeapon() != null && ((TF2DamageSource)source).getWeapon().getItem() instanceof ItemWrench){
			super.attackEntityFrom(source, amount);
		}
		return false;
	}
	protected void entityInit()
	{
		super.entityInit();
	}
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12D);
    }
	protected SoundEvent getHurtSound()
    {
        return null;
    }

    protected SoundEvent getDeathSound()
    {
        return TF2Sounds.MOB_SAPPER_DEATH;
    }
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
    }
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
    }
}
