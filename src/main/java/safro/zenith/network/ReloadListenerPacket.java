package safro.zenith.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import safro.zenith.Zenith;
import safro.zenith.api.event.ServerEvents;
import safro.zenith.api.json.ZenithJsonReloadListener;
import safro.zenith.api.placebo.json.PlaceboJsonReloadListener;
import safro.zenith.api.placebo.json.TypeKey;

import java.util.List;

public class ReloadListenerPacket {

    public static class Start {
        public static ResourceLocation ID = new ResourceLocation(Zenith.MODID, "reload_listener_start");

        public static void sendToAll(String path) {
            if (ServerEvents.getCurrentServer() != null) {
                List<ServerPlayer> list = ServerEvents.getCurrentServer().getPlayerList().getPlayers();
                for (ServerPlayer p : list) {
                    sendTo(p, path);
                }
            }
        }

        public static void sendTo(ServerPlayer player, String path) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(path, 50);
            ServerPlayNetworking.send(player, ID, buf);
        }

        public static void init() {
            ClientPlayNetworking.registerGlobalReceiver(ID, ((client, handler, buf, responseSender) -> {
                String msg = buf.readUtf(50);
                ZenithJsonReloadListener.initSync(msg);
                PlaceboJsonReloadListener.initSync(msg);
            }));
        }
    }

    public static class Content {
        public static ResourceLocation ID = new ResourceLocation(Zenith.MODID, "reload_listener_content");

        public static <V extends ZenithJsonReloadListener.TypeKeyed<V>> void sendToAll(String path, ResourceLocation k, V v) {
            if (ServerEvents.getCurrentServer() != null) {
                List<ServerPlayer> list = ServerEvents.getCurrentServer().getPlayerList().getPlayers();
                for (ServerPlayer p : list) {
                    sendTo(p, path, k, v);
                }
            }
        }
        public static <V extends TypeKey<V>> void sendToAllNew(String path, ResourceLocation k, V v) {
            if (ServerEvents.getCurrentServer() != null) {
                List<ServerPlayer> list = ServerEvents.getCurrentServer().getPlayerList().getPlayers();
                for (ServerPlayer p : list) {
                    sendToNew(p, path, k, v);
                }
            }
        }
        public static <V extends TypeKey<V>> void sendToNew(ServerPlayer player, String path, ResourceLocation k, V v) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(path, 50);
            buf.writeResourceLocation(k);
            PlaceboJsonReloadListener.writeItem(path, v, buf);
            ServerPlayNetworking.send(player, ID, buf);
        }

        public static <V extends ZenithJsonReloadListener.TypeKeyed<V>> void sendTo(ServerPlayer player, String path, ResourceLocation k, V v) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(path, 50);
            buf.writeResourceLocation(k);
            ZenithJsonReloadListener.writeItem(path, v, buf);
            ServerPlayNetworking.send(player, ID, buf);
        }


        public static void init(){
            init1();
            init2();
        }
        public static <V extends ZenithJsonReloadListener.TypeKeyed<V>> void init1() {
            ClientPlayNetworking.registerGlobalReceiver(ID, ((client, handler, buf, responseSender) -> {
                String path = buf.readUtf(50);
                ResourceLocation key = buf.readResourceLocation();
                V item = ZenithJsonReloadListener.readItem(path, key, buf);
                ZenithJsonReloadListener.acceptItem(path, key, item);
            }));
        }
        public static <V extends TypeKey<V>> void init2() {
            ClientPlayNetworking.registerGlobalReceiver(ID, ((client, handler, buf, responseSender) -> {
                String path = buf.readUtf(50);
                ResourceLocation key = buf.readResourceLocation();
                V item = PlaceboJsonReloadListener.readItem(path, key, buf);;
                PlaceboJsonReloadListener.acceptItem(path, item);
            }));
        }
    }

    public static class End {
        public static ResourceLocation ID = new ResourceLocation(Zenith.MODID, "reload_listener_end");

        public static void sendToAll(String path) {
            if (ServerEvents.getCurrentServer() != null) {
                List<ServerPlayer> list = ServerEvents.getCurrentServer().getPlayerList().getPlayers();
                for (ServerPlayer p : list) {
                    sendTo(p, path);
                }
            }
        }

        public static void sendTo(ServerPlayer player, String path) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(path, 50);
            ServerPlayNetworking.send(player, ID, buf);
        }

        public static void init() {
            ClientPlayNetworking.registerGlobalReceiver(ID, ((client, handler, buf, responseSender) -> {
                String path = buf.readUtf(50);
                ZenithJsonReloadListener.endSync(path);
                PlaceboJsonReloadListener.endSync(path);
            }));
        }
    }
}
