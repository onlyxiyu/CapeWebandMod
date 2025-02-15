package org.xiyu.yee.capemanages.cape;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.xiyu.yee.capemanages.Capemanages;
import org.xiyu.yee.capemanages.network.CapeUpdatePacket;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.mojang.text2speech.Narrator.LOGGER;

public class CapeManager {
    public static final CapeManager INSTANCE = new CapeManager();
    private static final String CHANNEL_NAME ="cape_channel"; ;
    private final Map<String, ResourceLocation> playerCapes = new HashMap<>();
    private final Map<String, CapeInfo> capeInfoMap = new HashMap<>();
    private boolean enabled = true;
    private final Gson gson = new Gson();

    public static SimpleChannel NETWORK;

    // 添加披风缓存
    private final Map<String, byte[]> capeCache = new HashMap<>();
    private final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION = 1000 * 60 * 30; // 30分钟缓存

    public void init() {
        try {
            // 获取Minecraft游戏目录
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            // 创建cape目录
            Path capeDir = gameDir.resolve("cape");
            Files.createDirectories(capeDir);
            
            // 创建缓存目录
            Path cacheDir = capeDir.resolve("cache");
            Files.createDirectories(cacheDir);
            
            LOGGER.info("Cape directories initialized at: {}", capeDir);
        } catch (IOException e) {
            LOGGER.error("Failed to initialize cape directories", e);
        }

        // 获取游戏根目录并打印
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        System.out.println("Game directory: " + gameDir.toAbsolutePath());

        // 获取当前玩家名称
        String playerName = Minecraft.getInstance().getUser().getName();
        System.out.println("Current player: " + playerName);

        // 确保玩家配置文件存在
        ensurePlayerConfig(gameDir.resolve("cape"), playerName);

        loadCapes();

        // 延迟加载玩家披风
        Minecraft.getInstance().tell(this::loadLastUsedCape);
    }

    public static void registerNetworking() {
        // 使用新的ChannelBuilder API
        NETWORK = ChannelBuilder.named(new ResourceLocation(Capemanages.MODID, CHANNEL_NAME))
            .networkProtocolVersion(1)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

        // 注册消息
        NETWORK.messageBuilder(CapeUpdatePacket.class, 0)
            .encoder(CapeUpdatePacket::encode)
            .decoder(CapeUpdatePacket::decode)
            .consumerMainThread(CapeUpdatePacket::handle)
            .add();
    }

    private void loadCapes() {
        System.out.println("Loading predefined capes...");
        capeInfoMap.clear();

        // 从CapeType枚举加载预定义披风
        for (CapeType type : CapeType.values()) {
            capeInfoMap.put(type.getId(), new CapeInfo(type.getDisplayName(), type.getUrl()));
            System.out.println("Loaded cape: " + type.getId() + " -> " + type.getDisplayName());
        }

        System.out.println("Successfully loaded " + capeInfoMap.size() + " capes");
        System.out.println("Available cape IDs: " + String.join(", ", capeInfoMap.keySet()));
    }

