package io.github.natanfudge.genericutils.network

import io.github.natanfudge.genericutils.csId
import io.github.natanfudge.genericutils.CommonInit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.Buf
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier


class C2SPacketType<T>(val id: Identifier, val serializer: KSerializer<T>, val format: Buf) {

    context(CommonInit)
    fun register(receiveOnServer: (server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, content: T, responseSender: PacketSender) -> Unit) {
        ServerPlayNetworking.registerGlobalReceiver(id) { server, player, handler, buf, responseSender ->
            val content = format.decodeFromByteBuf(serializer, buf)
            server.execute {
                receiveOnServer(server, player, handler, content, responseSender)
            }
        }
    }
}

inline fun <reified T> c2sPacket(path: String, format: Buf = Buf) = C2SPacketType<T>(csId(path), format.serializersModule.serializer(), format)

class S2CPacketType<T>(val id: Identifier, val serializer: KSerializer<T>, val format: Buf) {
    fun send(value: T, player: ServerPlayerEntity) {
        ServerPlayNetworking.send(player, id, PacketByteBufs.create().also { format.encodeToByteBuf(serializer, value, it) })
    }
}

inline fun <reified T> s2cPacket(path: String, format: Buf = Buf) = S2CPacketType<T>(csId(path), format.serializersModule.serializer(), format)