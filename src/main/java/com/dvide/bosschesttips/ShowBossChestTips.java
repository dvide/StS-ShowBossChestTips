package com.dvide.bosschesttips;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.util.Objects;

@SpireInitializer
@SuppressWarnings("unused")
public class ShowBossChestTips implements PostInitializeSubscriber {
    public static void initialize() {
        new ShowBossChestTips();
    }

    public ShowBossChestTips() {
        BaseMod.subscribe(this);
    }

    @Override
    public void receivePostInitialize() {
        final ModInfo ourModInfo = getOurModInfo();
        final Texture badgeTexture = ImageMaster.loadImage("resources.dvide.bosschesttips/images/ModBadge.png");
        BaseMod.registerModBadge(badgeTexture, ourModInfo.Name, ourModInfo.Authors[0], ourModInfo.Description, null);
    }

    private static ModInfo getOurModInfo() {
        final String ourModID = "ShowBossChestTips";

        for (ModInfo modInfo : Loader.MODINFOS) {
            if (Objects.equals(modInfo.ID, ourModID)) {
                return modInfo;
            }
        }

        throw new RuntimeException("Failed to load ModInfo for '" + ourModID + "'.");
    }
}
