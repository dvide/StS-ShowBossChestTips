package com.dvide.bosschesttips.patches;

import basemod.ReflectionHacks;
import chronometry.patches.NoSkipBossRelicPatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static com.dvide.bosschesttips.util.Reflection.implMethod;

@SuppressWarnings("unused")
public class AlwaysRenderBossChestTips {
    private static final boolean TEST_TWITCH_MODE = true;
    private static final Texture startScreenImage = ImageMaster.loadImage("resources.dvide.bosschesttips/images/FacesOfEvil.png");

    public static ArrayList<AbstractRelic> bossRelics = null;
    public static ArrayList<Optional<ArrayList<PowerTip>>> firstTipsOnly = null;

    @SpirePatch2(clz = BossRelicSelectScreen.class, method = "update")
    public static class BossRelicSelectScreen_update_Patches {
        @SpireInsertPatch(locator = ArrayList_clear_Locator.class)
        public static void forgetBossRelics(final BossRelicSelectScreen __instance) {
            forgetRelics();
        }

        private static class ArrayList_clear_Locator extends SpireInsertLocator {
            public int[] Locate(final CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                final Matcher methodCallMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "clear");
                return LineFinder.findInOrder(ctBehavior, methodCallMatcher);
            }
        }
    }

    @SpirePatch2(clz = BossRelicSelectScreen.class, method = "open")
    public static class BossRelicSelectScreen_open_Patches {
        @SpirePostfixPatch
        public static void rememberBossRelics(final BossRelicSelectScreen __instance) {
            rememberRelics(__instance.relics);
        }

        @SpireInstrumentPatch
        public static ExprEditor adjustRelicSpawnPositions() {
            return new ExprEditor() {
                private int callNum = 0;
                final Class<BossRelicSelectScreen_open_Patches> ourClass = BossRelicSelectScreen_open_Patches.class;

                @Override
                public void edit(final MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("spawn") && m.getClassName().equals(AbstractRelic.class.getName())) {
                        try {
                            m.replace("{ " + implMethod(ourClass) + "(" + callNum + ", (this.getVoter().isPresent()), $0, $1, $2); }");
                            callNum++;
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
        }

        public static void adjustRelicSpawnPositions_Impl(final int relicSlot, boolean twitchVoterPresent, final AbstractRelic relic, float x, float y) {
            final Point2D p = getAdjustedRelicSlotPosition(relicSlot, twitchVoterPresent, x, y);
            relic.spawn(p.x, p.y);
        }
    }

    @SpirePatch2(clz = BossRelicSelectScreen.class, method = "render")
    public static class BossRelicSelectScreen_render_Patches {
        @SpirePostfixPatch
        public static void renderMockTwitchVotes(final SpriteBatch sb) {
            if (TEST_TWITCH_MODE) {
                sb.draw(startScreenImage, Settings.WIDTH / 2.0f, 0.0f);

                final Point2D slot1 = getAdjustedRelicSlotPosition(0, true);
                final Point2D slot2 = getAdjustedRelicSlotPosition(1, true);
                final Point2D slot3 = getAdjustedRelicSlotPosition(2, true);
                final Point2D seconds = new Point2D((float)Settings.WIDTH * 0.275f, (float)Settings.HEIGHT * 0.175f);

                String msg = "#0: 14 (9%)";
                FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, msg, slot1.x, slot1.y - 75.0f * Settings.scale, Color.WHITE);
                msg = "#1: 100 (69%)";
                FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, msg, slot2.x, slot2.y - 75.0f * Settings.scale, Color.WHITE);
                msg = "#2: 30 (20%)";
                FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, msg, slot3.x, slot3.y - 75.0f * Settings.scale, Color.WHITE);

                FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, BossRelicSelectScreen.TEXT[4] + 26 + BossRelicSelectScreen.TEXT[5], seconds.x, seconds.y, Color.WHITE);
            }
        }
    }

    @SpirePatch2(clz = BossRelicSelectScreen.class, method = "renderTwitchVotes")
    @SpirePatch2(clz = NoSkipBossRelicPatch.class, method = "RenderVote", requiredModId = "versus", optional = true)
    public static class RenderTwitchVotes_Patches {
        @SpireInstrumentPatch
        public static ExprEditor adjustVoteTextPositions() {
            return new ExprEditor() {
                private int callNum = 0;
                final Class<RenderTwitchVotes_Patches> ourClass = RenderTwitchVotes_Patches.class;

                @Override
                public void edit(final MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("renderFontCentered") && m.getClassName().equals(FontHelper.class.getName())) {
                        try {
                            m.replace("{ " + implMethod(ourClass) + "(" + callNum + ", $1, $2, $3, $4, $5, $6); }");
                            callNum++;
                        } catch (NoSuchMethodException e) {
                            throw new CannotCompileException(e);
                        }
                    }
                }
            };
        }

        public static void adjustVoteTextPositions_Impl(final int callNum, final SpriteBatch sb, final BitmapFont font, final String msg, float x, float y, final Color c) {
            // We are rendering Twitch voting remaining time, so we want to adjust its
            // position towards the left. Otherwise bottom relic tip can often overlap.
            if (callNum == 3) {
                x = (float)Settings.WIDTH * 0.275f;
                y = (float)Settings.HEIGHT * 0.175f;
            }

            final Point2D p = getAdjustedRelicSlotPosition(callNum, true, x, y);
            FontHelper.renderFontCentered(sb, font, msg, p.x, p.y, c);
        }
    }

    @SpirePatch2(clz = AbstractRelic.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class AbstractRelic_render_Patches {
        @SpireInstrumentPatch
        public static ExprEditor doNotRenderNormalBossTip() {
            return new ExprEditor() {
                @Override
                public void edit(final MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("renderBossTip")) {
                        m.replace("{}");
                    }
                }
            };
        }
    }

    @SpirePatch2(clz = CardCrawlGame.class, method = "render")
    public static class CardCrawlGame_render_Patches {
        @SpireInsertPatch(locator = TipHelper_render_Locator.class)
        public static void renderBossChestTips(final CardCrawlGame __instance) {
            if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.BOSS_REWARD && AbstractDungeon.isPlayerInDungeon() && bossRelics != null && !Settings.hidePopupDetails && !CardCrawlGame.relicPopup.isOpen && !CardCrawlGame.cardPopup.isOpen) {
                final SpriteBatch sb = ReflectionHacks.getPrivate(__instance, CardCrawlGame.class, "sb");
                renderThreeBossRelicTips(sb);
            }
        }

        private static class TipHelper_render_Locator extends SpireInsertLocator {
            public int[] Locate(final CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                final Matcher methodCallMatcher = new Matcher.MethodCallMatcher(TipHelper.class, "render");
                return LineFinder.findInOrder(ctBehavior, methodCallMatcher);
            }
        }
    }

    public static void renderThreeBossRelicTips(final SpriteBatch sb) {
        assert bossRelics != null;
        final int atMost3Relics = Math.min(3, bossRelics.size());

        for (int i = 0; i < atMost3Relics; i++) {
            if (!bossRelics.get(i).isObtained) {
                renderBossRelicTip(bossRelics.get(i), i, sb);
            }
        }
    }

    @SuppressWarnings("OptionalIsPresent")
    public static void renderBossRelicTip(final AbstractRelic relic, int bossRelicIndex, final SpriteBatch sb) {
        final float tipBoxWidth = ReflectionHacks.getPrivateStatic(TipHelper.class, "BOX_W");

        float x, y;
        switch (bossRelicIndex) {
            case 0:
                x = Settings.WIDTH * 0.37f - tipBoxWidth;
                y = Settings.HEIGHT * 0.63f;
                break;
            case 1:
                x = Settings.WIDTH * 0.63f;
                y = Settings.HEIGHT * 0.63f;
                break;
            case 2:
                x = (Settings.WIDTH - tipBoxWidth) * 0.5f;
                y = Settings.HEIGHT * 0.39f;
                break;
            default:
                return;
        }

        if (!Settings.isTouchScreen && relic.hb.hovered) {
            renderPowerTips(relic.tips, sb, x, y);
        } else {
            Optional<ArrayList<PowerTip>> firstTip = firstTipsOnly.get(bossRelicIndex);

            if (firstTip.isPresent()) {
                renderPowerTips(firstTip.get(), sb, x, y);
            }
        }
    }

    private static void renderPowerTips(final ArrayList<PowerTip> tips, final SpriteBatch sb, float x, float y) {
        ReflectionHacks.privateStaticMethod(TipHelper.class, "renderPowerTips",
            float.class, float.class, SpriteBatch.class, ArrayList.class)
            .invoke(new Object[]{ x, y, sb, tips }
        );
    }

    private static void rememberRelics(final ArrayList<AbstractRelic> relics) {
        bossRelics = relics;
        firstTipsOnly = new ArrayList<>();

        final int atMost3Relics = Math.min(3, bossRelics.size());

        for (int i = 0; i < atMost3Relics; i++) {
            final AbstractRelic relic = bossRelics.get(i);
            if (relic.tips.isEmpty()){
                firstTipsOnly.add(Optional.empty());
            } else {
                ArrayList<PowerTip> firstTip = new ArrayList<>();
                firstTip.add(relic.tips.get(0));
                firstTipsOnly.add(Optional.of(firstTip));
            }
        }
    }

    private static void forgetRelics() {
        bossRelics = null;
        firstTipsOnly = null;
    }

    private static Point2D getAdjustedRelicSlotPosition(int relicSlot, boolean twitchVoterPresent, float x, float y) {
        if (relicSlot < 0 || relicSlot > 2) {
            return new Point2D(x, y);
        } else {
            return getAdjustedRelicSlotPosition(relicSlot, twitchVoterPresent);
        }
    }

    private static Point2D getAdjustedRelicSlotPosition(int relicSlot, boolean twitchVoterPresent) {
        final float slot1x = ReflectionHacks.getPrivateStatic(BossRelicSelectScreen.class, "SLOT_1_X");
        final float slot1y = ReflectionHacks.getPrivateStatic(BossRelicSelectScreen.class, "SLOT_1_Y");
        final float slot2x = ReflectionHacks.getPrivateStatic(BossRelicSelectScreen.class, "SLOT_2_X");
        final float slot2y = ReflectionHacks.getPrivateStatic(BossRelicSelectScreen.class, "SLOT_2_Y");
        final float slot3x = ReflectionHacks.getPrivateStatic(BossRelicSelectScreen.class, "SLOT_3_X");

        assert relicSlot >= 0 && relicSlot <= 2;

        // Change boss relic slot positions so that two boss relics are at the top (towards the left and right),
        // and one is at the bottom in the center. If Twich voting is present, adjust the positions a bit more.
        float x, y;
        switch (relicSlot) {
            case 0:
                x = slot2x - (twitchVoterPresent || TEST_TWITCH_MODE ? 15.0f * Settings.scale : 0.0f);
                y = slot1y + (twitchVoterPresent || TEST_TWITCH_MODE ? 20.0f * Settings.scale : 0.0f);
                break;
            case 1:
                x = slot3x + (twitchVoterPresent || TEST_TWITCH_MODE ? 15.0f * Settings.scale : 0.0f);
                y = slot1y + (twitchVoterPresent || TEST_TWITCH_MODE ? 20.0f * Settings.scale : 0.0f);
                break;
            case 2:
                x = slot1x;
                y = slot2y + (twitchVoterPresent || TEST_TWITCH_MODE ? 20.0f * Settings.scale : 0.0f);
                break;
            default:
                // This shouldn't ever trigger
                x = -9999.0f;
                y = -9999.0f;
                break;
        }

        return new Point2D(x, y);
    }

    private static class Point2D {
        public final float x;
        public final float y;

        public Point2D(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
