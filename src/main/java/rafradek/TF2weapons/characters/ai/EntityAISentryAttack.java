package rafradek.TF2weapons.characters.ai;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.MapList;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.projectiles.EntityProjectileBase;
import rafradek.TF2weapons.weapons.ItemWeapon;

public class EntityAISentryAttack extends EntityAIBase {

	public EntitySentry host;
	public EntityLivingBase target;
	private boolean lockTarget;
	public EntityAISentryAttack(EntitySentry sentry){
		this.host=sentry;
		this.setMutexBits(1);
	}
	public void resetTask()
    {
		this.target=null;
		this.host.setSoundState(0);
    }
	@Override
	public boolean shouldExecute() {
		// TODO Auto-generated method stub
		return !this.host.isDisabled()&&!this.host.isControlled()&&(target=this.host.getAttackTarget())!=null&&this.host.getEntitySenses().canSee(this.host.getAttackTarget());
	}
	public void updateTask()
    {
		//System.out.println("Executing: "+this.target+" "+this.host.attackDelay);
    	if((this.target != null && this.target.deathTime>0) || this.host.deathTime>0){
    		this.resetTask();
    		return;
    	}
    	if(this.target == null){
    		return;
    	}
    	EntityLivingBase owner=this.host.getOwner();
    	if(owner==null||owner.isDead){
    		owner=this.host;
    	}
    	double lookX=this.target.posX;
        double lookY=this.target.posY+this.target.height/2;
        double lookZ=this.target.posZ;
        if(this.lockTarget){
        	this.host.getLookHelper().setLookPosition(lookX,lookY,lookZ,30, 75);
        }
        else{
        	this.host.getLookHelper().setLookPosition(lookX,lookY,lookZ,5f+this.host.getLevel()*2.25f,50);

        }
        if(TF2weapons.lookingAt(this.host,12,lookX,lookY,lookZ)){
        	this.lockTarget=true;
        	this.host.shootBullet(owner);
        	this.host.shootRocket(owner);
        }
        else{
        	this.lockTarget=false;
        	if(this.host.getSoundState()>2){
        		this.host.setSoundState(1);
        	}
        }
    }
}
