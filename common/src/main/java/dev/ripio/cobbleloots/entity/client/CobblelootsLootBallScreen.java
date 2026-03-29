package dev.ripio.cobbleloots.entity.client;

import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import dev.ripio.cobbleloots.network.CobblelootsLootBallUpdatePayload;
import dev.ripio.cobbleloots.network.CobblelootsClientNetworkSender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

public class CobblelootsLootBallScreen extends Screen {
    private final CobblelootsLootBall entity;
    private final CompoundTag snapshot;

    private EditBox textureBox;
    private CycleButton<Boolean> invisibleBtn;
    private CycleButton<Boolean> sparksBtn;
    private EditBox usesBox;
    private EditBox multiplierBox;
    private EditBox xpBox;
    private EditBox playerTimerBox;
    private EditBox despawnTickBox;
    private EditBox itemBox;
    private EditBox itemCountBox;

    private EditBox dataIdBox;
    private EditBox variantIdBox;

    // Internal helper for rendering labels
    private record FieldLabel(Component component, int x, int y) {
    }

    private final List<FieldLabel> fieldLabels = new ArrayList<>();

    private double scrollAmount = 0;
    private int maxScroll = 0;
    private final Map<AbstractWidget, Integer> widgetOriginalY = new IdentityHashMap<>();

    public CobblelootsLootBallScreen(CobblelootsLootBall entity, CompoundTag snapshot) {
        super(Component.translatable("gui.cobbleloots.loot_ball.title"));
        this.entity = entity;
        this.snapshot = snapshot;
    }

