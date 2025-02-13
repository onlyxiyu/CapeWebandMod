package org.xiyu.yee.capemanages.cape;

public enum CapeType {
    YEAR_15("15 Year Anniversary",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/15year_texture.png"),
    MINECON_2011("Minecon 2011",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2011_cape.png"),
    MINECON_2012("Minecon 2012",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2012_cape.png"),
    MINECON_2013("Minecon 2013",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2013_cape.png"),
    MINECON_2015("Minecon 2015",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2015_cape.png"),
    MINECON_2016("Minecon 2016",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/minecon_2016_cape.png"),
    BACON("Bacon",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/bacon_cape.png"),
    MILLIONTH("Millionth Customer",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/1mcape.png"),
    DB("Diamond Blue",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/db_cape.png"),
    TRANSLATOR("Chinese Translator",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/chinese_translator_cape.png"),
    MOJIRA("Mojira Moderator",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/bugtracker_cape.png"),
    REALMS("Mapmaker",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/mapmaker_cape_texture.png"),
    EYEBLOSSOM("Eye of Ender",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_1/eyeblossom_texture.png"),
    // 来自OfficalCape_2
    PARTY("Birthday",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/party_cape.png"),
    SCROLLS("Scrolls",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/scrolls_cape.png"),
    PRISMARINE("Prismarine",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/prismarine_cape.png"),
    VOLGAR("Volgar",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/volgar_cape.png"),
    MOJANG_STUDIOS("Mojang Studios",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/ms_cape.png"),
    VALENTINE("Valentine's Day",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/valentine_cape.png"),
    PURPLE_HEART("Purple Heart",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/purple_heart_cape.png"),
    SPADE("Spade",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mrmessiah_2d.png"),
    MOJANG_CAPE("Mojang Classic",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mojang_cape.png"),
    MOJANG_NEW("Mojang New",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/mojang_cape_2015.png"),
    TURTLE("Turtle",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/turtle_cape.png"),
    VANILLA("Vanilla",
            "https://gitee.com/god_xiyu/capeimage/releases/download/OfficalCape_2/vanilla_cape.png"),
    MIGRATOR("Migrator",
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