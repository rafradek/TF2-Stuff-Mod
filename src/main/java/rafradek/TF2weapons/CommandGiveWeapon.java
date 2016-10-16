package rafradek.TF2weapons;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class CommandGiveWeapon extends CommandBase {

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		// TODO Auto-generated method stub
		return "command.giveweapon.usage";
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return "giveweapon";
	}

	@Override
	public void execute(MinecraftServer server,ICommandSender sender, String[] args) throws CommandException {
		if(args.length<1){
			throw new WrongUsageException("commands.giveweapon.usage", new Object[0]);
		}
		try{
			ItemStack item=ItemFromData.getNewStack(args[0]);
			NBTTagCompound attributes=item.getTagCompound().getCompoundTag("Attributes");
			for(int i=2;i<args.length;i++){
				String[] attr=args[i].split(":");
				attributes.setFloat(attr[0], Float.parseFloat(attr[1]));
			}
			EntityPlayerMP entityplayermp = args.length > 1 ? getPlayer(server,sender, args[1]) : getCommandSenderAsPlayer(sender);
			EntityItem entityitem = entityplayermp.dropItem(item, false);
	        entityitem.setPickupDelay(0);
	        //entityitem.func_145797_a(entityplayermp.getCommandSenderName());
	        //func_152373_a(p_71515_1_, this, "commands.giveweapon.success", new Object[] {item.func_151000_E(), entityplayermp.getCommandSenderName()});
		}
		catch(Exception e){
			throw new CommandException("Error");
		}
	}

}
