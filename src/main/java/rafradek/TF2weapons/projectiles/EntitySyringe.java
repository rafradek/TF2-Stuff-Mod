package rafradek.TF2weapons.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;

public class EntitySyringe extends EntityProjectileSimple {

	public boolean sticked;
	
	public EntitySyringe(World world){
		super(world);
		this.setSize(0.3F, 0.3F);
	}
	
	public EntitySyringe(World world, EntityLivingBase living, EnumHand hand) {
		super(world, living, hand);
		this.setSize(0.3F, 0.3F);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onHitGround(int x, int y, int z, RayTraceResult mop) {
		super.onHitGround(x, y, z, mop);
		this.setPosition(mop.hitVec.xCoord+mop.sideHit.getFrontOffsetX()*0.1, mop.hitVec.yCoord-mop.sideHit.getFrontOffsetY()*0.15f, mop.hitVec.zCoord+mop.sideHit.getFrontOffsetZ()*0.1);
		this.sticked=true;
		this.stickedBlock=mop.getBlockPos();
	}

	public void onUpdate(){
		if(this.ticksExisted>this.getMaxTime()||(this.worldObj.isRemote&&this.sticked&&this.worldObj.isAirBlock(stickedBlock))){
			this.setDead();
			return;
		}
		else if(!this.sticked){
			super.onUpdate();
		}
	}
	public void setDead(){
		if(this.impact){
			this.impact=false;
		}
		else{
			super.setDead();
		}
	}
	@Override
	public void spawnParticles(double x, double y, double z) {
		// TODO Auto-generated method stub

	}
	public boolean moveable(){
		return !this.sticked;
	}
}