    @Override
    protected void init() {
        this.fieldLabels.clear();
        int centerX = this.width / 2;
        int col1 = centerX - 150;
        int col2 = centerX + 10;
        int w = 140;
        int h = 20;
        int y = 25;
        int gap = 35; // Vertical gap between rows

        String initialDataId = snapshot.getString("LootBallData");
        if (initialDataId.isEmpty())
            initialDataId = "";

        // Row 1 (Type - Full Width)
        this.fieldLabels.add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.data"), col1, y));
        this.dataIdBox = new EditBox(this.font, col1, y + 10, 300, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.data"));
        this.dataIdBox.setMaxLength(256);
        this.dataIdBox.setValue(initialDataId);
        this.dataIdBox.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.data")));
        this.addRenderableWidget(this.dataIdBox);

        // Row 2 (Variant - Full Width)
        y += gap;
        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.variant"), col1, y));
        this.variantIdBox = new EditBox(this.font, col1, y + 10, 300, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.variant"));
        this.variantIdBox.setMaxLength(256);
        this.variantIdBox.setValue(snapshot.getString("Variant"));
        this.variantIdBox
                .setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.variant")));
        this.addRenderableWidget(this.variantIdBox);

        // Row 3 (Texture - Full Width)
        y += gap;
        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.texture"), col1, y));
        this.textureBox = new EditBox(this.font, col1, y + 10, 300, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.texture"));
        this.textureBox.setMaxLength(256);
        this.textureBox.setValue(snapshot.getString("Texture"));
        this.textureBox.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.texture")));
        this.addRenderableWidget(this.textureBox);

        // Row 4
        y += gap;
        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.invisible"), col1, y));
        this.invisibleBtn = CycleButton.booleanBuilder(Component.literal("Yes"), Component.literal("No"))
                .withInitialValue(snapshot.getBoolean("Invisible"))
                .displayOnlyValue()
                .create(col1, y + 10, w, h, Component.empty());
        this.addRenderableWidget(this.invisibleBtn);

        this.fieldLabels.add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.sparks"), col2, y));
        this.sparksBtn = CycleButton.booleanBuilder(Component.literal("Yes"), Component.literal("No"))
                .withInitialValue(snapshot.contains("Sparks") ? snapshot.getBoolean("Sparks") : true)
                .displayOnlyValue()
                .create(col2, y + 10, w, h, Component.empty());
        this.addRenderableWidget(this.sparksBtn);

        // Row 5
        y += gap;
        this.fieldLabels.add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.uses"), col1, y));
        this.usesBox = new EditBox(this.font, col1, y + 10, w, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.uses"));
        this.usesBox.setMaxLength(32);
        this.usesBox.setValue(String.valueOf(snapshot.contains("Uses") ? snapshot.getInt("Uses") : 1));
        this.usesBox.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.uses")));
        this.addRenderableWidget(this.usesBox);

        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.multiplier"), col2, y));
        this.multiplierBox = new EditBox(this.font, col2, y + 10, w, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.multiplier"));
        this.multiplierBox.setMaxLength(32);
        this.multiplierBox
                .setValue(String.valueOf(snapshot.contains("Multiplier") ? snapshot.getFloat("Multiplier") : 1.0f));
        this.multiplierBox
                .setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.multiplier")));
        this.addRenderableWidget(this.multiplierBox);

        // Row 6
        y += gap;
        this.fieldLabels.add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.xp"), col1, y));
        this.xpBox = new EditBox(this.font, col1, y + 10, w, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.xp"));
        this.xpBox.setMaxLength(32);
        this.xpBox.setValue(String.valueOf(snapshot.getInt("XP")));
        this.xpBox.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.xp")));
        this.addRenderableWidget(this.xpBox);

        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.playertimer"), col2, y));
        this.playerTimerBox = new EditBox(this.font, col2, y + 10, w, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.playertimer"));
        this.playerTimerBox.setMaxLength(32);
        this.playerTimerBox.setValue(String.valueOf(snapshot.getLong("PlayerTimer")));
        this.playerTimerBox
                .setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.playertimer")));
        this.addRenderableWidget(this.playerTimerBox);

        // Row 7
        y += gap;
        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.despawntick"), col1, y));
        this.despawnTickBox = new EditBox(this.font, col1, y + 10, w, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.despawntick"));
        this.despawnTickBox.setMaxLength(32);
        this.despawnTickBox.setValue(String.valueOf(snapshot.getLong("DespawnTick")));
        this.despawnTickBox
                .setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.despawntick")));
        this.addRenderableWidget(this.despawnTickBox);

        // Row 8 (Item Row)
        y += gap;
        this.fieldLabels.add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.item"), col1, y));
        this.itemBox = new EditBox(this.font, col1, y + 10, 180, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.item"));
        this.itemBox.setMaxLength(256);

        int qtyCol = col1 + 190;
        this.fieldLabels
                .add(new FieldLabel(Component.translatable("gui.cobbleloots.loot_ball.field.itemcount"), qtyCol, y));
        this.itemCountBox = new EditBox(this.font, qtyCol, y + 10, 35, h,
                Component.translatable("gui.cobbleloots.loot_ball.field.itemcount"));
        this.itemCountBox.setMaxLength(32);

        if (snapshot.contains("Items", 9)) {
            net.minecraft.nbt.ListTag itemsList = snapshot.getList("Items", 10);
            if (!itemsList.isEmpty()) {
                CompoundTag itemTag = itemsList.getCompound(0);
                this.itemBox.setValue(itemTag.getString("id"));
                this.itemCountBox.setValue(String.valueOf(itemTag.getInt("count")));
            }
        } else {
            this.itemBox.setValue("");
            this.itemCountBox.setValue("1");
        }
        this.itemBox.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.item")));
        this.addRenderableWidget(this.itemBox);
        this.addRenderableWidget(this.itemCountBox);

        Button fromHandBtn = Button.builder(Component.translatable("gui.cobbleloots.loot_ball.btn.from_hand"), btn -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                ItemStack hand = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
                if (!hand.isEmpty()) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(hand.getItem());
                    this.itemBox.setValue(id.toString());
                    this.itemCountBox.setValue(String.valueOf(hand.getCount()));
                } else {
                    this.itemBox.setValue("");
                }
            }
        }).bounds(qtyCol + 45, y + 10, 65, h).build();
        fromHandBtn.setTooltip(Tooltip.create(Component.translatable("gui.cobbleloots.loot_ball.tooltip.from_hand")));
        this.addRenderableWidget(fromHandBtn);

        // Row 9 (Buttons)
        y += gap + 5;
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cobbleloots.loot_ball.save"), btn -> this.save())
                        .bounds(centerX - 105, y, 100, 20).build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cobbleloots.loot_ball.cancel"), btn -> this.onClose())
                        .bounds(centerX + 5, y, 100, 20).build());

        int maxY = y + 25;
        this.maxScroll = Math.max(0, maxY - this.height);

        this.widgetOriginalY.clear();
        for (GuiEventListener listener : this.children()) {
            if (listener instanceof AbstractWidget aw) {
                this.widgetOriginalY.put(aw, aw.getY());
            }
        }
        this.setScrollAmount(this.scrollAmount);
    }

    private void save() {
        CompoundTag update = new CompoundTag();
        update.putString("LootBallData", this.dataIdBox.getValue());
        update.putString("Variant", this.variantIdBox.getValue());
        update.putString("Texture", this.textureBox.getValue());
        update.putBoolean("Invisible", this.invisibleBtn.getValue());
        update.putBoolean("Sparks", this.sparksBtn.getValue());

        try {
            update.putInt("Uses", Integer.parseInt(this.usesBox.getValue()));
        } catch (Exception ignored) {
        }
        try {
            update.putFloat("Multiplier", Float.parseFloat(this.multiplierBox.getValue()));
        } catch (Exception ignored) {
        }
        try {
            update.putInt("XP", Integer.parseInt(this.xpBox.getValue()));
        } catch (Exception ignored) {
        }
        try {
            update.putLong("PlayerTimer", Long.parseLong(this.playerTimerBox.getValue()));
        } catch (Exception ignored) {
        }
        try {
            update.putLong("DespawnTick", Long.parseLong(this.despawnTickBox.getValue()));
        } catch (Exception ignored) {
        }

        update.putString("GUI_Item_Id", this.itemBox.getValue());
        try {
            update.putInt("GUI_Item_Count", Integer.parseInt(this.itemCountBox.getValue()));
        } catch (Exception e) {
            update.putInt("GUI_Item_Count", 1);
        }

        CobblelootsClientNetworkSender
                .sendLootBallUpdate(new CobblelootsLootBallUpdatePayload(this.entity.getId(), update));
        this.onClose();
    }

    private void setScrollAmount(double amount) {
        this.scrollAmount = Mth.clamp(amount, 0, this.maxScroll);
        int scroll = (int) this.scrollAmount;
        for (Map.Entry<AbstractWidget, Integer> entry : this.widgetOriginalY.entrySet()) {
            entry.getKey().setY(entry.getValue() - scroll);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll > 0) {
            this.setScrollAmount(this.scrollAmount - scrollY * 15.0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.maxScroll > 0 && button == 0 && mouseX >= this.width - 12) {
            double scrollRatio = dragY / (double) (this.height - 20) * this.maxScroll;
            this.setScrollAmount(this.scrollAmount + scrollRatio);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int scroll = (int) this.scrollAmount;
        for (FieldLabel lbl : this.fieldLabels) {
            graphics.drawString(this.font, lbl.component(), lbl.x(), lbl.y() - scroll, 0xE0E0E0, true);
        }

        // Header overlay
        graphics.fill(0, 0, this.width, 24, 0xDD000000); // Dark grey opaque header
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        // Scrollbar
        if (this.maxScroll > 0) {
            int scrollbarX = this.width - 6;
            int scrollbarY = 0;
            int scrollbarHeight = this.height;
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0x80000000);

            int thumbHeight = Math.max(20, (int) ((float) this.height / (this.height + this.maxScroll) * this.height));
            int thumbY = (int) ((float) this.scrollAmount / this.maxScroll * (this.height - thumbHeight));
            graphics.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFFFFFFFF);
        }
    }
}
