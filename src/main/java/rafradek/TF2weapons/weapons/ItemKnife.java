package rafradek.TF2weapons.weapons;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.building.EntityBuilding;

public class ItemKnife extends ItemMeleeWeapon {

	public ItemKnife(){
		super();
		this.addPropertyOverride(new ResourceLocation("backstab"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
            	if(entityIn == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().objectMouseOver!=null&&Minecraft.getMinecraft().objectMouseOver.entityHit!=null&&
            			Minecraft.getMinecraft().objectMouseOver.entityHit.getDistanceSqToEntity(entityIn)<=getMaxRange()*getMaxRange()&&
        				isBackstab(entityIn, Minecraft.getMinecraft().objectMouseOver.entityHit)){
        			return 1;
        		}
            	return 0;
            }
        });
	}
	public boolean isBackstab(EntityLivingBase living, Entity target){
		if(target != null&& target instanceof EntityLivingBase&&!(target instanceof EntityBuilding)){
			float ourAngle=180+MathHelper.wrapDegrees(living.rotationYawHead);
			float angle2=(float) (MathHelper.atan2(living.posX-target.posX, living.posZ-target.posZ) * 180.0D / Math.PI);
			//System.out.println(angle2);
			if(angle2>=0){
				angle2=180-angle2;
			}
			else{
				angle2=-180-angle2;
			}
			angle2+=180;
			float enemyAngle=180+MathHelper.wrapDegrees(target.getRotationYawHead());
			float difference=180 - Math.abs(Math.abs(ourAngle - enemyAngle) - 180); 
			float difference2=180 - Math.abs(Math.abs(angle2 - enemyAngle) - 180); 
			//System.out.println(angle2+" "+difference2+" "+difference);
			if(difference<90&&difference2<90){
				return true;
			}
		}
		return false;
	}
	public float getWeaponDamage(ItemStack stack,EntityLivingBase living, Entity target){
		if(this.isBackstab(living, target)){
			return Math.min(50, ((EntityLivingBase)target).getMaxHealth()*2);
		}
		return super.getWeaponDamage(stack, living, target);
	}
	public int setCritical(ItemStack stack,EntityLivingBase shooter, Entity target, int old){
		return super.setCritical(stack, shooter, target, this.isBackstab(shooter, target)?2:old);
	}
	
	@SideOnly(Side.CLIENT)
    public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
    {
		if(/*Minecraft.getMinecraft().thePlayer!=null&&Minecraft.getMinecraft().thePlayer.getHeldItem(EnumHand.MAIN_HAND)==stack&&
				stack.getItem() instanceof ItemKnife&&*/Minecraft.getMinecraft().objectMouseOver.entityHit!=null&&
				this.isBackstab(player, Minecraft.getMinecraft().objectMouseOver.entityHit)){
			return ClientProxy.nameToModel.get(stack.getTagCompound().getString("Type")+"/b");
		}
		return null;
    }
}
