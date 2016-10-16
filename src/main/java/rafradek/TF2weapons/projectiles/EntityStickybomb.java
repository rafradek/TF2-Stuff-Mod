package rafradek.TF2weapons.projectiles;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemStickyLauncher;

public class EntityStickybomb extends EntityProjectileBase {

	public EntityStickybomb(World p_i1756_1_) {
		super(p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}
	
	public EntityStickybomb(World p_i1756_1_, EntityLivingBase p_i1756_2_, EnumHand hand) {
		super(p_i1756_1_, p_i1756_2_, hand);
		this.setSize(0.3f, 0.3f);
	}
	
	public float getPitchAddition(){
    	return 3;
    }
	
	@Override
	public void onHitGround(int x, int y, int z, RayTraceResult mop) {
		
	}
	@Override
	public void onHitMob(Entity entityHit, RayTraceResult mop) {
		
	}
	public double maxMotion(){
		return Math.max(this.motionX, Math.max(this.motionY, this.motionZ));
	}
	public void spawnParticles(double x, double y, double z){

	}
	public void onUpdate()
    {
		super.onUpdate();
		if(!this.shootingEntity.isEntityAlive()){
			this.setDead();
		}
    }
	public void setDead(){
		super.setDead();
		if(!this.worldObj.isRemote){
			ItemStickyLauncher.activeBombs.get(this.shootingEntity).remove(this);
		}
	}
	protected float getSpeed()
    {
        return 0.7667625f;
    }
    
    protected double getGravity()
    {
        return 0.0381f;
    }
    public boolean isSticky(){
    	return true;
    }
    public boolean useCollisionBox(){
		return true;
	}
    public int getMaxTime(){
		return 72000;
	}
    public void onHitBlockX(){
    	this.motionX=0;
    	this.motionY=0;
    	this.motionZ=0;
	}
	public void onHitBlockY(Block block){
		this.motionX=0;
    	this.motionY=0;
    	this.motionZ=0;
	}
	public void onHitBlockZ(){
		this.motionX=0;
    	this.motionY=0;
    	this.motionZ=0;
	}
	
}
