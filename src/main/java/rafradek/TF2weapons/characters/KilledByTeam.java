package rafradek.TF2weapons.characters;

import java.util.Random;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class KilledByTeam implements LootCondition {

	@Override
	public boolean testCondition(Random rand, LootContext context) {
		
		return context.getKillerPlayer() !=null && context.getKillerPlayer().getTeam() != null;
	}

	public static class Serializer extends LootCondition.Serializer<KilledByTeam>
    {
        public Serializer()
        {
            super(new ResourceLocation("killed_by_player_team"), KilledByTeam.class);
        }

        public void serialize(JsonObject json, KilledByTeam value, JsonSerializationContext context)
        {
            //json.addProperty("inverse", Boolean.valueOf(value));
        }

        public KilledByTeam deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return new KilledByTeam();
        }
    }
}
