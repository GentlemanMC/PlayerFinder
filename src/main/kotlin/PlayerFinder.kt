import com.lambda.client.event.events.PacketEvent
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.EntityUtils.isFakeOrSelf
import com.lambda.client.util.math.CoordinateConverter.asString
import com.lambda.client.util.text.MessageSendHelper.sendChatMessage
import com.lambda.client.util.threads.safeListener
import com.lambda.event.listener.listener
import net.minecraft.entity.item.EntityBoat
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketEntityTeleport
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketMaps
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.world.storage.MapData
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object PlayerFinder: PluginModule(
    name = "PlayerFinder",
    category = Category.MISC,
    description = "Finds players using a brownmen exploit",
    pluginMain = PluginExample
) {
    private val packetsPerTick by setting("Packets per tick", 2, 0..5, 1, description = "Amount of packets sent per tick")

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            if (player.timeInPortal > 0 && player.ridingEntity is EntityBoat) {
                if (player.inventory.getCurrentItem().item == Items.MAP) {
                    connection.sendPacket(CPacketPlayerTryUseItemOnBlock(player.position, EnumFacing.UP, EnumHand.MAIN_HAND, 0f, -1337.77f, 0f))
                }
                for (i in 0..packetsPerTick) {
                    connection.sendPacket(CPacketPlayer.Position(player.posX, -1337.77, player.posZ, false))
                    connection.sendPacket(CPacketSteerBoat(false, true))
                }
            }

            world.playerEntities.filter { players ->
                !players.isFakeOrSelf
            }.forEach { players ->
                sendChatMessage("$chatName Found player@(${players.position.asString()})")
            }
        }

        listener<PacketEvent.Receive> {
            when (it.packet) {
                is SPacketMaps -> (it.packet as SPacketMaps).setMapdataTo(MapData("Lambda on top"))
                is SPacketEntityVelocity -> it.cancel()
                is SPacketEntityTeleport -> it.cancel()
            }
        }

        listener<PacketEvent.Send> {
            when (it.packet) {
                is CPacketConfirmTeleport -> it.cancel()
                is CPacketPlayerTryUseItem -> it.cancel()
            }
        }
    }
}