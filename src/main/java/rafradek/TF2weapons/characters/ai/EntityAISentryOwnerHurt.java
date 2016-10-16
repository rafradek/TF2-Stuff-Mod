package rafradek.TF2weapons.characters.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import rafradek.TF2weapons.building.EntityBuilding;

public class EntityAISentryOwnerHurt extends EntityAITarget{

	public EntityLivingBase target;
	public int timer;
	public EntityBuilding sentry;
	public EntityAISentryOwnerHurt(EntityBuilding creature, boolean checkSight) {
		super(creature, checkSight);
		this.sentry=creature;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase owner=this.sentry.getOwner();
		if(owner !=null){
			this.target=owner.getAITarget();
			return this.isSuitableTarget(this.target, false);
		}
		return false;
	}
	public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.target);

        super.startExecuting();
    }

}
