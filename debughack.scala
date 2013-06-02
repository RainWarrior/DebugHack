/*
Copyright (c) 2013 RainWarrior

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package rainwarrior.debughack

import scala.collection.mutable.ListBuffer
import java.util.logging.Logger
import cpw.mods.fml.{ common, relauncher }
import common.{ Mod, event, network, FMLCommonHandler, SidedProxy }
import relauncher.FMLRelaunchLog
import network.NetworkMod

import net.minecraftforge.event.ForgeSubscribe
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraft.client.Minecraft
import Minecraft.{ getMinecraft => mc }

trait LoadLater extends DelayedInit {
  var stuff = new ListBuffer[() => Unit]
  var fired = false

  def delayedInit(code: => Unit) {
    if(!fired) {
      stuff += (() => code)
    } else {
      code
    }
  }

  def init() {
    fired = true
    stuff.toList.foreach(_())
  }
}

object CommonProxy extends LoadLater {
}

object ClientProxy extends LoadLater {
  import cpw.mods.fml.client.registry._
  net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this)

  @ForgeSubscribe
  def onRenderWorld(e: RenderWorldLastEvent) {
    mc.gameSettings.showDebugInfo = false
  }
}

sealed class CommonProxy
class ClientProxy extends CommonProxy {
  CommonProxy.delayedInit(ClientProxy.init())
}

@Mod(
  modLanguage = "scala",
  modid = DebugMod.modId,
  name = DebugMod.modName,
  version = "0.01"
)
@NetworkMod(
//  channels = Array(modId),
  clientSideRequired = true,
  serverSideRequired = false
)
object DebugMod {
  final val modId = "DebugHack"
  final val modName = "Debug Hack"

  val log = Logger.getLogger(modId)
  log.setParent(FMLRelaunchLog.log.getLogger)

  def isServer() = FMLCommonHandler.instance.getEffectiveSide.isServer

  @SidedProxy(
    clientSide="rainwarrior.debughack.ClientProxy",
    serverSide="rainwarrior.debughack.CommonProxy")
  var proxy: CommonProxy = null

  @Mod.Init def init(e: event.FMLInitializationEvent) = CommonProxy.init()
}

object MovingRegistry {
}
