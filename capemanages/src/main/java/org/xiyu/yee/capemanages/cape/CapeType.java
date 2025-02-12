package org.xiyu.yee.capemanages.cape;

public enum CapeType {

    PARTY("Birthday Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/party_cape.png"),
    SCROLLS("Scrolls Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/scrolls_cape.png"),
    PRISMARINE("Prismarine Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/prismarine_cape.png"),
    VOLGAR("Volgar Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/volgar_cape.png"),
    MOJANG_STUDIOS("Mojang Studios Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/ms_cape.png"),
    VALENTINE("Valentine Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/valentine_cape.png"),
    PURPLE_HEART("Purple Heart Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/purple_heart_cape.png"),
    SPADE("Spade Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mrmessiah_2d.png"),
    MOJANG_CAPE("Mojang Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mojang_cape.png"),
    MOJANG_NEW("New Mojang Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mojang_cape_2015.png"),
    TURTLE("Turtle Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/turtle_cape.png"),
    VANILLA("Vanilla Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/vanilla_cape.png"),

    MINECON_2011("Minecon 2011 Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2011_cape.png"),
    MINECON_2012("Minecon 2012 Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2012_cape.png"),
    MINECON_2013("Minecon 2013 Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2013_cape.png"),
    MINECON_2015("Minecon 2015 Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2015_cape.png"),
    MINECON_2016("Minecon 2016 Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2016_cape.png"),
    BACON("Bacon Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/bacon_cape.png"),
    MILLIONTH("Millionth Player Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/1mcape.png"),
    DB("DB Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/db_cape.png"),
    TRANSLATOR("Translator Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/chinese_translator_cape.png"),
    MOJIRA("Mojira Moderator Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/bugtracker_cape.png"),
    REALMS("Realms Mapmaker Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/mapmaker_cape_texture.png"),
    MIGRATOR("Migrator Cape", 
        "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/ms_cape.png");

    private final String displayName;
    private final String url;

    CapeType(String displayName, String url) {
        this.displayName = displayName;
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return name().toLowerCase();
    }
} 