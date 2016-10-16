package rafradek.TF2weapons.message;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.BulletMessage;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.weapons.ItemBulletWeapon;
import rafradek.TF2weapons.weapons.ItemWeapon;

public class TF2ProjectileHandler implements IMessageHandler<TF2Message.PredictionMessage, IMessage> {

	//public static HashMap<Entity, ArrayList<PredictionMessage>> nextShotPos= new HashMap<Entity, ArrayList<PredictionMessage>>();
	
	@Override
	public IMessage onMessage(PredictionMessage message, MessageContext ctx) {
		EntityPlayer shooter=ctx.getServerHandler().playerEntity;
		//ItemStack stack=shooter.getHeldItem(EnumHand.MAIN_HAND);
		shooter.getCapability(TF2weapons.WEAPONS_CAP, null).predictionList.add(message);
		return null;
	}

}
