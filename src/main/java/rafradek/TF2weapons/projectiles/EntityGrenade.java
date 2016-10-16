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
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2weapons;

public class EntityGrenade extends EntityProjectileBase {

	public boolean hitGround;
	
	public int fuse = 46;

	public EntityGrenade(World p_i1756_1_) {
		super(p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}
	
	public EntityGrenade(World p_i1756_1_, EntityLivingBase p_i1756_2_,EnumHand hand) {
		super(p_i1756_1_, p_i1756_2_, hand);
		this.setSize(0.3f, 0.3f);
	}
	
	public float getPitchAddition(){
    	return -3;
    }
	
	@Override
	public void onHitGround(int x, int y, int z, RayTraceResult mop) {
		
	}
	@Override
	public void onHitMob(Entity entityHit, RayTraceResult mop) {
		if(!this.hitGround){
			this.explode(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,mop.entityHit,1);
		}
	}
	public double maxMotion(){
		return Math.max(this.motionX, Math.max(this.motionY, this.motionZ));
	}
	
	public void onUpdate()
    {
		super.onUpdate();
		this.fuse--;
		if(this.fuse<=0){
			this.explode(this.posX, this.posY+this.height/2, this.posZ, null,0.64f);
		}
		if(this.isCollided){
			this.hitGround=true;
			if(!this.worldObj.isRemote){
				int attr=(int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, (EntityLivingBase) this.shootingEntity);
	        	if(attr==2){
	        		this.explode(this.posX, this.posY, this.posZ, null, 0.64f);
	        	}
	        	if(attr==1){
	        		this.setDead();
	        	}
			}
		}
    }
	public void spawnParticles(double x, double y, double z){
		
	}
	protected float getSpeed()
    {
        return 1.16205f;
    }
    
    protected double getGravity()
    {
        return 0.0381f;
    }
    
    public boolean useCollisionBox(){
		return true;
	}

    public void onHitBlockX(){
    	this.motionX=-this.motionX*0.18;
    	this.motionY=this.motionY*0.8;
    	this.motionZ=this.motionZ*0.8;
	}
	public void onHitBlockY(Block block){
		this.motionX=this.motionX*0.8;
    	this.motionY=-this.motionY*0.18;
    	this.motionZ=this.motionZ*0.8;
	}
	public void onHitBlockZ(){
		this.motionX=this.motionX*0.8;
    	this.motionY=this.motionY*0.8;
    	this.motionZ=-this.motionZ*0.18;
	}
	
}
