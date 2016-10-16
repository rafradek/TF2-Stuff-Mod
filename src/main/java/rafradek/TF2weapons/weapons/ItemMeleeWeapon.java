package rafradek.TF2weapons.weapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

public class ItemMeleeWeapon extends ItemBulletWeapon {

	public float getMaxRange(){
		return 2.4f;
	}
	
	public float getBulletSize(){
		return 0.35f;
	}
	
	public boolean showTracer(ItemStack stack){
		return false;
	}
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return TF2Attribute.getModifier("Ball Release", item, 0, player)>0?2000:super.getAltFiringSpeed(item, player);
	}
	public void altUse(ItemStack stack, EntityLivingBase living,
			World world) {
		if(TF2Attribute.getModifier("Ball Release", stack, 0, living)>0){
			ItemStack ballStack=getNewStack("sandmanball");
			if(ItemAmmo.searchForAmmo(living, ballStack)!=null){
				ItemStack oldHeldItem=living.getHeldItemMainhand();
				living.setHeldItem(EnumHand.MAIN_HAND, ballStack);
				((ItemProjectileWeapon) ballStack.getItem()).use(ballStack,living,world,EnumHand.MAIN_HAND,null);
				living.setHeldItem(EnumHand.MAIN_HAND, oldHeldItem);
			}
		}
	}
	@Override
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand, PredictionMessage message)
    {
		ItemWeapon.shouldSwing=true;
		living.swingArm(hand);
		ItemWeapon.shouldSwing=false;
		return super.use(stack, living, world, hand, message);
	}
	
	public float critChance(ItemStack stack, Entity entity){
    	float chance=0.15f;
    	if(ItemUsable.lastDamage.containsKey(entity)){
			for(int i=0;i<20;i++){
				chance+=ItemUsable.lastDamage.get(entity)[i]/177;
			}
    	}
    	return Math.min(chance,0.6f);
    }
	public boolean doMuzzleFlash(ItemStack stack, EntityLivingBase attacker){
		return false;
	}
}
