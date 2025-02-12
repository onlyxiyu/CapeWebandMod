private byte[] downloadWithHttpClient(String url) {
    try {
        // 处理LittleSkin URL
        if (url.contains("littleskin.cn/preview")) {
            // 将预览URL转换为raw下载URL
            String rawUrl = url.replace("/preview/", "/raw/");
            System.out.println("Converting LittleSkin preview URL to raw URL: " + rawUrl);
            return downloadDirectUrl(rawUrl);
        }
        
        // 处理Gitee URL
        if (url.contains("gitee.com")) {
            String processedUrl = url + "?download=true&t=" + System.currentTimeMillis();
            System.out.println("Processing Gitee URL: " + processedUrl);
            return downloadDirectUrl(processedUrl);
        }
        
        // 其他URL尝试直接下载
        return downloadDirectUrl(url);
    } catch (Exception e) {
        System.out.println("Download error: " + e.getMessage());
        e.printStackTrace();
        return null;
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
        System.out.println("Direct download error: " + e.getMessage());
        e.printStackTrace();
    }
    return null;
}

private void downloadAndSaveFile(String url, Path savePath) {
    try {
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
            
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "image/png")
            .build();
            
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(savePath));
        
        if (response.statusCode() == 200) {
            System.out.println("File downloaded successfully to: " + savePath);
        } else {
            System.out.println("Failed to download file: HTTP " + response.statusCode());
        }
    } catch (Exception e) {
        System.out.println("File download error: " + e.getMessage());
        e.printStackTrace();
    }
}

// 在玩家加入服务器时调用
public void onPlayerJoin(String playerName) {
    sendCurrentCapeState(playerName);
}

// 在切换披风时调用
public void switchCape(String playerName, String url) {
    setPlayerCape(playerName, url, true);
    sendCurrentCapeState(playerName);
} 