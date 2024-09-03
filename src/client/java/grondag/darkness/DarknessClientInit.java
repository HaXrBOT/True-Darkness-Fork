package grondag.darkness;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class DarknessClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        int protocolVersion = 0;
        // spotless:off
        //#if MC >= 12100
        ClientLoginNetworking.registerGlobalReceiver(ResourceLocation.parse(DarknessInit.MOD_ID),
                //#elseif MC < 12100
                //$$ ClientLoginNetworking.registerGlobalReceiver(ResourceLocation.tryParse(DarknessInit.MOD_ID),
                //#endif
                //spotless:on
                (client, handler, buf, callback) -> {
                    int version = buf.readVarInt();
                    if (version > protocolVersion) {
                        handler.handleDisconnect(new ClientboundLoginDisconnectPacket(
                                Component.literal("Your client has an older version of " + DarknessInit.MOD_NAME
                                        + " than what is on the server!")));
                        return CompletableFuture.completedFuture(null);
                    } else if (version < protocolVersion) {
                        handler.handleDisconnect(new ClientboundLoginDisconnectPacket(
                                Component.literal("Your client has a newer version of " + DarknessInit.MOD_NAME
                                        + " than what is on the server!")));
                        return CompletableFuture.completedFuture(null);
                    } else {
                        return CompletableFuture.completedFuture(new FriendlyByteBuf(Unpooled.EMPTY_BUFFER));
                    }
                });
    }
}
