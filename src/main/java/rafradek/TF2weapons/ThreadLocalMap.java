package rafradek.TF2weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;

public class ThreadLocalMap<A,B> {
	
	public ConcurrentHashMap<A,B> client=new ConcurrentHashMap<A,B>();
	public ConcurrentHashMap<A,B> server=new ConcurrentHashMap<A,B>();
	
	public Map<A,B> get(boolean client){
		return client?this.client:this.server;
	}
	
}
