import com.lambda.client.event.events.PacketEvent
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.TickTimer
import com.lambda.client.util.TimeUnit
import com.lambda.client.util.text.MessageDetection
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.text.MessageSendHelper.sendServerMessage
import com.lambda.client.util.threads.safeListener
import com.lambda.client.event.listener.listener
import net.minecraft.network.play.server.SPacketChat
import net.minecraftforge.fml.common.gameevent.TickEvent

internal object ChatPlusAutoReply : PluginModule(
    name = "AutoReply",
    description = "Automatically reply to direct messages",
    category = Category.CHAT,
    pluginMain = ChatPlusPlugin
) {
    private val customMessage by setting("Custom Message", false)
    private val customText by setting("Custom Text", "unchanged", { customMessage })

    private val timer = TickTimer(TimeUnit.SECONDS)
    private const val defaultMessage = "I just automatically replied, thanks to Lambda's AutoReply module!"

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketChat) {
                val message = (it.packet as SPacketChat).chatComponent.unformattedText
                if (MessageDetection.Direct.RECEIVE detect message) {
                    if (customMessage) {
                        if (!message.contains(customText)) sendServerMessage("/r $customText")
                    } else {
                        if (!message.contains(defaultMessage)) sendServerMessage("/r $defaultMessage")
                    }
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (timer.tick(5L) && customMessage && customText.equals("unchanged", true)) {
                MessageSendHelper.sendWarningMessage("$chatName Warning: In order to use the custom $name, please change the CustomText setting in ClickGUI")
            }
        }
    }
}