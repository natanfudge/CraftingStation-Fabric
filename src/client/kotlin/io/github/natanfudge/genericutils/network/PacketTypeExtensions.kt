package io.github.natanfudge.genericutils.network

import io.github.natanfudge.genericutils.network.C2SPacketType
import io.github.natanfudge.genericutils.network.S2CPacketType
import io.github.natanfudge.genericutils.ClientInit
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

fun <T> C2SPacketType<T>.send(value: T) =
    ClientPlayNetworking.send(id, PacketByteBufs.create().also { format.encodeToByteBuf(serializer, value, it) })

context(ClientInit)
fun <T> S2CPacketType<T>.register(receiveOnClient: (server: MinecraftClient, handler: ClientPlayNetworkHandler, content: T, responseSender: PacketSender) -> Unit) {
    ClientPlayNetworking.registerGlobalReceiver(id) { client, handler, buf, responseSender ->
        val content = format.decodeFromByteBuf(serializer, buf)
        client.execute {
            receiveOnClient(client, handler, content, responseSender)
        }
    }
}