    private void loadLastUsedCape() {
        try {
            String playerName = Minecraft.getInstance().getUser().getName();
            System.out.println("Loading cape for player: " + playerName);

            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            Path path = gameDir.resolve("cape").resolve(playerName + ".json");

            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    String type = json.get("type").getAsString();
                    String name = json.get("name").getAsString();

                    System.out.println("Found saved cape data for " + playerName + ": type=" + type + ", name=" + name);

                    // 获取披风URL并广播
                    String capeUrl = getCapeUrl(playerName);
                    if (capeUrl != null) {
                        System.out.println("Broadcasting saved cape: " + capeUrl);
                        broadcastCapeUpdate(playerName, capeUrl);
                    } else {
                        // 如果获取URL失败，尝试使用预定义披风
                        if ("predefined".equals(type)) {
                            try {
                                CapeType capeType = CapeType.valueOf(name.toUpperCase());
                                setCape(playerName, capeType);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Invalid cape type in config for " + playerName + ", resetting to default");
                                setCape(playerName, CapeType.VANILLA);
                            }
                        } else if ("custom".equals(type)) {
                            setCustomCape(playerName);
                        }
                    }
                }
            } else {
                // 如果配置文件不存在，创建默认配置
                ensurePlayerConfig(gameDir.resolve("cape"), playerName);
                // 设置默认披风
                setCape(playerName, CapeType.VANILLA);
            }
        } catch (Exception e) {
            System.out.println("Failed to load last used cape: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCape(String playerName, CapeType capeType) {
        String capeId = capeType.getId();
        System.out.println("Setting cape...");
        System.out.println("Player: " + playerName);
        System.out.println("Cape Type: " + capeType.name());
        System.out.println("Cape ID: " + capeId);

        // 检查当前披风是否与要设置的相同
        String currentCape = getCurrentCape(playerName);
        if (currentCape != null && currentCape.equals(capeType.getDisplayName())) {
            System.out.println("Cape already set to: " + currentCape);
            return;
        }

        try {
            // 获取披风URL并添加参数
            String processedUrl = capeType.getUrl();
            if (processedUrl.contains("gitee.com")) {
                processedUrl = processedUrl + "?download=true&t=" + System.currentTimeMillis();
            }

            // 创建唯一的ResourceLocation
            ResourceLocation capeLocation = new ResourceLocation(
                Capemanages.MODID,
                "textures/cape/" + capeId
            );

            System.out.println("Downloading cape from: " + processedUrl);

            // 下载并缓存披风
            HttpURLConnection connection = (HttpURLConnection) new URL(processedUrl).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Referer", "https://gitee.com/");
            connection.setRequestProperty("Accept", "image/png");
            connection.setRequestProperty("Accept-Encoding", "identity");

            int responseCode = connection.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[10240];
                    int nRead;
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();

                    byte[] imageData = buffer.toByteArray();
                    System.out.println("Downloaded " + imageData.length + " bytes");

                    try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                        NativeImage image = NativeImage.read(bis);
                        DynamicTexture texture = new DynamicTexture(image);

                        // 在主线程上注册纹理
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().getTextureManager().register(capeLocation, texture);
                            System.out.println("Successfully registered texture: " + capeLocation);
                        });

                        // 保存披风数据
                        playerCapes.put(playerName, capeLocation);
                        saveCapeData(playerName, new CapeData("predefined", capeId));

                        // 广播更新
                        broadcastCapeUpdate(playerName, processedUrl);

                        System.out.println("Successfully set cape: " + capeType.getDisplayName());
                    }
                }
            } else {
                throw new IOException("Server returned HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Failed to set cape: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCustomCape(String playerName) {
        try {
            // 对用户名进行URL编码
            String encodedUsername = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
            String apiUrl = "http://110.42.96.4:40017/api/users/" + encodedUsername;

            System.out.println("Fetching custom cape for: " + playerName);
            System.out.println("Encoded API URL: " + apiUrl);

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject response = gson.fromJson(reader, JsonObject.class);
                    String capeUrl = response.get("url").getAsString();

                    // 保存配置
                    saveCapeData(playerName, new CapeData("custom", "custom"));
                    Component.literal("§8[§bCape§8] §r §aoi小伙子，你的披风已经设置好了！\n记得进群昂：§b426345022").getString();

                    // 广播URL
                    broadcastCapeUpdate(playerName, capeUrl);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch custom cape for " + playerName + ": " + e.getMessage());
            e.printStackTrace();
            Component.literal("§8[§cCape§8] §r §c布什哥们，你真以为能自定义啊").getString();
        }
    }

    public void setPlayerCape(String playerName, String url, boolean save) {
        try {
            System.out.println("Setting cape for player: " + playerName + " with URL: " + url);

            // 为URL添加参数
            String processedUrl = url;
            if (url.contains("gitee.com")) {
                processedUrl = url + "?download=true&t=" + System.currentTimeMillis();
            } else {
                processedUrl = url + "?t=" + System.currentTimeMillis();
            }

            // 使用mod ID作为命名空间，使用URL的哈希作为路径
            ResourceLocation capeLocation = new ResourceLocation(
                Capemanages.MODID,
                "textures/cape/" + String.format("%08x", url.hashCode())
            );

            System.out.println("Created ResourceLocation: " + capeLocation);
            System.out.println("Downloading cape from: " + processedUrl);

            downloadAndCacheCape(processedUrl, capeLocation);
            playerCapes.put(playerName, capeLocation);

            if (save) {
                saveLastUsedCape(playerName, url);
            }

            System.out.println("Successfully set cape for " + playerName);
            broadcastCapeUpdate(playerName, url);

        } catch (Exception e) {
            System.out.println("Failed to set cape: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadAndCacheCape(String url, ResourceLocation location) {
        try {
            System.out.println("Attempting to load cape: " + url);

            // 清理旧的缓存文件
            cleanupCacheFiles();

            // 使用固定的缓存文件名（基于URL的哈希）
            String cacheFileName = String.format("%08x", url.hashCode()) + ".png";
            Path cacheDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("cape").resolve("cache");

            // 确保缓存目录存在
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            Path cacheFile = cacheDir.resolve(cacheFileName);

            // 如果缓存文件已存在且有效，直接使用
            if (Files.exists(cacheFile)) {
                System.out.println("Using cached cape file: " + cacheFileName);
                byte[] fileData = Files.readAllBytes(cacheFile);
                applyTextureFromBytes(fileData, location);
                return;
            }

            // 异步下载披风
            CompletableFuture.runAsync(() -> {
                try {
                    byte[] imageData;

                    // 检查URL类型
                    if (url.contains("littleskin.cn/preview")) {
                        // 如果是LittleSkin预览URL，转换为raw下载URL
                        String rawUrl = url.replace("/preview/", "/raw/");
                        imageData = downloadWithHttpClient(rawUrl);
                    } else if (url.contains("gitee.com")) {
                        // Gitee需要特殊处理
                        String processedUrl = url + "?download=true&t=" + System.currentTimeMillis();
                        imageData = downloadWithHttpClient(processedUrl);
                    } else {
                        // 其他URL直接下载
                        imageData = downloadWithHttpClient(url);
                    }

                    if (imageData != null && imageData.length > 0) {
                        // 验证图片数据
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                            NativeImage testImage = NativeImage.read(bis);
                            if (testImage != null && testImage.getWidth() > 0) {
                                // 保存到缓存
                                Files.write(cacheFile, imageData);
                                // 应用纹理
                                applyTextureFromBytes(imageData, location);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Failed to download cape: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("Failed to start cape download: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] downloadWithHttpClient(String url) {
        try {
            // URL编码处理
            String encodedUrl = url;
            if (url.contains("littleskin.cn/preview")) {
                // 将预览URL转换为raw下载URL，并处理中文编码
                String rawUrl = url.replace("/preview/", "/raw/");
                encodedUrl = encodeUrl(rawUrl);
                System.out.println("Converting LittleSkin preview URL to raw URL: " + encodedUrl);
                return downloadDirectUrl(encodedUrl);
            } else if (url.contains("gitee.com")) {
                // 处理Gitee URL，添加时间戳和下载参数
                encodedUrl = encodeUrl(url + "?download=true&t=" + System.currentTimeMillis());
                System.out.println("Processing Gitee URL: " + encodedUrl);
                return downloadDirectUrl(encodedUrl);
            } else {
                // 其他URL也需要编码
                encodedUrl = encodeUrl(url);
                return downloadDirectUrl(encodedUrl);
            }
        } catch (Exception e) {
            System.out.println("Download error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String encodeUrl(String url) {
        try {
            // 分解URL
            URI uri = new URI(url);
            // 重新构建URL，对路径部分进行编码
            return new URI(
                uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            ).toASCIIString();
        } catch (Exception e) {
            System.out.println("URL encoding error: " + e.getMessage());
            return url;
        }
    }

    private byte[] downloadDirectUrl(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "image/png")
                .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                if (contentType.startsWith("image/")) {
                    return response.body();
                } else {
                    System.out.println("Invalid content type: " + contentType);
                }
            } else {
                System.out.println("Failed to download: HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Download error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void applyTextureFromBytes(byte[] imageData, ResourceLocation location) {
        Minecraft.getInstance().execute(() -> {
            try {
                // 先检查并移除已存在的纹理
                if (Minecraft.getInstance().getTextureManager().getTexture(location) != null) {
                    Minecraft.getInstance().getTextureManager().release(location);
                }
                
                // 创建新的纹理
                NativeImage image = NativeImage.read(new ByteArrayInputStream(imageData));
                DynamicTexture texture = new DynamicTexture(image);
                
                // 注册新纹理
                Minecraft.getInstance().getTextureManager().register(location, texture);
                
                System.out.println("Successfully applied texture: " + location);
            } catch (IOException e) {
                System.err.println("Failed to apply texture: " + e.getMessage());
            }
        });
    }

    private void broadcastCapeUpdate(String playerName, String url) {
        if (NETWORK != null) {
            System.out.println("Broadcasting cape update for " + playerName);
            CapeUpdatePacket packet = new CapeUpdatePacket(playerName, url);

            // Ensure the server instance is available
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {

                // waaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                NETWORK.send(PacketDistributor.ALL.noArg(), () -> packet);
                // Apply the update locally
                applyUpdate(playerName, url);
            } else {
                System.out.println("Server instance is not available. Cannot broadcast cape update.");
            }
        }
    }

    // 修改handleUpdatePacket方法
    public void handleUpdatePacket(String playerName, String url) {
        System.out.println("Received cape update for " + playerName + ": " + url);

        // 创建唯一的ResourceLocation
        ResourceLocation location = new ResourceLocation(
            Capemanages.MODID,
            "textures/cape/" + String.format("%08x", url.hashCode())
        );

        // 检查是否已加载相同的纹理
        if (playerCapes.containsKey(playerName) &&
            playerCapes.get(playerName).equals(location)) {
            System.out.println("Cape already loaded for " + playerName);
            return;
        }

        // 在主线程上执行纹理更新
        Minecraft.getInstance().execute(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Referer", "https://gitee.com/");
                connection.setRequestProperty("Accept", "image/png");
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 先检查并释放旧的纹理
                    if (Minecraft.getInstance().getTextureManager().getTexture(location) != null) {
                        Minecraft.getInstance().getTextureManager().release(location);
                    }

                    // 读取并验证图片数据
                    try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                        NativeImage image = NativeImage.read(in);
                        if (image != null && image.getWidth() > 0) {
                            DynamicTexture texture = new DynamicTexture(image);
                            Minecraft.getInstance().getTextureManager().register(location, texture);
                            playerCapes.put(playerName, location);
                            System.out.println("Successfully applied cape update for: " + playerName);
                        } else {
                            System.out.println("Invalid image data received for cape");
                        }
                    }
                } else {
                    System.out.println("Failed to download cape: HTTP " + connection.getResponseCode());
                }
            } catch (Exception e) {
                System.out.println("Failed to apply cape update: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // 修改sendCurrentCapeState方法
    public void sendCurrentCapeState(String playerName) {
        String capeUrl = getCapeUrl(playerName);
        if (capeUrl != null) {
            System.out.println("Sending current cape state for: " + playerName);
            CapeUpdatePacket packet = new CapeUpdatePacket(playerName, capeUrl);
            NETWORK.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    // 抽取共同的更新逻辑到单独的方法
    private void applyUpdate(String playerName, String url) {
        try {
            // 创建唯一的ResourceLocation
            ResourceLocation capeLocation = new ResourceLocation(
                Capemanages.MODID,
                "textures/cape/" + String.format("%08x", url.hashCode())
            );

            // 下载并缓存披风
            downloadAndCacheCape(url, capeLocation);

            // 更新玩家披风映射
            playerCapes.put(playerName, capeLocation);

            System.out.println("Applied cape update for player: " + playerName);
        } catch (Exception e) {
            System.out.println("Failed to apply cape update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveLastUsedCape(String playerName, String url) {
        // 使用游戏根目录
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        Path path = gameDir.resolve("cape").resolve(playerName + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("type", "custom");
        json.addProperty("name", "clouds");

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(json, writer);
            System.out.println("Saved cape data for: " + playerName);
        } catch (IOException e) {
            System.out.println("Failed to save cape data: " + e.getMessage());
        }
    }

    public ResourceLocation getPlayerCape(String playerName) {
        return playerCapes.get(playerName);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCurrentCape(String playerName) {
        ResourceLocation cape = playerCapes.get(playerName);
        if (cape != null) {
            // 修改这里，使用游戏根目录
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            Path path = gameDir.resolve("cape").resolve(playerName + ".json");

            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    String type = json.get("type").getAsString();
                    String name = json.get("name").getAsString();

                    if ("predefined".equals(type)) {
                        CapeInfo capeInfo = capeInfoMap.get(name);
                        return capeInfo != null ? capeInfo.name : name;
                    } else {
                        return "Custom cape";
                    }
                } catch (IOException e) {
                    System.out.println("Failed to read cape data: " + e.getMessage());
                }
            }
            return "Unknown cape";
        }
        return null;
    }

    private void saveCapeData(String playerName, CapeData capeData) {
        Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
        Path path = gameDir.resolve("cape").resolve(playerName + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("type", capeData.type);
        json.addProperty("name", capeData.name);

        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(json, writer);
            System.out.println("Saved cape data for: " + playerName);
        } catch (IOException e) {
            System.out.println("Failed to save cape data: " + e.getMessage());
        }
    }

    public Map<String, CapeInfo> getCapeInfoMap() {
        return Collections.unmodifiableMap(capeInfoMap);
    }

    private static class CapeInfo {
        final String name;
        final String url;

        CapeInfo(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    private static class CapeData {
        String type;  // "predefined" 或 "custom"
        String name;  // 预定义披风的ID或"custom"

        CapeData(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    // 新增方法：确保玩家配置文件存在
    private void ensurePlayerConfig(Path capePath, String playerName) {
        try {
            Path playerJsonPath = capePath.resolve(playerName + ".json");
            if (!Files.exists(playerJsonPath)) {
                // 创建玩家的初始披风配置文件
                JsonObject json = new JsonObject();
                json.addProperty("type", "predefined");  // 默认使用预定义披风
                json.addProperty("name", "vanilla");     // 默认使用vanilla披风

                try (Writer writer = Files.newBufferedWriter(playerJsonPath)) {
                    gson.toJson(json, writer);
                    System.out.println("Created initial cape configuration for player: " + playerName);
                }
            } else {
                // 验证现有配置文件的有效性
                try (Reader reader = Files.newBufferedReader(playerJsonPath)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    if (!json.has("type") || !json.has("name")) {
                        // 如果配置文件无效，重新创建
                        json = new JsonObject();
                        json.addProperty("type", "predefined");
                        json.addProperty("name", "vanilla");

                        try (Writer writer = Files.newBufferedWriter(playerJsonPath)) {
                            gson.toJson(json, writer);
                            System.out.println("Recreated invalid cape configuration for player: " + playerName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to ensure player config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 修改syncCapesForNewPlayer方法
    public void syncCapesForNewPlayer(String playerName) {
        if (Minecraft.getInstance().level != null) {
            for (var player : Minecraft.getInstance().level.players()) {
                String otherPlayerName = player.getName().getString();
                if (!otherPlayerName.equals(playerName)) {
                    String capeUrl = getCapeUrl(otherPlayerName);
                    if (capeUrl != null) {
                        System.out.println("Syncing cape for player: " + otherPlayerName);
                        CapeUpdatePacket packet = new CapeUpdatePacket(otherPlayerName, capeUrl);
                        NETWORK.send(PacketDistributor.ALL.noArg(), packet);
                    }
                }
            }
        }
    }

    // 将getCapeUrl方法改为public
    public String getCapeUrl(String username) {
        try {
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            Path path = gameDir.resolve("cape").resolve(username + ".json");

            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    String type = json.get("type").getAsString();
                    String name = json.get("name").getAsString();

                    if ("predefined".equals(type)) {
                        try {
                            CapeType capeType = CapeType.valueOf(name.toUpperCase());
                            return capeType.getUrl();
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    } else if ("custom".equals(type)) {
                        // 对用户名进行URL编码
                        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
                        String apiUrl = "http://110.42.96.4:40017/api/users/" + encodedUsername;

                        URL url = new URL(apiUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        if (conn.getResponseCode() == 200) {
                            try (Reader apiReader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                                JsonObject response = gson.fromJson(apiReader, JsonObject.class);
                                return response.get("url").getAsString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get cape URL for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 添加新方法：清理缓存文件
    private void cleanupCacheFiles() {
        try {
            Path cacheDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("cape").resolve("cache");

            if (Files.exists(cacheDir)) {
                // 获取所有缓存文件
                Files.list(cacheDir)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.out.println("Failed to delete cache file: " + path);
                        }
                    });
            }
        } catch (IOException e) {
            System.out.println("Failed to cleanup cache files: " + e.getMessage());
        }
    }


    public void syncCurrentPlayerCape(String currentPlayerName) {
        String capeUrl = getCapeUrl(currentPlayerName);
        if (capeUrl != null) {
            System.out.println("Syncing current player cape: " + currentPlayerName);
            CapeUpdatePacket packet = new CapeUpdatePacket(currentPlayerName, capeUrl);
            NETWORK.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    public void onPlayerJoin(String playerName) {
        System.out.println("Handling player join: " + playerName);
        // 同步当前玩家的披风状态
        sendCurrentCapeState(playerName);
        // 同步其他在线玩家的披风状态
        syncCapesForNewPlayer(playerName);
    }

    public void switchCape(String playerName, String url) {
        System.out.println("Switching cape for " + playerName + " to: " + url);
        setPlayerCape(playerName, url, true);
//        sendCurrentCapeState(playerName);
    }

    public void createPlayerCapeJson(String playerName) {
        try {
            Path capeDir = Minecraft.getInstance().gameDirectory.toPath().resolve("cape");
            Path playerJson = capeDir.resolve(playerName + ".json");
            
            if (!Files.exists(playerJson)) {
                JsonObject json = new JsonObject();
                json.addProperty("type", "custom");
                json.addProperty("name", "custom");
                
                try (BufferedWriter writer = Files.newBufferedWriter(playerJson)) {
                    new Gson().toJson(json, writer);
                }
                LOGGER.info("Created cape config for player: {}", playerName);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create cape config for player: {}", playerName, e);
        }
    }
} 