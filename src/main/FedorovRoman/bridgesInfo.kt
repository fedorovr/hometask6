package FedorovRoman

import bridges.*
import bridges.BridgeState.CLOSED
import bridges.SwitchAction.CHANGE
import bridges.SwitchType.HALF_BLOCK

class BridgesInfoBuilder {
    private val bridges: MutableMap<Char, Bridge> = mutableMapOf()
    private val switches: MutableMap<Char, Switch> = mutableMapOf()

    fun bridge(name: Char, initialState: BridgeState = CLOSED, init: BridgeBuilder.() -> Unit = {}) {
        Bridge(name, initialState).apply {
            BridgeBuilder(this).init()
            bridges[name] = this
        }
    }

    inner class BridgeBuilder(val bridge: Bridge) {
        fun switch(name: Char, action: SwitchAction = CHANGE, type: SwitchType = HALF_BLOCK) {
            switches[name] = Switch(name, bridge, action, type)
        }
    }

    fun build(): BridgesInfo {
        return BridgesInfo(bridges, switches)
    }
}

fun bridgesInfo(init: BridgesInfoBuilder.() -> Unit): BridgesInfo = BridgesInfoBuilder().apply { init() }.build()
