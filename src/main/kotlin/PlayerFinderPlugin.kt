import com.lambda.client.plugin.api.Plugin

internal object PlayerFinderPlugin: Plugin() {

    override fun onLoad() {
        // Load any modules, commands, or HUD elements here
        modules.add(PlayerFinder)
    }

    override fun onUnload() {
        // Here you can unregister threads etc...
    }
}